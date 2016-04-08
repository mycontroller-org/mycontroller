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

import org.mycontroller.standalone.McUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.model.RuleDefinitionCompare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */
public class McConditionCompare extends McRuleBase {
    private static final Logger _logger = LoggerFactory.getLogger(McConditionCompare.class);
    public RuleDefinitionCompare rdCompare; //data to operate on

    public McConditionCompare(RuleDefinitionTable ruleDefinitionTable) {
        rdCompare = new RuleDefinitionCompare();
        rdCompare.updateRuleDefinition(ruleDefinitionTable);
        setRuleDefinitionBase(rdCompare);
    }

    public McConditionCompare(RuleDefinitionCompare rdCompare) {
        this.rdCompare = rdCompare;
        setRuleDefinitionBase(this.rdCompare);
    }

    @Override
    public boolean evaluate() {
        boolean triggerOperation = false;
        String date2ValueString = null;
        //Update current value
        try {
            actualValue = super.getResourceValue(rdCompare.getResourceType(), rdCompare.getResourceId());
            date2ValueString = super
                    .getResourceValue(rdCompare.getData2ResourceType(), rdCompare.getData2ResourceId());
        } catch (IllegalAccessException ex) {
            _logger.error("Failed to get actual value", ex);
            return false;
        }
        if (_logger.isDebugEnabled()) {
            _logger.debug("Actual value:{}, data2Value:{}", actualValue, date2ValueString);
        }
        //If either value is NULL cannot execute
        if (actualValue == null || date2ValueString == null) {
            if (_logger.isDebugEnabled()) {
                _logger.debug("compare can not be executed with NULL. actualValue:{}, date2ValueString:{}",
                        actualValue, date2ValueString);
            }
            return false;
        }
        double avDouble = McUtils.getDouble(actualValue);
        double data2Value = McUtils.getDouble(date2ValueString);
        //Multiplier will be in percentage, change it double value
        double data2Multiplier = rdCompare.getData2Multiplier() / 100.0;

        switch (rdCompare.getOperator()) {
            case GT:
                if (avDouble > (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            case GTE:
                if (avDouble >= (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            case LT:
                if (avDouble < (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            case LTE:
                if (avDouble <= (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            case EQ:
                if (avDouble == (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            case NEQ:
                if (avDouble != (data2Multiplier * data2Value)) {
                    triggerOperation = true;
                }
                break;
            default:
                _logger.warn("Operater[{}] not supported!", rdCompare.getOperator());
                return false;
        }

        if (_logger.isDebugEnabled()) {
            _logger.debug("Rule evaluate result:{}", triggerOperation);
        }
        return executeDampening(triggerOperation);
    }
}
