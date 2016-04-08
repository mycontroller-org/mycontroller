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
package org.mycontroller.standalone.operation.model;

import java.util.HashMap;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.McObjectManager;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.ResourceOperation;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.group.ResourcesGroupUtils;
import org.mycontroller.standalone.model.ResourceModel;
import org.mycontroller.standalone.operation.OperationUtils;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.mycontroller.standalone.scheduler.SchedulerUtils;
import org.mycontroller.standalone.timer.TimerSimple;
import org.mycontroller.standalone.timer.TimerUtils;
import org.mycontroller.standalone.timer.jobs.TimerJob;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
public class OperationSendPayload extends Operation {

    public static final String KEY_RESOURCE_TYPE = "resourceType";
    public static final String KEY_RESOURCE_ID = "resourceId";
    public static final String KEY_PAYLOAD = "payload";
    public static final String KEY_DELAY_TIME = "delayTime";

    private RESOURCE_TYPE resourceType;
    private Integer resourceId;
    private String payload;
    private Long delayTime;

    public OperationSendPayload() {

    }

    public OperationSendPayload(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        resourceType = RESOURCE_TYPE.fromString((String) operationTable.getProperties().get(KEY_RESOURCE_TYPE));
        resourceId = (Integer) operationTable.getProperties().get(KEY_RESOURCE_ID);
        payload = (String) operationTable.getProperties().get(KEY_PAYLOAD);
        delayTime = (Long) operationTable.getProperties().get(KEY_DELAY_TIME);
        if (delayTime == null) {
            delayTime = 0L;
        }

    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_RESOURCE_TYPE, resourceType.getText());
        properties.put(KEY_RESOURCE_ID, resourceId);
        properties.put(KEY_PAYLOAD, payload);
        properties.put(KEY_DELAY_TIME, delayTime);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(new ResourceModel(resourceType, resourceId).getResourceLessDetails());
        stringBuilder.append(", Payload:").append(payload).append(", Delay time:")
                .append(McUtils.getFriendlyTime(delayTime, true, "No delay")).append(" ]");
        return stringBuilder.toString();
    }

    //These methods are used for JSON
    @JsonGetter("resourceType")
    private String getResourceTypeString() {
        return resourceType.getText();
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        sendPayload();
    }

    @Override
    public void execute(Timer timer) {
        sendPayload();
    }

    private void sendPayload() {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        if (getDelayTime() == 0) { //Send payload immediately
            sendPayload(getResourceType(), getResourceId(), getPayload());
        } else {  //Create timer to send payload
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(TimerJob.KEY_RESOURCE_TYPE, getResourceType());
            properties.put(TimerJob.KEY_RESOURCE_ID, getResourceId());
            properties.put(TimerJob.KEY_PAYLOAD, getPayload());
            TimerSimple timerSimple = new TimerSimple(
                    OperationUtils.getSendPayloadTimerJobName(getOperationTable()),//Job Name
                    this.getDelayTime(),
                    1//Repeat count
            );
            //Adding a job to send payload with specified delay
            SchedulerUtils.loadTimerJob(timerSimple.getTimer(), properties);
        }
        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());

    }

    public void sendPayload(RESOURCE_TYPE resourceType, Integer resourceId, String payload) {
        ResourceModel resourceModel = new ResourceModel(resourceType, resourceId);
        ResourceOperation resourceOperation = new ResourceOperation(payload);
        //we have to handle gateway,alarm,resource groups and timer operations
        switch (resourceModel.getResourceType()) {
            case GATEWAY:
                GatewayUtils.executeGatewayOperation(resourceModel, resourceOperation);
                break;
            case RULE_DEFINITION:
                RuleUtils.executeRuleDefinitionOperation(resourceModel, resourceOperation);
                break;
            case TIMER:
                TimerUtils.executeTimerOperation(resourceModel, resourceOperation);
            case RESOURCES_GROUP:
                ResourcesGroupUtils.executeResourceGroupsOperation(resourceModel, resourceOperation);
                break;
            default:
                McObjectManager.getMcActionEngine().executeSendPayload(resourceModel, resourceOperation);
                break;
        }
    }
}
