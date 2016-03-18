/**
 * Copyright (C) 2015-2016 Jeeva Kandasamy (jkandasa@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mycontroller.standalone.api.jaxrs;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.ObjectManager;
import org.mycontroller.standalone.AppProperties.NETWORK_TYPE;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiError;
import org.mycontroller.standalone.api.jaxrs.mapper.ApiMessage;
import org.mycontroller.standalone.api.jaxrs.mapper.Query;
import org.mycontroller.standalone.api.jaxrs.mapper.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.auth.AuthUtils;
import org.mycontroller.standalone.db.DaoUtils;
import org.mycontroller.standalone.db.DeleteResourceUtils;
import org.mycontroller.standalone.db.tables.Gateway;
import org.mycontroller.standalone.gateway.GatewayUtils;
import org.mycontroller.standalone.gateway.GatewayUtils.TYPE;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@Path("/rest/gateways")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "User" })
public class GatewayHandler extends AccessEngine {

    @PUT
    @Path("/")
    public Response updateGateway(Gateway gateway) {
        this.hasAccessGateway(gateway.getId());
        GatewayUtils.updateGateway(gateway);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @RolesAllowed({ "admin" })
    @POST
    @Path("/")
    public Response addGateway(Gateway gateway) {
        GatewayUtils.addGateway(gateway);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @GET
    @Path("/{id}")
    public Response getGateway(@PathParam("id") Integer gatewayId) {
        this.hasAccessGateway(gatewayId);
        return RestUtils.getResponse(Status.OK, DaoUtils.getGatewayDao().getById(gatewayId));
    }

    @GET
    @Path("/")
    public Response getAllGateways(
            @QueryParam(Gateway.KEY_NAME) List<String> name,
            @QueryParam(Gateway.KEY_NETWORK_TYPE) String networkType,
            @QueryParam(Gateway.KEY_TYPE) String type,
            @QueryParam(Gateway.KEY_STATE) String state,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(Gateway.KEY_NAME, name);
        filters.put(Gateway.KEY_NETWORK_TYPE, NETWORK_TYPE.fromString(networkType));
        filters.put(Gateway.KEY_TYPE, TYPE.fromString(type));
        filters.put(Gateway.KEY_STATE, STATE.fromString(state));

        //Add id filter if he is non-admin
        if (!AuthUtils.isSuperAdmin(securityContext)) {
            filters.put(Gateway.KEY_ID, AuthUtils.getUser(securityContext).getAllowedResources().getGatewayIds());
        }

        QueryResponse queryResponse = DaoUtils.getGatewayDao().getAll(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : Gateway.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1l)
                        .build());

        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/delete")
    public Response deleteGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        DeleteResourceUtils.deleteGateways(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/enable")
    public Response enableGateway(List<Integer> ids) {
        updateGatewayIds(ids);
        GatewayUtils.enableGateways(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/disable")
    public Response enableGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        GatewayUtils.disableGateways(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/reload")
    public Response reloadGateways(List<Integer> ids) {
        updateGatewayIds(ids);
        GatewayUtils.reloadGateways(ids);
        return RestUtils.getResponse(Status.ACCEPTED);
    }

    @POST
    @Path("/discover")
    public Response executeNodeDiscover(List<Integer> ids) {
        updateGatewayIds(ids);
        try {
            for (Integer id : ids) {
                Gateway gateway = DaoUtils.getGatewayDao().getById(id);
                if (gateway.getEnabled()) {
                    ObjectManager.getIActionEngine(gateway.getNetworkType()).discover(id);
                }
            }
            return RestUtils.getResponse(Status.OK,
                    ApiMessage.builder().message("Node Discover util started successfully").build());

        } catch (Exception ex) {
            return RestUtils.getResponse(Status.BAD_REQUEST, new ApiError(ex.getMessage()));
        }
    }

}
