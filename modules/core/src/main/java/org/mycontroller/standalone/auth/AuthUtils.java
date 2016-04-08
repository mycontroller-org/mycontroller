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
package org.mycontroller.standalone.auth;

import javax.ws.rs.core.SecurityContext;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
public class AuthUtils {
    public static final Logger _logger = LoggerFactory.getLogger(AuthUtils.class);

    private AuthUtils() {

    }

    public enum PERMISSION_TYPE {
        SUPER_ADMIN("Super admin"),
        USER("User"),
        MQTT_USER("MQTT user");
        private final String name;

        private PERMISSION_TYPE(String name) {
            this.name = name;
        }

        public String getText() {
            return this.name;
        }

        public static PERMISSION_TYPE get(int id) {
            for (PERMISSION_TYPE type : values()) {
                if (type.ordinal() == id) {
                    return type;
                }
            }
            throw new IllegalArgumentException(String.valueOf(id));
        }

        public static PERMISSION_TYPE fromString(String text) {
            if (text != null) {
                for (PERMISSION_TYPE type : PERMISSION_TYPE.values()) {
                    if (text.equalsIgnoreCase(type.getText())) {
                        return type;
                    }
                }
            }
            return null;
        }
    }

    public static User getUser(SecurityContext securityContext) {
        return (User) securityContext.getUserPrincipal();
    }

    public static boolean isSuperAdmin(SecurityContext securityContext) {
        return isSuperAdmin(AuthUtils.getUser(securityContext));
    }

    public static boolean isSuperAdmin(User user) {
        _logger.debug("User:{}", user);
        return user.getPermissions().contains(PERMISSION_TYPE.SUPER_ADMIN.getText());
    }

    public static boolean hasAccess(SecurityContext securityContext, RESOURCE_TYPE resourceType, Integer resourceId) {
        return hasAccess(getUser(securityContext), resourceType, resourceId);
    }

    public static boolean hasAccess(User user, RESOURCE_TYPE resourceType, Integer resourceId) {
        switch (resourceType) {
            case GATEWAY:
                return user.getAllowedResources().getGatewayIds().contains(resourceId);
            case NODE:
                return user.getAllowedResources().getNodeIds().contains(resourceId);
            case SENSOR:
                return user.getAllowedResources().getSensorIds().contains(resourceId);
            case SENSOR_VARIABLE:
                return user.getAllowedResources().getSensorVariableIds().contains(resourceId);
            default:
                return false;
        }
    }

    public static boolean hasPermission(User user, PERMISSION_TYPE permissionType) {
        if (user.getPermission().equalsIgnoreCase(permissionType.getText())) {
            return true;
        }
        return false;
    }

    public static boolean authenticateMqttUser(String aUsername, String aPassword) {
        if (_logger.isDebugEnabled()) {
            _logger.debug("MQTT authentication: User:{}", aUsername);
        }
        User user = DaoUtils.getUserDao().getByUsername(aUsername);
        if (user != null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("User Found...User:{}", user);
            }
            if (user.getPassword().equals(aPassword)) {
                user.setPassword(null);
                if (isSuperAdmin(user) || hasPermission(user, PERMISSION_TYPE.MQTT_USER)) {
                    return true;
                }
                _logger.warn("User[{}] does not have MQTT access permission!", user.getUsername());
                return false;
            }
            if (_logger.isDebugEnabled()) {
                _logger.debug("Invalid password for the user: {}", user.getUsername());
            }
            return false;
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("user[{}] not found!", aUsername);
        }
        return false;
    }
}
