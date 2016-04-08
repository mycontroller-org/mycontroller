/*
 * Copyright 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.backup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.BadRequestException;

import org.apache.commons.io.FileUtils;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.StartApp;
import org.mycontroller.standalone.api.jaxrs.json.BackupFile;
import org.mycontroller.standalone.db.DataBaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class Restore implements Runnable {
    private static final Logger _logger = LoggerFactory.getLogger(Restore.class.getName());
    private BackupFile backupFile;

    public Restore(BackupFile backupFile) {
        this.backupFile = backupFile;
    }

    public static void restore(BackupFile backupFile) throws IOException {
        if (BRCommons.isBackupRestoreRunning()) {
            throw new BadRequestException("A backup or restore is running");
        }

        if (!backupFile.getName().contains(BRCommons.FILE_NAME_IDENTITY)) {
            throw new BadRequestException("backup file name should contain '" + BRCommons.FILE_NAME_IDENTITY
                    + "'. Your input:"
                    + backupFile.getName());
        }

        BRCommons.setBackupRestoreRunning(true);

        String extractedLocation = McObjectManager.getAppProperties().getTmpLocation()
                + backupFile.getName().replaceAll(".zip", "");
        try {

            String oldDatabaseLocation = McObjectManager.getAppProperties().getDbH2DbLocation();
            //Extract zip file
            _logger.debug("Zip file:{}", backupFile.getAbsolutePath());

            extractZipFile(backupFile.getAbsolutePath(), extractedLocation);
            _logger.debug("All the files extracted to '{}'", extractedLocation);

            //Validate required files
            if (!FileUtils.getFile(extractedLocation + File.separator + BRCommons.DATABASE_FILENAME).exists()) {
                _logger.error("Unable to continue restore opration! selected file not found! File:{}",
                        extractedLocation + File.separator + BRCommons.DATABASE_FILENAME);
                return;
            }

            //Stop all services
            StartApp.stopServices();

            //Restore properties file
            //Remove old properties file
            FileUtils
                    .deleteQuietly(FileUtils.getFile(BRCommons.APP_CONF_LOCATION + BRCommons.APP_PROPERTIES_FILENAME));
            FileUtils.moveFile(
                    FileUtils.getFile(extractedLocation + File.separator + BRCommons.APP_PROPERTIES_FILENAME),
                    FileUtils.getFile(BRCommons.APP_CONF_LOCATION + BRCommons.APP_PROPERTIES_FILENAME));

            //Load initial properties
            StartApp.loadInitialProperties();

            if (McObjectManager.getAppProperties().isWebHttpsEnabled()) {
                //Remove old files
                FileUtils.deleteQuietly(FileUtils.getFile(McObjectManager.getAppProperties().getWebSslKeystoreFile()));
                //restore key store file
                FileUtils.moveFile(
                        FileUtils.getFile(extractedLocation + File.separator + FileUtils.getFile(
                                McObjectManager.getAppProperties().getWebSslKeystoreFile()).getName()),
                        FileUtils.getFile(McObjectManager.getAppProperties().getWebSslKeystoreFile()));
            }

            //Restore scripts directory
            //Remove old files
            FileUtils.deleteQuietly(FileUtils.getFile(McObjectManager.getAppProperties().getScriptLocation()));
            //restore
            FileUtils.copyDirectory(
                    FileUtils.getFile(extractedLocation + File.separator + BRCommons.SCRIPTS_LOCATION),
                    FileUtils.getFile(McObjectManager.getAppProperties().getScriptLocation()),
                    true);

            //remove old database
            if (FileUtils.deleteQuietly(FileUtils.getFile(oldDatabaseLocation + ".h2.db"))) {
                _logger.debug("Old database removed successfully");
            } else {
                _logger.warn("Unable to remove old database");
            }
            //restore database
            if (DataBaseUtils.restoreDatabase(extractedLocation + File.separator + BRCommons.DATABASE_FILENAME)) {
                _logger.info("Restore completed successfully. Start '{}' server manually",
                        AppProperties.APPLICATION_NAME);
            } else {
                _logger.error("Restore failed!");
            }

        } finally {
            //clean tmp file
            FileUtils.deleteQuietly(FileUtils.getFile(extractedLocation));
            _logger.debug("Tmp location[{}] clean success", extractedLocation);
            BRCommons.setBackupRestoreRunning(false);
        }
        //Stop application
        System.exit(0);
    }

    private static void extractZipFile(String zipFileName, String destination)
            throws FileNotFoundException, IOException {
        ZipFile zipFile = new ZipFile(zipFileName);
        Enumeration<?> enu = zipFile.entries();
        //create destination if not exists
        FileUtils.forceMkdir(FileUtils.getFile(destination));
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();
            String name = zipEntry.getName();
            long size = zipEntry.getSize();
            long compressedSize = zipEntry.getCompressedSize();
            _logger.debug("name:{} | size:{} | compressed size:{}", name, size, compressedSize);
            File file = FileUtils.getFile(destination + File.separator + name);
            //Create destination if it's not available
            if (name.endsWith(File.separator)) {
                file.mkdirs();
                continue;
            }

            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }

            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();
        }
        zipFile.close();
    }

    @Override
    public void run() {
        try {
            //wait few seconds to response for caller
            Thread.sleep(McUtils.ONE_SECOND * 3);
            restore(backupFile);
        } catch (Exception ex) {
            _logger.error("Restore failed!", ex);
        }

    }
}
