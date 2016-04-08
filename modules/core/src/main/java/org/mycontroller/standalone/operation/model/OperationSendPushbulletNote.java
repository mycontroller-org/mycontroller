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

import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.OperationTable;
import org.mycontroller.standalone.db.tables.Timer;
import org.mycontroller.standalone.operation.OperationNotification;
import org.mycontroller.standalone.operation.PushbulletUtils;
import org.mycontroller.standalone.rule.model.RuleDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class OperationSendPushbulletNote extends Operation {
    private static final Logger _logger = LoggerFactory.getLogger(OperationSendPushbulletNote.class);

    public static final String KEY_IDENS = "idens";
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";

    private String idens;
    private String title;
    private String body;

    public OperationSendPushbulletNote() {

    }

    public OperationSendPushbulletNote(OperationTable operationTable) {
        this.updateOperation(operationTable);
    }

    @Override
    public void updateOperation(OperationTable operationTable) {
        super.updateOperation(operationTable);
        idens = (String) operationTable.getProperties().get(KEY_IDENS);
        title = (String) operationTable.getProperties().get(KEY_TITLE);
        body = (String) operationTable.getProperties().get(KEY_BODY);

    }

    @Override
    @JsonIgnore
    public OperationTable getOperationTable() {
        OperationTable operationTable = super.getOperationTable();
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put(KEY_IDENS, idens);
        properties.put(KEY_TITLE, title);
        properties.put(KEY_BODY, body);
        operationTable.setProperties(properties);
        return operationTable;
    }

    @Override
    public String getOperationString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.getType().getText()).append(" [ ");
        stringBuilder.append(title).append(" ]");
        return stringBuilder.toString();
    }

    @Override
    public void execute(RuleDefinition ruleDefinition) {
        if (!getEnabled()) {
            //This operation disabled, nothing to do.
            return;
        }
        try {
            OperationNotification operationNotification = new OperationNotification(ruleDefinition);
            if (body != null && body.trim().length() > 0) {
                PushbulletUtils.sendNote(
                        idens,
                        operationNotification.updateReferances(title),
                        operationNotification.updateReferances(body));
            } else {
                PushbulletUtils.sendNote(
                        idens,
                        operationNotification.updateReferances(title),
                        operationNotification.toString());
            }
        } catch (Exception ex) {
            _logger.error("Exception,", ex);
        }

        //Update last execution
        setLastExecution(System.currentTimeMillis());
        DaoUtils.getOperationDao().update(this.getOperationTable());
    }

    @Override
    public void execute(Timer timer) {
        _logger.error("Timer will not support for this operation");
    }

}
