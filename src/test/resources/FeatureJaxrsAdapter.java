package com.technology.jep.jepriashowcase.feature.rest;

import com.technology.jep.jepriashowcase.feature.FeatureServerFactory;
import com.technology.jep.jepriashowcase.feature.dto.FeatureCreateDto;
import com.technology.jep.jepriashowcase.feature.dto.FeatureDto;
import com.technology.jep.jepriashowcase.feature.dto.FeatureSearchDto;
import com.technology.jep.jepriashowcase.feature.dto.FeatureUpdateDto;
import org.jepria.server.data.OptionDto;
import org.jepria.server.data.SearchRequestDto;
import org.jepria.server.service.rest.ExtendedResponse;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;

import javax.annotation.security.RolesAllowed;
import javax.validation.constraints.Pattern;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/feature")
@HttpBasic(passwordType = HttpBasic.PASSWORD)
@RolesAllowed({"JrsEditFeature", "JrsAssignResponsibleFeature", "JrsEditAllFeature"})
public class FeatureJaxrsAdapter extends JaxrsAdapterBase {

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> FeatureServerFactory.getInstance().getEntityService());

  protected final SearchEndpointAdapter searchEndpointAdapter = new SearchEndpointAdapter(() -> FeatureServerFactory.getInstance().getSearchService(() -> request.getSession()));

  //------------ application-specific methods ------------//

  @POST
  @Path("/{featureId}/set-feature-responsible")
  public Response setFeatureResponsible(@PathParam("featureId") Integer featureId, @QueryParam("responsibleId") Integer responsibleId) {
    FeatureServerFactory.getInstance().getService().setFeatureResponsible(featureId, responsibleId, securityContext.getCredential());
    return Response.ok().build();
  }

  @GET
  @Path("/option/feature-operator")
  public Response getFeatureOperator() {
    List<OptionDto<Integer>> result = FeatureServerFactory.getInstance().getService().getFeatureOperator();
    return Response.ok(result).build();
  }

  @GET
  @Path("/option/feature-status")
  public Response getFeatureStatus() {
    List<OptionDto<String>> result = FeatureServerFactory.getInstance().getService().getFeatureStatus();
    return Response.ok(result).build();
  }

  //------------ entity methods ------------//

  @GET
  @Path("/{recordId}")
  public Response getRecordById(@Pattern(regexp = "\\d+", message = "ID must be an integer") @PathParam("recordId") String recordId) {
    FeatureDto result = (FeatureDto) entityEndpointAdapter.getRecordById(recordId);
    return Response.ok(result).build();
  }

  @POST
  public Response create(FeatureCreateDto record) {
    return entityEndpointAdapter.create(record);
  }

  @DELETE
  @Path("/{recordId}")
  public Response deleteRecordById(@Pattern(regexp = "\\d+", message = "ID must be an integer") @PathParam("recordId") String recordId) {
    entityEndpointAdapter.deleteRecordById(recordId);
    return Response.ok().build();
  }

  @PUT
  @Path("/{recordId}")
  public Response update(@Pattern(regexp = "\\d+", message = "ID must be an integer") @PathParam("recordId") String recordId, FeatureUpdateDto record) {
    entityEndpointAdapter.update(recordId, record);
    return Response.ok().build();
  }

  //------------ search methods ------------//

  @POST
  @Path("/search")
  public Response postSearch(SearchRequestDto<FeatureSearchDto> searchRequestDto,
                             org.jepria.server.data.SearchRequestDto<FeatureSearchDto> searchRequestDto2,
                             SearchRequestDto searchRequestDto3,
                             org.jepria.server.data.SearchRequestDto searchRequestDto4,
                             @Pattern(regexp = "(resultset/paged-by-\\d+/\\d+)|(resultset\\?pageSize\\d+&page=\\d+)|(resultset\\?page=\\d+&pageSize=\\d+)", message = "Bad Extended-Response header value")
                             @HeaderParam(ExtendedResponse.REQUEST_HEADER_NAME) String extendedResponse,
                             @HeaderParam("Cache-Control") String cacheControl) {
    return searchEndpointAdapter.postSearch(searchRequestDto, extendedResponse, cacheControl);
  }

  @GET
  @Path("/search/{searchId}")
  public Response getSearchRequest(
          @PathParam("searchId") String searchId) {
    SearchRequestDto<FeatureSearchDto> result = (SearchRequestDto<FeatureSearchDto>)searchEndpointAdapter.getSearchRequest(searchId);
    return Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset-size")
  public Response getSearchResultsetSize(@PathParam("searchId") String searchId,
                                         @HeaderParam("Cache-Control") String cacheControl) {
    int result = searchEndpointAdapter.getSearchResultsetSize(searchId, cacheControl);
    return Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset")
  public Response getResultset(
          @PathParam("searchId") String searchId,
          @QueryParam("pageSize") Integer pageSize,
          @QueryParam("page") Integer page,
          @HeaderParam("Cache-Control") String cacheControl) {
    List<FeatureDto> result = (List<FeatureDto>)searchEndpointAdapter.getResultset(searchId, pageSize, page, cacheControl);
    return result == null ? Response.noContent().build() : Response.ok(result).build();
  }

  @GET
  @Path("/search/{searchId}/resultset/paged-by-{pageSize:\\d+}/{page}")
  public Response getResultsetPaged(
          @PathParam("searchId") String searchId,
          @PathParam("pageSize") Integer pageSize,
          @PathParam("page") Integer page,
          @HeaderParam("Cache-Control") String cacheControl) {
    List<FeatureDto> result = (List<FeatureDto>)searchEndpointAdapter.getResultsetPaged(searchId, pageSize, page, cacheControl);
    return result == null ? Response.noContent().build() : Response.ok(result).build();
  }
}
