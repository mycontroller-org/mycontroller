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
package org.mycontroller.standalone.db.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DbException;
import org.mycontroller.standalone.db.tables.Node;
import org.mycontroller.standalone.db.tables.Sensor;
import org.mycontroller.standalone.message.McMessageUtils.MESSAGE_TYPE_PRESENTATION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.1
 */
public class SensorDaoImpl extends BaseAbstractDaoImpl<Sensor, Integer> implements SensorDao {
    private static final Logger _logger = LoggerFactory.getLogger(SensorDaoImpl.class);

    public SensorDaoImpl(ConnectionSource connectionSource) throws SQLException {
        super(connectionSource, Sensor.class);
    }

    @Override
    public void create(Integer gatewayId, String nodeEui, Sensor sensor) {
        create(DaoUtils.getNodeDao().get(gatewayId, nodeEui).getId(), sensor);
    }

    @Override
    public void create(Integer nodeId, Sensor sensor) {
        sensor.setNode(Node.builder().id(nodeId).build());
        this.create(sensor);
    }

    @Override
    public void createOrUpdate(Integer gatewayId, String nodeEui, Sensor sensor) {
        sensor.setNode(DaoUtils.getNodeDao().get(gatewayId, nodeEui));
        this.createOrUpdate(sensor);
    }

    @Override
    public void createOrUpdate(Integer nodeId, Sensor sensor) {
        sensor.setNode(Node.builder().id(nodeId).build());
        this.createOrUpdate(sensor);
    }

    @Override
    public void create(Integer gatewayId, String nodeEui, String sensorId) {
        this.create(DaoUtils.getNodeDao().get(gatewayId, nodeEui).getId(), Sensor.builder().sensorId(sensorId).build());

    }

    @Override
    public void delete(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            DeleteBuilder<Sensor, Integer> deleteBuilder = this.getDao().deleteBuilder();
            deleteBuilder.where().eq(Sensor.KEY_NODE_ID, sensor.getNode().getId())
                    .and().eq(Sensor.KEY_SENSOR_ID, sensor.getSensorId());
            int deleteCount = deleteBuilder.delete();
            _logger.debug("Deleted senosor:[{}], delete count:{}", sensor, deleteCount);
        } catch (SQLException ex) {
            _logger.error("unable to delete, sensor:{}", sensor, ex);
        } catch (DbException dbEx) {
            _logger.error("unable to delete, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void delete(Integer gatewayId, String nodeEui, String sensorId) {
        Sensor sensor = Sensor.builder().sensorId(sensorId).build();
        sensor.setNode(DaoUtils.getNodeDao().get(gatewayId, nodeEui));
        this.delete(sensor);
    }

    @Override
    public void update(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            UpdateBuilder<Sensor, Integer> updateBuilder = this.getDao().updateBuilder();

            if (sensor.getType() != null) {
                updateBuilder.updateColumnValue(Sensor.KEY_TYPE, sensor.getType());
            }
            if (sensor.getName() != null) {
                updateBuilder.updateColumnValue(Sensor.KEY_NAME, sensor.getName());
            }
            if (sensor.getLastSeen() != null) {
                updateBuilder.updateColumnValue(Sensor.KEY_LAST_SEEN, sensor.getLastSeen());
            }
            if (sensor.getSensorId() != null) {
                updateBuilder.updateColumnValue(Sensor.KEY_SENSOR_ID, sensor.getSensorId());
            }

            if (sensor.getRoom() != null && sensor.getRoom().getId() == null) {
                updateBuilder.updateColumnValue(Sensor.KEY_ROOM_ID, null);
            } else {
                updateBuilder.updateColumnValue(Sensor.KEY_ROOM_ID, sensor.getRoom());
            }

            updateBuilder.where().eq(Sensor.KEY_ID, sensor.getId());
            int updateCount = updateBuilder.update();
            _logger.debug("Updated senosor:[{}], update count:{}", sensor, updateCount);
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to update, sensor:{}", sensor, dbEx);
        }
    }

    @Override
    public void update(Integer nodeId, Sensor sensor) {
        sensor.setNode(Node.builder().id(nodeId).build());
        update(sensor);
    }

    @Override
    public void update(Integer gatewayId, String nodeEui, Sensor sensor) {
        sensor.setNode(DaoUtils.getNodeDao().get(gatewayId, nodeEui));
        this.update(sensor);
    }

