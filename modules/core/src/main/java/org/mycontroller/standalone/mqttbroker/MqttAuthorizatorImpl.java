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
package org.mycontroller.standalone.mqttbroker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.spi.security.IAuthorizator;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class MqttAuthorizatorImpl implements IAuthorizator {
    private static final Logger _logger = LoggerFactory.getLogger(MqttAuthorizatorImpl.class.getName());

    @Override
    public boolean canRead(String topic, String user, String client) {
        _logger.debug("Can read check for Topic:{}, User:{}, Client:{}", topic, user, client);
        return true;
    }

    @Override
    public boolean canWrite(String topic, String user, String client) {
        _logger.debug("Can write check for Topic:{}, User:{}, Client:{}", topic, user, client);
        return true;
    }

}
