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
package org.mycontroller.standalone.scripts.api;

import org.mycontroller.standalone.api.BackupApi;
import org.mycontroller.standalone.api.GatewayApi;
import org.mycontroller.standalone.api.MetricApi;
import org.mycontroller.standalone.api.NodeApi;
import org.mycontroller.standalone.api.OperationApi;
import org.mycontroller.standalone.api.RuleApi;
import org.mycontroller.standalone.api.SensorApi;
import org.mycontroller.standalone.api.TimerApi;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class ActionApi {
    private GatewayApi gatewayApi = new GatewayApi();
    private NodeApi nodeApi = new NodeApi();
    private SensorApi sensorApi = new SensorApi();
    private TimerApi timerApi = new TimerApi();
    private MetricApi metricApi = new MetricApi();
    private BackupApi backupApi = new BackupApi();
    private OperationApi operationApi = new OperationApi();
    private RuleApi ruleApi = new RuleApi();

    public SensorApi sensor() {
        return sensorApi;
    }

    public GatewayApi gateway() {
        return gatewayApi;
    }

    public NodeApi node() {
        return nodeApi;
    }

    public TimerApi timer() {
        return timerApi;
    }

    public MetricApi metric() {
        return metricApi;
    }

    public BackupApi backup() {
        return backupApi;
    }

    public OperationApi operation() {
        return operationApi;
    }

    public RuleApi rule() {
        return ruleApi;
    }

}