    @Override
    public List<Sensor> getAllByNodeId(Integer nodeId) {
        try {
            if (nodeId == null) {
                return null;
            }
            return this.getDao().queryForEq(Sensor.KEY_NODE_ID, nodeId);
        } catch (SQLException ex) {
            _logger.error("unable to get all list with node id:{}", nodeId, ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getAll(String nodeEui, Integer gatewayId) {
        return getAllByNodeId(DaoUtils.getNodeDao().get(gatewayId, nodeEui).getId());
    }

    @Override
    public List<Sensor> getByType(String typeString) {
        try {
            return this.getDao()
                    .queryForEq("type", MESSAGE_TYPE_PRESENTATION.valueOf(typeString));
        } catch (SQLException ex) {
            _logger.error("unable to get all list with typeString: {}", typeString, ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getAll() {
        try {
            return this.getDao().queryForAll();
        } catch (SQLException ex) {
            _logger.error("unable to get all list", ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getAllByNodeIds(List<Integer> nodeIds) {
        try {
            if (nodeIds == null) {
                return null;
            }
            QueryBuilder<Sensor, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().in(Sensor.KEY_NODE_ID, nodeIds);
            return queryBuilder.query();
        } catch (SQLException ex) {
            _logger.error("unable to get all list with nodeIds:{}", nodeIds, ex);
            return null;
        }
    }

    @Override
    public Sensor get(Integer nodeId, String sensorId) {
        try {
            nodeIdSensorIdnullCheck(nodeId, sensorId);
            return this.getDao().queryForFirst(
                    this.getDao().queryBuilder()
                            .where().eq(Sensor.KEY_NODE_ID, nodeId)
                            .and().eq(Sensor.KEY_SENSOR_ID, sensorId).prepare());
        } catch (SQLException ex) {
            _logger.error("unable to get", ex);
        } catch (DbException dbEx) {
            _logger.error("unable to get, nodeId:{},sensorId:{}", nodeId, sensorId, dbEx);
        }
        return null;
    }

    @Override
    public Sensor get(Integer gatewayId, String nodeEui, String sensorId) {
        Node node = DaoUtils.getNodeDao().get(gatewayId, nodeEui);
        if (node != null) {
            return this.get(node.getId(), sensorId);
        } else {
            return null;
        }
    }

    @Override
    public Sensor get(Sensor sensor) {
        try {
            this.nodeIdSensorIdnullCheck(sensor);
            return this.get(sensor.getNode().getId(), sensor.getSensorId());
        } catch (DbException ex) {
            _logger.error("unable to get", ex);
            return null;
        }
    }

    private void nodeIdSensorIdnullCheck(Integer nodeId, String sensorId) throws DbException {
        if (nodeId != null && sensorId != null) {
            return;
        } else {
            throw new DbException("SensorId or nodeId should not be a NULL, nodeId:" + nodeId + ",SensorId:"
                    + sensorId);
        }
    }

    private void nodeIdSensorIdnullCheck(Sensor sensor) throws DbException {
        if (sensor != null && sensor.getSensorId() != null && sensor.getNode() != null
                && sensor.getNode().getId() != null) {
            return;
        } else {
            throw new DbException("SensorId or NodeId should not be a NULL, Sensor:" + sensor);
        }
    }

    @Override
    public List<Integer> getSensorIds(String nodeEui, Integer gatewayId) {
        List<Sensor> sensors = this.getAll(nodeEui, gatewayId);
        List<Integer> ids = new ArrayList<Integer>();
        //TODO: should modify by query (RAW query)
        for (Sensor sensor : sensors) {
            ids.add(sensor.getId());
        }
        return ids;
    }

    @Override
    public long countOf(Integer nodeId) {
        try {
            QueryBuilder<Sensor, Integer> queryBuilder = this.getDao().queryBuilder();
            queryBuilder.where().eq(Sensor.KEY_NODE_ID, nodeId);
            return queryBuilder.countOf();
        } catch (SQLException ex) {
            _logger.error("unable to get Sensor count:[NodeId:{}]", nodeId, ex);
        }
        return 0;
    }

    @Override
    public List<Sensor> getAllByIds(List<Integer> ids) {
        try {
            if (ids != null && !ids.isEmpty()) {
                QueryBuilder<Sensor, Integer> queryBuilder = this.getDao().queryBuilder();
                queryBuilder.where().in(Sensor.KEY_ID, ids);
                return queryBuilder.query();
            }
        } catch (SQLException ex) {
            _logger.error("unable to get all list with sensor Ids:{}", ids, ex);
        }
        return null;
    }

    @Override
    public QueryResponse getAll(Query query) {
        try {
            return super.getQueryResponse(query, Sensor.KEY_ID);
        } catch (SQLException ex) {
            _logger.error("unable to run query:[{}]", query, ex);
            return null;
        }
    }

    @Override
    public List<Sensor> getAll(List<Integer> ids) {
        return getAll(Sensor.KEY_ID, ids);
    }

    @Override
    public List<Integer> getSensorIdsByNodeIds(List<Integer> ids) {
        List<Sensor> sensors = super.getAll(Sensor.KEY_NODE_ID, ids);
        List<Integer> sensorIds = new ArrayList<Integer>();
        for (Sensor sensor : sensors) {
            sensorIds.add(sensor.getId());
        }
        return sensorIds;
    }

    @Override
    public List<Sensor> getAllByRoomId(Integer roomId) {
        try {
            if (roomId == null) {
                return null;
            }
            return this.getDao().queryForEq(Sensor.KEY_ROOM_ID, roomId);
        } catch (SQLException ex) {
            _logger.error("unable to get all list with room id:{}", roomId, ex);
            return null;
        }
    }
}