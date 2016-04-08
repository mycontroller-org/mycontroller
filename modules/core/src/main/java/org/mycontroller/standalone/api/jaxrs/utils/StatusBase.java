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

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.management.OperatingSystemMXBean;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class StatusBase {
    static final Logger _logger = LoggerFactory.getLogger(StatusBase.class.getName());

    static OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(
            OperatingSystemMXBean.class);
    static ClassLoadingMXBean classLoadingMXBean = ManagementFactory.getPlatformMXBean(ClassLoadingMXBean.class);
    static List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
    static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    static ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    static Runtime runtime = Runtime.getRuntime();
    static final long MB_SIZE = 1024 * 1024;

}
