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
package org.mycontroller.standalone.message;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.AppProperties.UNIT_CONFIG;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.metrics.MetricsUtils.METRIC_TYPE;
import org.mycontroller.standalone.provider.mysensors.MySensorsProviderBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* All the messages based on MYSENSORS.ORG, Do not add new */
/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class McMessageUtils {
    private static final Logger _logger = LoggerFactory.getLogger(McMessageUtils.class.getName());

    private McMessageUtils() {

    }

    private static HashMap<Integer, Boolean> discoverRunning = new HashMap<Integer, Boolean>();

    public static synchronized boolean isDiscoverRunning(int gatewayId) {
        if (discoverRunning.get(gatewayId) == null) {
            discoverRunning.put(gatewayId, false);
        }
        return discoverRunning.get(gatewayId);
    }

    public static synchronized void updateDiscoverRunning(int gatewayId, boolean status) {
        discoverRunning.put(gatewayId, status);
    }

    // Message types
    public enum MESSAGE_TYPE {
        C_PRESENTATION("Presentation"),
        C_SET("Set"),
        C_REQ("Request"),
        C_INTERNAL("Internal"),
        C_STREAM("Stream"); // For Firmware and other larger chunks of data that need to be divided into pieces

        public static MESSAGE_TYPE get(int id) {
            for (MESSAGE_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MESSAGE_TYPE(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE type : MESSAGE_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of internal messages (for internal messages)
    public enum MESSAGE_TYPE_INTERNAL {
        I_BATTERY_LEVEL("Battery level"),
        I_TIME("Time"),
        I_VERSION("Version"),
        I_ID_REQUEST("Id request"),
        I_ID_RESPONSE("Id response"),
        I_INCLUSION_MODE("Inclusion mode"),
        I_CONFIG("Config"),
        I_FIND_PARENT("Find parent"),
        I_FIND_PARENT_RESPONSE("Find parent response"),
        I_LOG_MESSAGE("Log message"),
        I_CHILDREN("Children"),
        I_SKETCH_NAME("Sketch name"),
        I_SKETCH_VERSION("Sketch version"),
        I_REBOOT("Reboot"),
        I_GATEWAY_READY("GatewayTable ready"),
        I_REQUEST_SIGNING("Request signing"),
        I_GET_NONCE("Get nonce"),
        I_GET_NONCE_RESPONSE("Get nonce response"),
        I_HEARTBEAT("Heartbeat"),
        I_PRESENTATION("Presentation"),
        I_DISCOVER("Discover"),
        I_DISCOVER_RESPONSE("Discover respone"),
        I_HEARTBEAT_RESPONSE("Heartbeat Response");
        public static MESSAGE_TYPE_INTERNAL get(int id) {
            for (MESSAGE_TYPE_INTERNAL type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MESSAGE_TYPE_INTERNAL(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_INTERNAL fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_INTERNAL type : MESSAGE_TYPE_INTERNAL.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor  (for presentation message)
    public enum MESSAGE_TYPE_PRESENTATION {
        S_DOOR("Door"),
        S_MOTION("Motion"),
        S_SMOKE("Smoke"),
        S_BINARY("Binary"),
        S_DIMMER("Dimmer"),
        S_COVER("Cover"),
        S_TEMP("Temperature"),
        S_HUM("Humidity"),
        S_BARO("Barometer"),
        S_WIND("Wind"),
        S_RAIN("Rain"),
        S_UV("UV"),
        S_WEIGHT("Weight"),
        S_POWER("Power"),
        S_HEATER("Heater"),
        S_DISTANCE("Distance"),
        S_LIGHT_LEVEL("Light level"),
        S_ARDUINO_NODE("Node"),
        S_ARDUINO_REPEATER_NODE("Repeater node"),
        S_LOCK("Lock"),
        S_IR("IR"),
        S_WATER("Water"),
        S_AIR_QUALITY("Air quality"),
        S_CUSTOM("Custom"),
        S_DUST("Dust"),
        S_SCENE_CONTROLLER("Scene controller"),
        S_RGB_LIGHT("RGB light"),
        S_RGBW_LIGHT("RGBW light"),
        S_COLOR_SENSOR("Color sensor"),
        S_HVAC("HVAC"),
        S_MULTIMETER("Multimeter"),
        S_SPRINKLER("Sprinkler"),
        S_WATER_LEAK("Water leak"),
        S_SOUND("Sound"),
        S_VIBRATION("Vibration"),
        S_MOISTURE("Moisture"),
        S_INFO("Information"),
        S_GAS("Gas"),
        S_GPS("GPS");

        public static MESSAGE_TYPE_PRESENTATION get(int id) {
            for (MESSAGE_TYPE_PRESENTATION type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MESSAGE_TYPE_PRESENTATION(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_PRESENTATION fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_PRESENTATION type : MESSAGE_TYPE_PRESENTATION.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of sensor data (for set/req/ack messages)
    public enum MESSAGE_TYPE_SET_REQ {
        V_TEMP("Temperature"),
        V_HUM("Humidity"),
        V_STATUS("Status"),
        V_PERCENTAGE("Percentage"),
        V_PRESSURE("Pressure"),
        V_FORECAST("Forecast"),
        V_RAIN("Rain"),
        V_RAINRATE("Rain rate"),
        V_WIND("Wind"),
        V_GUST("Gust"),
        V_DIRECTION("Direction"),
        V_UV("UV"),
        V_WEIGHT("Weight"),
        V_DISTANCE("Distance"),
        V_IMPEDANCE("Impedance"),
        V_ARMED("Armed"),
        V_TRIPPED("Tripped"),
        V_WATT("Watt"),
        V_KWH("KWh"),
        V_SCENE_ON("Scene ON"),
        V_SCENE_OFF("Scene OFF"),
        V_HVAC_FLOW_STATE("HVAC flow state"),
        V_HVAC_SPEED("HVAC speed"),
        V_LIGHT_LEVEL("Light level"),
        V_VAR1("Variable 1"),
        V_VAR2("Variable 2"),
        V_VAR3("Variable 3"),
        V_VAR4("Variable 4"),
        V_VAR5("Variable 5"),
        V_UP("Up"),
        V_DOWN("Down"),
        V_STOP("Stop"),
        V_IR_SEND("IR send"),
        V_IR_RECEIVE("IR receive"),
        V_FLOW("Flow"),
        V_VOLUME("Volume"),
        V_LOCK_STATUS("Lock status"),
        V_LEVEL("Level"),
        V_VOLTAGE("Voltage"),
        V_CURRENT("Current"),
        V_RGB("RGB"),
        V_RGBW("RGBW"),
        V_ID("KEY_ID"),
        V_UNIT_PREFIX("Unit prefix"),
        V_HVAC_SETPOINT_COOL("HVAC setpoint cool"),
        V_HVAC_SETPOINT_HEAT("HVAC setpoint heat"),
        V_HVAC_FLOW_MODE("HVAC flow mode"),
        V_TEXT("Text"),
        V_CUSTOM("Custom"),
        V_POSITION("Position"),
        V_IR_RECORD("IR record");

        public static MESSAGE_TYPE_SET_REQ get(int id) {
            for (MESSAGE_TYPE_SET_REQ type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MESSAGE_TYPE_SET_REQ(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_SET_REQ fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_SET_REQ type : MESSAGE_TYPE_SET_REQ.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    // Type of data stream  (for streamed message)
    public enum MESSAGE_TYPE_STREAM {
        ST_FIRMWARE_CONFIG_REQUEST("Firmware config request"),
        ST_FIRMWARE_CONFIG_RESPONSE("Firmware config response"),
        ST_FIRMWARE_REQUEST("Firmware request"),
        ST_FIRMWARE_RESPONSE("Firmware response"),
        ST_SOUND("Sound"),
        ST_IMAGE("Image");
        public static MESSAGE_TYPE_STREAM get(int id) {
            for (MESSAGE_TYPE_STREAM type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        private String text;

        public String getText() {
            return this.text;
        }

        private MESSAGE_TYPE_STREAM(String text) {
            this.text = text;
        }

        public static MESSAGE_TYPE_STREAM fromString(String text) {
            if (text != null) {
                for (MESSAGE_TYPE_STREAM type : MESSAGE_TYPE_STREAM.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public enum PAYLOAD_TYPE {
        PL_DOUBLE, PL_BOOLEAN, PL_INTEGER, PL_FLOAT, PL_BYTE, PL_HEX, PL_STRING;
    }

    public static METRIC_TYPE getMetricType(PAYLOAD_TYPE payloadType) {
        switch (payloadType) {
            case PL_BOOLEAN:
                return METRIC_TYPE.BINARY;
            case PL_DOUBLE:
                return METRIC_TYPE.DOUBLE;
            default:
                return METRIC_TYPE.NONE;
        }
    }

    public static METRIC_TYPE getMetricType(MESSAGE_TYPE_SET_REQ type_set_req) {
        return getMetricType(getPayLoadType(type_set_req));
    }

    public static PAYLOAD_TYPE getPayLoadType(MESSAGE_TYPE_SET_REQ type_set_req) {
        switch (type_set_req) {
            case V_TEMP:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_HUM:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_STATUS:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_PERCENTAGE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_PRESSURE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_FORECAST:
                return PAYLOAD_TYPE.PL_STRING;
            case V_RAIN:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_RAINRATE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_WIND:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_GUST:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_DIRECTION:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_UV:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_WEIGHT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_DISTANCE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_IMPEDANCE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_ARMED:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_TRIPPED:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_WATT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_KWH:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_SCENE_ON:
            case V_SCENE_OFF:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_HVAC_FLOW_STATE:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SPEED:
                return PAYLOAD_TYPE.PL_STRING;
            case V_LIGHT_LEVEL:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VAR1:
            case V_VAR2:
            case V_VAR3:
            case V_VAR4:
            case V_VAR5:
                return PAYLOAD_TYPE.PL_STRING;
            case V_UP:
            case V_DOWN:
            case V_STOP:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_IR_SEND:
            case V_IR_RECEIVE:
                return PAYLOAD_TYPE.PL_HEX;
            case V_FLOW:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VOLUME:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_LOCK_STATUS:
                return PAYLOAD_TYPE.PL_BOOLEAN;
            case V_LEVEL:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_VOLTAGE:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_CURRENT:
                return PAYLOAD_TYPE.PL_DOUBLE;
            case V_RGB:
            case V_RGBW:
                return PAYLOAD_TYPE.PL_HEX;
            case V_ID:
                return PAYLOAD_TYPE.PL_STRING;
            case V_UNIT_PREFIX:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SETPOINT_COOL:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_SETPOINT_HEAT:
                return PAYLOAD_TYPE.PL_STRING;
            case V_HVAC_FLOW_MODE:
                return PAYLOAD_TYPE.PL_STRING;
            default:
                //Make default to string
                return PAYLOAD_TYPE.PL_STRING;
        }
    }

    //HVAC Options flow state
    public static final HashMap<String, String> HVAC_OPTIONS_FLOW_STATE;
    static {
        HVAC_OPTIONS_FLOW_STATE = new HashMap<String, String>();
        HVAC_OPTIONS_FLOW_STATE.put("AutoChangeOver", "Auto Change Over");
        HVAC_OPTIONS_FLOW_STATE.put("HeatOn", "Heat On");
        HVAC_OPTIONS_FLOW_STATE.put("CoolOn", "Cool On");
        HVAC_OPTIONS_FLOW_STATE.put("Off", "Off");
    }

    //HVAC Options flow state
    public static final HashMap<String, String> HVAC_OPTIONS_FLOW_MODE;
    static {
        HVAC_OPTIONS_FLOW_MODE = new HashMap<String, String>();
        HVAC_OPTIONS_FLOW_MODE.put("Auto", "Auto");
        HVAC_OPTIONS_FLOW_MODE.put("ContinuousOn", "Continuous On");
        HVAC_OPTIONS_FLOW_MODE.put("PeriodicOn", "Periodic On");
    }

    //HVAC heater options - HVAC fan speed
    public static final HashMap<String, String> HVAC_OPTIONS_FAN_SPEED;
    static {
        HVAC_OPTIONS_FAN_SPEED = new HashMap<String, String>();
        HVAC_OPTIONS_FAN_SPEED.put("Min", "Minimum");
        HVAC_OPTIONS_FAN_SPEED.put("Normal", "Normal");
        HVAC_OPTIONS_FAN_SPEED.put("Max", "Maximum");
        HVAC_OPTIONS_FAN_SPEED.put("Auto", "Auto");
    }

    public static String getMetricType() {
        if (McObjectManager.getAppProperties().getControllerSettings().getUnitConfig() != null) {
            return McObjectManager.getAppProperties().getControllerSettings().getUnitConfig();
        }
        return UNIT_CONFIG.METRIC.getText();
    }

    //Sensor provider bridge
    private static IProviderBridge mySensorsBridge = new MySensorsProviderBridge();

    public static synchronized void sendToGateway(RawMessage rawMessage) {
        //Send message to nodes [going out from MyController]
        try {
            if (McObjectManager.getGateway(rawMessage.getGatewayId()) != null) {
                McObjectManager.getGateway(rawMessage.getGatewayId()).write(rawMessage);
                if (_logger.isDebugEnabled()) {
                    _logger.debug("Message sent to gateway, {}", rawMessage);
                }
            } else {
                _logger.error("Message sending failed, Selected gateway not available! {}, {}",
                        rawMessage, GatewayUtils.getGateway(rawMessage.getGatewayId()));
            }
        } catch (Exception ex) {
            _logger.error("Message sending failed! {}", rawMessage, ex);
        }
    }

    public static synchronized void sendToProviderBridge(RawMessage rawMessage) {
        switch (rawMessage.getNetworkType()) {
            case MY_SENSORS:
                mySensorsBridge.executeRawMessage(rawMessage);
                break;
            default:
                _logger.warn("Unknown provider: {}", rawMessage.getNetworkType());
                break;
        }

    }

    public static synchronized void sendToProviderBridge(McMessage mcMessage) {
        if (mcMessage.getNetworkType() == null) {
            mcMessage.setNetworkType(GatewayUtils.getNetworkType(mcMessage.getGatewayId()));
        }
        if (mcMessage.isTxMessage() && !mcMessage.isScreeningDone()) {
            sendToMcMessageEngine(mcMessage);
        }
        switch (mcMessage.getNetworkType()) {
            case MY_SENSORS:
                mySensorsBridge.executeMcMessage(mcMessage);
                break;
            default:
                _logger.warn("Unknown provider: {}", mcMessage.getNetworkType());
                break;
        }

    }

    public static synchronized void sendToMcMessageEngine(McMessage mcMessage) {
        new Thread(new McMessageEngine(mcMessage)).start();
    }

    public static synchronized boolean validateNodeIdByProvider(Node node) {
        NETWORK_TYPE networkType = GatewayUtils.getNetworkType(node.getGatewayTable().getId());
        switch (networkType) {
            case MY_SENSORS:
                return mySensorsBridge.validateNodeId(node);
            default:
                _logger.warn("Unknown provider: {}", networkType);
                return false;
        }
    }

    public static synchronized boolean validateSensorIdByProvider(Sensor sensor) {
        NETWORK_TYPE networkType = GatewayUtils.getNetworkType(sensor);
        switch (networkType) {
            case MY_SENSORS:
                return mySensorsBridge.validateSensorId(sensor);
            default:
                _logger.warn("Unknown provider: {}", networkType);
                return false;
        }
    }

}
