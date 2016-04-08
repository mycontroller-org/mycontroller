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

import java.util.ArrayList;
import java.util.List;

import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.tables.RuleDefinitionTable;
import org.mycontroller.standalone.rule.RuleUtils;
import org.mycontroller.standalone.rule.model.RuleDefinition;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.3
 */

public class RuleApi {

    public RuleDefinitionTable getRaw(int id) {
        return DaoUtils.getRuleDefinitionDao().getById(id);
    }

    public QueryResponse getAllRaw(Query query) {
        return DaoUtils.getRuleDefinitionDao().getAll(query);
    }

    public RuleDefinition get(int id) {
        return RuleUtils.getRuleDefinition(getRaw(id));
    }

    public QueryResponse getAll(Query query) {
        QueryResponse queryResponse = getAllRaw(query);
        ArrayList<RuleDefinition> gateways = new ArrayList<RuleDefinition>();
        @SuppressWarnings("unchecked")
        List<RuleDefinitionTable> rows = (List<RuleDefinitionTable>) queryResponse.getData();
        for (RuleDefinitionTable row : rows) {
            gateways.add(RuleUtils.getRuleDefinition(row));
        }
        queryResponse.setData(gateways);
        return queryResponse;
    }

    public void add(RuleDefinition ruleDefinition) {
        ruleDefinition.reset();
        RuleUtils.addRuleDefinition(ruleDefinition);
    }

    public void update(RuleDefinition ruleDefinition) {
        RuleUtils.updateRuleDefinition(ruleDefinition);
    }

    public void deleteIds(List<Integer> ids) {
        RuleUtils.deleteRuleDefinitionIds(ids);
    }

    public void enableIds(List<Integer> ids) {
        RuleUtils.enableRuleDefinitions(ids);
    }

    public void disableIds(List<Integer> ids) {
        RuleUtils.disableRuleDefinitions(ids);
    }

}
