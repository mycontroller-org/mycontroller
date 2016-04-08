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
package org.mycontroller.standalone.api.jaxrs;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.HashMap;
import java.util.List;

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

import org.mycontroller.standalone.AppProperties.RESOURCE_TYPE;
import org.mycontroller.standalone.AppProperties.STATE;
import org.mycontroller.standalone.api.ResourcesGroupApi;
import org.mycontroller.standalone.api.jaxrs.json.Query;
import org.mycontroller.standalone.api.jaxrs.json.QueryResponse;
import org.mycontroller.standalone.api.jaxrs.utils.RestUtils;
import org.mycontroller.standalone.db.tables.ResourcesGroup;
import org.mycontroller.standalone.db.tables.ResourcesGroupMap;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */

@Path("/rest/resources/group")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RolesAllowed({ "admin" })
public class ResourcesGroupHandler {

    private ResourcesGroupApi resourcesGroupApi = new ResourcesGroupApi();

    @PUT
    @Path("/")
    public Response updateResourcesGroup(ResourcesGroup resourcesGroup) {
        resourcesGroupApi.updateResourcesGroup(resourcesGroup);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/")
    public Response addResourcesGroup(ResourcesGroup resourcesGroup) {
        resourcesGroupApi.addResourcesGroup(resourcesGroup);
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/{groupId}")
    public Response getResourcesGroup(@PathParam("groupId") Integer groupId) {
        return RestUtils.getResponse(Status.OK, resourcesGroupApi.getResourcesGroup(groupId));
    }

    @GET
    @Path("/")
    public Response getAllResourcesGroups(
            @QueryParam(ResourcesGroup.KEY_NAME) List<String> name,
            @QueryParam(ResourcesGroup.KEY_DESCRIPTION) List<String> description,
            @QueryParam(ResourcesGroup.KEY_STATE) String state,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(ResourcesGroup.KEY_NAME, name);
        filters.put(ResourcesGroup.KEY_DESCRIPTION, description);
        filters.put(ResourcesGroup.KEY_STATE, STATE.fromString(state));

        QueryResponse queryResponse = resourcesGroupApi.getAllResourcesGroups(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : ResourcesGroup.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1L)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/delete")
    public Response deleteResourcesGroup(List<Integer> ids) {
        resourcesGroupApi.deleteResourcesGroup(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/on")
    public Response onResourcesGroup(List<Integer> ids) {
        resourcesGroupApi.turnOn(ids);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/off")
    public Response offResourcesGroup(List<Integer> ids) {
        resourcesGroupApi.turnOff(ids);
        return RestUtils.getResponse(Status.OK);
    }

    //Mapping

    @PUT
    @Path("/map/")
    public Response updateResourcesGroupMap(ResourcesGroupMap resourcesGroupMap) {
        resourcesGroupApi.updateResourcesGroupMap(resourcesGroupMap);
        return RestUtils.getResponse(Status.OK);
    }

    @POST
    @Path("/map/")
    public Response addResourcesGroupMap(ResourcesGroupMap resourcesGroupMap) {
        resourcesGroupApi.addResourcesGroupMap(resourcesGroupMap);
        return RestUtils.getResponse(Status.OK);
    }

    @GET
    @Path("/map/{id}")
    public Response getResourcesGroupMap(@PathParam("id") Integer id) {
        return RestUtils.getResponse(Status.OK, resourcesGroupApi.getResourcesGroupMap(id));
    }

    @GET
    @Path("/map")
    public Response getAllResourcesGroupsMap(
            @QueryParam(ResourcesGroupMap.KEY_GROUP_ID) Integer groupId,
            @QueryParam(ResourcesGroupMap.KEY_PAYLOAD_ON) List<String> payloadOn,
            @QueryParam(ResourcesGroupMap.KEY_PAYLOAD_OFF) List<String> payloadOff,
            @QueryParam(ResourcesGroupMap.KEY_RESOURCE_TYPE) String resourceType,
            @QueryParam(Query.PAGE_LIMIT) Long pageLimit,
            @QueryParam(Query.PAGE) Long page,
            @QueryParam(Query.ORDER_BY) String orderBy,
            @QueryParam(Query.ORDER) String order) {
        HashMap<String, Object> filters = new HashMap<String, Object>();

        filters.put(ResourcesGroupMap.KEY_GROUP_ID, groupId);
        filters.put(ResourcesGroupMap.KEY_PAYLOAD_ON, payloadOn);
        filters.put(ResourcesGroupMap.KEY_PAYLOAD_OFF, payloadOff);
        filters.put(ResourcesGroupMap.KEY_RESOURCE_TYPE, RESOURCE_TYPE.fromString(resourceType));

        QueryResponse queryResponse = resourcesGroupApi.getAllResourcesGroupsMap(
                Query.builder()
                        .order(order != null ? order : Query.ORDER_ASC)
                        .orderBy(orderBy != null ? orderBy : ResourcesGroupMap.KEY_ID)
                        .filters(filters)
                        .pageLimit(pageLimit != null ? pageLimit : Query.MAX_ITEMS_PER_PAGE)
                        .page(page != null ? page : 1L)
                        .build());
        return RestUtils.getResponse(Status.OK, queryResponse);
    }

    @POST
    @Path("/map/delete")
    public Response deleteResourcesGroupMap(List<Integer> ids) {
        resourcesGroupApi.deleteResourcesGroupMap(ids);
        return RestUtils.getResponse(Status.OK);
    }

}
