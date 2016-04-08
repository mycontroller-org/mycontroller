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
package org.mycontroller.standalone.api.jaxrs.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.mycontroller.standalone.AppProperties;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.api.jaxrs.ScriptsHandler;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.scripts.McScript;
import org.mycontroller.standalone.scripts.McScriptEngineUtils.SCRIPT_TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.UtilityClass;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

@UtilityClass
public class McServerScriptFileUtils {
    static final Logger _logger = LoggerFactory.getLogger(McServerScriptFileUtils.class.getName());

    //mc script file filter
    private static final String[] MC_SCRIPT_SUFFIX_FILTER = { "js", "py", "rb", "groovy" };

    @SuppressWarnings("unchecked")
    public static QueryResponse getScriptFiles(Query query) throws IOException {
        //Check less info available and true, if true set less info return
        Boolean lessInfo = (Boolean) query.getFilters().get(ScriptsHandler.KEY_LESS_INFO);
        if (lessInfo) {
            query.setPageLimit(-1);
        }

        String scriptsFileLocation = McObjectManager.getAppProperties().getScriptLocation();

        String filesLocation = null;
        if (query.getFilters().get(ScriptsHandler.KEY_TYPE) == null) {
            filesLocation = scriptsFileLocation;
        } else if (query.getFilters().get(ScriptsHandler.KEY_TYPE) == SCRIPT_TYPE.CONDITION) {
            filesLocation = McObjectManager.getAppProperties().getScriptConditionsLocation();
        } else if (query.getFilters().get(ScriptsHandler.KEY_TYPE) == SCRIPT_TYPE.OPERATION) {
            filesLocation = McObjectManager.getAppProperties().getScriptOperationsLocation();
        }

        String locationCanonicalPath = McUtils.getDirectoryLocation(FileUtils.getFile(scriptsFileLocation)
                .getCanonicalPath());

        if (FileUtils.getFile(filesLocation).exists()) {
            List<McScript> files = new ArrayList<McScript>();
            List<String> filesString = new ArrayList<String>();

            //Filters
            //Extension filter
            String[] scriptSuffixFilter = null;
            if (query.getFilters().get(ScriptsHandler.KEY_EXTENSION) != null) {
                if (Arrays.asList(MC_SCRIPT_SUFFIX_FILTER).contains(
                        query.getFilters().get(ScriptsHandler.KEY_EXTENSION))) {
                    scriptSuffixFilter = new String[] { (String) query.getFilters().get(ScriptsHandler.KEY_EXTENSION) };
                }
            }

            if (scriptSuffixFilter == null) {
                scriptSuffixFilter = MC_SCRIPT_SUFFIX_FILTER;
            }
            SuffixFileFilter languageFilter = new SuffixFileFilter(scriptSuffixFilter, IOCase.INSENSITIVE);

            //name filter
            IOFileFilter nameFileFilter = null;
            List<String> fileNames = (List<String>) query.getFilters().get(ScriptsHandler.KEY_NAME);
            if (fileNames != null && !fileNames.isEmpty()) {
                for (String fileName : fileNames) {
                    if (nameFileFilter == null) {
                        nameFileFilter = FileFilterUtils.and(
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    } else {
                        nameFileFilter = FileFilterUtils.and(nameFileFilter,
                                new WildcardFileFilter("*" + fileName + "*", IOCase.INSENSITIVE));
                    }
                }
            }

            //Combine all filters
            IOFileFilter scriptsFileFilter = null;
            if (nameFileFilter != null) {
                scriptsFileFilter = FileFilterUtils.and(languageFilter, nameFileFilter);
            } else {
                scriptsFileFilter = languageFilter;
            }
            List<File> scriptFiles = new ArrayList<File>(FileUtils.listFiles(FileUtils.getFile(filesLocation),
                    scriptsFileFilter, TrueFileFilter.INSTANCE));
            query.setFilteredCount((long) scriptFiles.size());
            //Get total items without filter
            query.setTotalItems((long) FileUtils.listFiles(FileUtils.getFile(scriptsFileLocation),
                    new SuffixFileFilter(MC_SCRIPT_SUFFIX_FILTER, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE).size());

            int fileFrom;
            int fileTo;
            if (query.getPageLimit() == -1) {
                fileTo = scriptFiles.size();
                fileFrom = 0;
            } else {
                fileFrom = query.getStartingRow().intValue();
                fileTo = (int) (query.getPage() * query.getPageLimit());
            }
            for (; fileFrom < fileTo; fileFrom++) {
                if (scriptFiles.size() > fileFrom) {
                    File scriptFile = scriptFiles.get(fileFrom);
                    String name = scriptFile.getCanonicalPath().replace(locationCanonicalPath, "");
                    if (lessInfo) {
                        filesString.add(name);
                    } else {
                        files.add(McScript.builder()
                                .name(name)
                                .extension(FilenameUtils.getExtension(scriptFile.getCanonicalPath()))
                                .size(scriptFile.length())
                                .lastModified(scriptFile.lastModified())
                                .build());
                    }
                } else {
                    break;
                }
            }
            if (lessInfo) {
                return QueryResponse.builder().data(filesString).query(query).build();
            } else {
                return QueryResponse.builder().data(files).query(query).build();
            }

        } else {
            throw new FileNotFoundException("File location not found: " + locationCanonicalPath);
        }
    }

    public static void deleteScriptFiles(List<String> scriptFiles) throws IOException {
        String scriptsFileLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                McObjectManager.getAppProperties().getScriptLocation()).getCanonicalPath());
        for (String scriptFile : scriptFiles) {
            String fileFullPath = scriptsFileLocation + scriptFile;
            if (isInScope(scriptsFileLocation, fileFullPath)) {
                if (FileUtils.deleteQuietly(FileUtils.getFile(fileFullPath))) {
                    _logger.debug("File deletion successfully! {}", fileFullPath);
                } else {
                    _logger.warn("File deletion failed! {}", fileFullPath);
                }
            } else {
                _logger.warn("Trying to delete file from outside scope! Filepath:{}, CanonicalPath:{}",
                        fileFullPath,
                        FileUtils.getFile(fileFullPath).getCanonicalPath());
            }
        }
    }

