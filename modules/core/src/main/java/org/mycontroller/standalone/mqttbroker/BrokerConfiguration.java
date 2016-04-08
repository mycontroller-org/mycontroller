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

import static io.moquette.BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME;
import static io.moquette.BrokerConstants.AUTHENTICATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.AUTHORIZATOR_CLASS_NAME;
import static io.moquette.BrokerConstants.HOST_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PASSWORD_FILE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME;
import static io.moquette.BrokerConstants.PORT_PROPERTY_NAME;
import static io.moquette.BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME;

import java.util.Properties;

import org.h2.store.fs.FileUtils;
import org.mycontroller.standalone.McObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.moquette.server.config.IConfig;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class BrokerConfiguration implements IConfig {
    private static final Logger _logger = LoggerFactory.getLogger(BrokerConfiguration.class.getName());

    private final Properties m_properties = new Properties();

    public BrokerConfiguration() {
        createDefaultLocations();
        loadProperties();
    }

    private void createDefaultLocations() {
        FileUtils.createDirectory(FileUtils.getParent(McObjectManager.getAppProperties().
                getMqttBrokerPersistentStore()));
    }

    private void loadProperties() {
        m_properties.put(HOST_PROPERTY_NAME, McObjectManager.getAppProperties().getMqttBrokerBindAddress());
        m_properties.put(PORT_PROPERTY_NAME, String.valueOf(McObjectManager.getAppProperties().getMqttBrokerPort()));
        m_properties.put(WEB_SOCKET_PORT_PROPERTY_NAME,
                String.valueOf(McObjectManager.getAppProperties().getMqttBrokerWebsocketPort()));

        m_properties.put(PASSWORD_FILE_PROPERTY_NAME, "");
        m_properties.put(PERSISTENT_STORE_PROPERTY_NAME,
                McObjectManager.getAppProperties().getMqttBrokerPersistentStore());
        m_properties.put(ALLOW_ANONYMOUS_PROPERTY_NAME, "true");

        m_properties.put(AUTHENTICATOR_CLASS_NAME, MqttAuthenticatorImpl.class.getName());
        m_properties.put(AUTHORIZATOR_CLASS_NAME, MqttAuthorizatorImpl.class.getName());
        if (_logger.isDebugEnabled()) {
            _logger.debug("Properties:[{}]", m_properties);
        }
    }

    @Override
    public void setProperty(String name, String value) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Set property [name:{}, value:{}]", name, value);
        }
        m_properties.setProperty(name, value);
    }

    @Override
    public String getProperty(String name) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Get property [name:{}, value:{}]", name, m_properties.getProperty(name));
        }
        return m_properties.getProperty(name);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("Get property with default value [name:{}, value:{}, defaultValue:{}]", name,
                    m_properties.getProperty(name), defaultValue);
        }
        return m_properties.getProperty(name, defaultValue);
    }

}
