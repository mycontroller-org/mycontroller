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
package org.mycontroller.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.mycontroller.standalone.AppProperties.MC_LANGUAGE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class McUtils {
    private static final Logger _logger = LoggerFactory.getLogger(McUtils.class);
    public static final int DOUBLE_ROUND = 3;
    public static final long SECOND = 1000;
    public static final long MINUTE = SECOND * 60;
    public static final long HOUR = MINUTE * 60;
    public static final long DAY = HOUR * 24;
    public static final DecimalFormat decimalFormat = new DecimalFormat("#.###");
    public static final String MC_LOCALE_FILE_NAME = "mc_locale/mc_locale_java";

    public static final long KB = 1024;
    public static final long MB = 1024 * KB;
    public static final long GB = 1024 * MB;
    public static final long TB = 1024 * GB;

    public static final long TEN_MILLISECONDS = 10;
    public static final long ONE_SECOND = TEN_MILLISECONDS * 100;
    public static final long ONE_MINUTE = ONE_SECOND * 60;   // 1 minute
    public static final long FIVE_MINUTES = ONE_MINUTE * 5;  // 5 minute
    public static final long THREE_MINUTES = ONE_MINUTE * 3; // 5 minute
    public static final long ONE_HOUR = ONE_MINUTE * 60;     // 1 hour
    public static final long ONE_DAY = ONE_HOUR * 24;        // 1 day
    public static final long ONE_YEAR = ONE_DAY * 365;       // 1 year

    private McUtils() {

    }

    public static String getRandomAlphanumeric() {
        return getRandomAlphanumeric(12);
    }

    public static String getRandomAlphanumeric(int count) {
        return RandomStringUtils.randomAlphanumeric(count);
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double getDouble(Object value) {
        return getDouble(String.valueOf(value));
    }

    public static Double getDouble(String value) {
        if (value != null) {
            return round(Double.valueOf(value), DOUBLE_ROUND);
        }
        return null;
    }

    public static String getDoubleAsString(double value) {
        Double truncatedDouble = new BigDecimal(value).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        return String.valueOf(truncatedDouble);
    }

    public static String getDoubleAsString(String value) {
        try {
            if (value != null) {
                return getDoubleAsString(Double.valueOf(value));
            } else {
                return "-";
            }
        } catch (NumberFormatException nex) {
            _logger.error("Error on conversion! Input:[{}],", value, nex);
            return null;
        }

    }

    public static Integer getInteger(String value) {
        if (value != null) {
            return Integer.valueOf(value);
        } else {
            return null;
        }
    }

    public static Long getLong(String value) {
        if (value != null) {
            return Long.valueOf(value);
        } else {
            return null;
        }
    }

    public static Boolean getBoolean(Object value) {
        return getBoolean(String.valueOf(value));
    }

    public static Boolean getBoolean(String value) {
        if (value != null) {
            if (value.matches("\\d+")) {
                return getDouble(value) > 0 ? true : false;
            } else {
                return Boolean.valueOf(value);
            }
        } else {
            return null;
        }
    }

    public static String getStatusAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "OFF" : "ON";
        } else {
            return "-";
        }
    }

    public static String getArmedAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Bypassed" : "Armed";
        } else {
            return "-";
        }
    }

    public static String getTrippedAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Untripped" : "Tripped";
        } else {
            return "-";
        }
    }

    public static String getLockStatusAsString(String value) {
        if (value != null) {
            return value.equalsIgnoreCase("0") ? "Unlocked" : "Locked";
        } else {
            return "-";
        }
    }

    public static String getDifferenceFriendlyTime(long timestamp) {
        long diffMills = (System.currentTimeMillis() - timestamp);
        String friendlyTime = getFriendlyTime(diffMills, false);
        if (friendlyTime.contains("Now")) {
            return friendlyTime;
        } else {
            return friendlyTime + " ago";
        }
    }

    private static void updateFriendlyTime(StringBuilder builder, long milliseconds) {
        long diffMills = milliseconds;
        long diffSeconds = diffMills / SECOND;
        long diffMinutes = diffMills / MINUTE;
        long diffHours = diffMills / HOUR;
        long diffDays = diffMills / DAY;
        if (milliseconds >= SECOND) {
            if (builder.length() > 0) {
                builder.append(" ");
            }
            if (diffDays > 0) {
                builder.append(diffDays);
                if (diffDays == 1) {
                    builder.append(" Day");
                } else {
                    builder.append(" Days");
                }
            } else if (diffHours > 0) {
                builder.append(diffHours);
                if (diffHours == 1) {
                    builder.append(" Hour");
                } else {
                    builder.append(" Hours");
                }
            } else if (diffMinutes > 0) {
                builder.append(diffMinutes);
                if (diffMinutes == 1) {
                    builder.append(" Minute");
                } else {
                    builder.append(" Minutes");
                }
            } else if (diffSeconds > 0) {
                builder.append(diffSeconds);
                if (diffSeconds == 1) {
                    builder.append(" Second");
                } else {
                    builder.append(" Seconds");
                }
            }
        }
    }

    public static String getFriendlyTime(Long milliseconds, boolean strict) {
        return getFriendlyTime(milliseconds, strict, "Now");
    }

    public static String getFriendlyTime(Long milliseconds, boolean strict, String forZero) {
        if (milliseconds == null) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();

        if (strict) {
            while (milliseconds >= SECOND) {
                if (milliseconds >= DAY) {
                    updateFriendlyTime(builder, milliseconds);
                    milliseconds = milliseconds % DAY;
                } else if (milliseconds >= HOUR) {
                    updateFriendlyTime(builder, milliseconds);
                    milliseconds = milliseconds % HOUR;
                } else if (milliseconds >= MINUTE) {
                    updateFriendlyTime(builder, milliseconds);
                    milliseconds = milliseconds % MINUTE;
                } else if (milliseconds >= SECOND) {
                    updateFriendlyTime(builder, milliseconds);
                    milliseconds = milliseconds % SECOND;
                } else {
                    break;
                }
            }
        } else {
            updateFriendlyTime(builder, milliseconds);
        }

        if (builder.length() == 0) {
            builder.append(forZero);
        }
        return builder.toString();
    }

    public static void updateLocale(MC_LANGUAGE mcLanguage) {
        String[] locale = mcLanguage.name().split("_");
        McObjectManager.setMcLocale(ResourceBundle.getBundle(MC_LOCALE_FILE_NAME,
                new Locale(locale[0].toLowerCase(), locale[1].toUpperCase())));
    }

    public static void updateLocale() {
        updateLocale(MC_LANGUAGE.fromString(McObjectManager.getAppProperties().getControllerSettings().getLanguage()));
    }

    /* file utils*/
    public static void addFileToZip(String fileName, ZipOutputStream zos, String removePrefix)
            throws FileNotFoundException, IOException {
        addFileToZip(FileUtils.getFile(fileName), zos, removePrefix);
    }

    public static void addFileToZip(File srcFile, ZipOutputStream zos, String removePrefix)
            throws FileNotFoundException, IOException {
        byte[] bytes = new byte[1024];
        for (File file : srcFile.listFiles()) {
            if (file.isDirectory()) {
                addFileToZip(file, zos, removePrefix);
                continue;
            }
            if (_logger.isDebugEnabled()) {
                _logger.debug("Writing '{}' to zip file", file.getAbsolutePath());
            }
            FileInputStream fis = new FileInputStream(file.getAbsolutePath());
            zos.putNextEntry(new ZipEntry(file.getCanonicalPath().replace(removePrefix, "")));

            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    public static void createZipFile(String directoryName, String zipFileName) throws IOException {
        directoryName = FileUtils.getFile(directoryName).getCanonicalPath();
        File directory = FileUtils.getFile(directoryName);
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName));
        if (_logger.isDebugEnabled()) {
            _logger.debug("Creating: {}", zipFileName);
        }
        addFileToZip(directory, zos, directoryName);
        zos.close();
    }

    public static String getDirectoryLocation(String directoryLocation) {
        if (!directoryLocation.endsWith(File.separator)) {
            directoryLocation = directoryLocation + File.separator;
        }
        return directoryLocation;
    }

}
