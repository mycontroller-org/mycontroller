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
package org.mycontroller.standalone.api;

import java.util.List;

import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.GatewayTable;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.exceptions.McBadRequestException;
import org.mycontroller.standalone.exceptions.McDuplicateException;
import org.mycontroller.standalone.message.McMessageUtils;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class NodeApi {

    public QueryResponse getAllNodes(Query query) {
        return DaoUtils.getNodeDao().getAll(query);
    }

    public Node get(int id) {
        return DaoUtils.getNodeDao().getById(id);
    }

    public void deleteIds(List<Integer> ids) {
        DeleteResourceUtils.deleteNodes(ids);
    }

    public void update(Node node) throws McDuplicateException, McBadRequestException {
        Node availabilityCheck = DaoUtils.getNodeDao().get(node.getGatewayTable().getId(), node.getEui());
        if (availabilityCheck != null && availabilityCheck.getId() != node.getId()) {
            throw new McDuplicateException("A node available with this EUI.");
        }

        if (McMessageUtils.validateNodeIdByProvider(node)) {
            DaoUtils.getNodeDao().update(node);
        }
    }

    public void add(Node node) throws McDuplicateException {
        if (DaoUtils.getNodeDao().get(node.getGatewayTable().getId(), node.getEui()) != null) {
            throw new McDuplicateException("A node available with this EUI.");
        }
        GatewayTable gatewayTable = DaoUtils.getGatewayDao().getById(node.getGatewayTable().getId());
        node.setGatewayTable(gatewayTable);
        if (McMessageUtils.validateNodeIdByProvider(node)) {
            DaoUtils.getNodeDao().create(node);
        }
    }

    public void reboot(List<Integer> ids) throws McBadRequestException {
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                McObjectManager.getMcActionEngine().rebootNode(node);
            }
        } else {
            throw new McBadRequestException("Selected Node(s) not available!");
        }
    }

    public void uploadFirmware(List<Integer> ids) throws McBadRequestException {
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                if (node.getFirmware() != null) {
                    McObjectManager.getMcActionEngine().uploadFirmware(node);
                }
            }
        } else {
            throw new McBadRequestException("Selected Node(s) not available!");
        }
    }

    public void eraseConfig(List<Integer> ids) throws McBadRequestException {
        List<Node> nodes = DaoUtils.getNodeDao().getAll(ids);
        if (nodes != null && nodes.size() > 0) {
            for (Node node : nodes) {
                McObjectManager.getMcActionEngine().eraseConfiguration(node);
            }

        } else {
            throw new McBadRequestException("Selected Node not available!");
        }
    }

}