    public static McScript getScriptFile(String scriptFile) throws IOException, IllegalAccessException,
            McBadRequestException {
        String scriptsFileLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                McObjectManager.getAppProperties().getScriptLocation()).getCanonicalPath());
        String fileFullPath = scriptsFileLocation + scriptFile;
        if (isInScope(scriptsFileLocation, fileFullPath)) {
            if (!FileUtils.getFile(fileFullPath).exists()) {
                throw new McBadRequestException("File not found! " + fileFullPath);
            }
            File fileScript = FileUtils.getFile(fileFullPath);

            McScript mcScript = McScript.builder()
                    .extension(FilenameUtils.getExtension(fileScript.getCanonicalPath()))
                    .size(fileScript.length())
                    .lastModified(fileScript.lastModified())
                    .data(FileUtils.readFileToString(fileScript, StandardCharsets.UTF_8))
                    .build();

            String name = fileScript.getCanonicalPath().replace(scriptsFileLocation, "");
            SCRIPT_TYPE type = null;
            if (name.startsWith(AppProperties.CONDITIONS_SCRIPT_DIRECTORY)) {
                type = SCRIPT_TYPE.CONDITION;
            } else {
                type = SCRIPT_TYPE.OPERATION;
            }
            mcScript.setType(type);
            name = name.replace(AppProperties.CONDITIONS_SCRIPT_DIRECTORY, "")
                    .replace(AppProperties.OPERATIONS_SCRIPT_DIRECTORY, "");
            mcScript.setName(FilenameUtils.getBaseName(name));
            return mcScript;
        } else {
            _logger.warn("Trying to delete file from outside scope! Filepath:{}, CanonicalPath:{}",
                    fileFullPath,
                    FileUtils.getFile(fileFullPath).getCanonicalPath());
            throw new IllegalAccessException("Trying to delete file from outside scope!");
        }
    }

    public static void uploadScript(McScript mcScript) throws IOException, IllegalAccessException,
            McBadRequestException {
        if (mcScript == null
                || mcScript.getType() == null
                || mcScript.getType() == null
                || mcScript.getData() == null
                || mcScript.getExtension() == null
                || mcScript.getName() == null) {
            throw new McBadRequestException("Required parameter(s) missing!");
        }
        String fileLocation = McUtils.getDirectoryLocation(FileUtils.getFile(
                McObjectManager.getAppProperties().getScriptLocation()).getCanonicalPath());
        String fileFullPath = null;
        if (mcScript.getType() == SCRIPT_TYPE.CONDITION) {
            fileFullPath = fileLocation + AppProperties.CONDITIONS_SCRIPT_DIRECTORY
                    + mcScript.getName() + "." + mcScript.getExtension();
        } else {
            fileFullPath = fileLocation + AppProperties.OPERATIONS_SCRIPT_DIRECTORY
                    + mcScript.getName() + "." + mcScript.getExtension();
        }
        FileUtils.writeStringToFile(FileUtils.getFile(fileFullPath), (String) mcScript.getData(), false);
        if (_logger.isDebugEnabled()) {
            _logger.debug("Write success! File:{}", fileFullPath);
        }
    }

    private static boolean isInScope(String scopeLocation, String fileFullPath) throws IOException {
        if (FileUtils.getFile(fileFullPath).getCanonicalPath().startsWith(scopeLocation)) {
            return true;
        }
        return false;
    }
}
