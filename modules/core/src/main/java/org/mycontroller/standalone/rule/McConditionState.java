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
package org.mycontroller.standalone.rule;

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.model.RuleDefinitionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McConditionState extends McRuleBase {
    private static final Logger _logger = LoggerFactory.getLogger(McConditionState.class);
    public RuleDefinitionState rdState; //data to operate on

    public McConditionState(RuleDefinitionTable ruleDefinitionTable) {
        rdState = new RuleDefinitionState();
        rdState.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdState);
    }

    public McConditionState(RuleDefinitionState rdState) {
        this.rdState = rdState;
        setRuleDefinitionBase(this.rdState);
    }

    @Override
    public boolean evaluate() {
        boolean triggerOperation = false;
        STATE actualState = null;

        //Update current value
        try {
            actualValue = super.getResourceValue(rdState.getResourceType(), rdState.getResourceId());
            if (rdState.getResourceType() == RESOURCE_TYPE.SENSOR_VARIABLE) {
                actualState = McUtils.getInteger(actualValue) > 0 ? STATE.ON : STATE.OFF;
            } else {
                actualState = STATE.fromString(actualValue);
            }
        } catch (IllegalAccessException ex) {
            _logger.error("Failed to get actual value", ex);
            return false;
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Actual value:{}", actualValue);
        }

        switch (rdState.getOperator()) {
            case EQ:
                if (rdState.getState() == actualState) {
                    triggerOperation = true;
                }
                break;
            case NEQ:
                if (rdState.getState() != actualState) {
                    triggerOperation = true;
                }
                break;
            default:
                _logger.warn("Operater[{}] not supported!", rdState.getOperator());
                return false;
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Rule evaluate result:{}", triggerOperation);
        }
        return executeDampening(triggerOperation);
    }
}
