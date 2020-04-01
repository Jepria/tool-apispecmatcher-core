package com.technology.jep.jepriashowcase.featureprocess.rest;

import com.technology.jep.jepriashowcase.featureprocess.FeatureProcessFieldNames;
import com.technology.jep.jepriashowcase.featureprocess.FeatureProcessServerFactory;
import com.technology.jep.jepriashowcase.featureprocess.dto.FeatureProcessCreateDto;
import com.technology.jep.jepriashowcase.featureprocess.dto.FeatureProcessCreateDtoInternal;
import com.technology.jep.jepriashowcase.featureprocess.dto.FeatureProcessDto;
import com.technology.jep.jepriashowcase.featureprocess.dto.FeatureProcessSearchDtoInternal;
import org.jepria.server.service.rest.JaxrsAdapterBase;
import org.jepria.server.service.security.HttpBasic;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/feature/{featureId}/feature-process")
@HttpBasic(passwordType = HttpBasic.PASSWORD)
@RolesAllowed({"JrsEditFeature", "JrsEditAllFeature"})
public class FeatureProcessJaxrsAdapter extends JaxrsAdapterBase {

  protected final EntityEndpointAdapter entityEndpointAdapter = new EntityEndpointAdapter(() -> FeatureProcessServerFactory.getInstance().getEntityService());

  //------------ application-specific methods ------------//

  @GET
  public Response findFeatureProcess(@PathParam("featureId") Integer featureId) {
    // convert path params to internal dto by adding foreign key
    FeatureProcessSearchDtoInternal dto = new FeatureProcessSearchDtoInternal();
    dto.setFeatureId(featureId);
    List<FeatureProcessDto> result = FeatureProcessServerFactory.getInstance().getService().findFeatureProcess(dto, securityContext.getCredential());
    return Response.ok(result).build();
  }

  //------------ entity methods ------------//

  @GET
  @Path("/{recordId}")
  public Response getRecordById(@PathParam("featureId") Integer featureId, @PathParam("recordId") String recordId) {
    // convert path params to a complex key
    String complexKey = FeatureProcessFieldNames.FEATURE_ID + "=" + featureId + "~" + FeatureProcessFieldNames.FEATURE_PROCESS_ID + "=" + recordId;
    FeatureProcessDto result = (FeatureProcessDto) entityEndpointAdapter.getRecordById(complexKey);
    return Response.ok(result).build();
  }

  @POST
  public Response create(@PathParam("featureId") Integer featureId, FeatureProcessCreateDto record) {
    // convert external dto to internal by adding foreign key
    FeatureProcessCreateDtoInternal dto = new FeatureProcessCreateDtoInternal();
    dto.setFeatureId(featureId);
    dto.setFeatureStatusCode(record.getFeatureStatusCode());
    return entityEndpointAdapter.create(dto);
  }

  @DELETE
  @Path("/{recordId}")
  public Response deleteRecordById(@PathParam("featureId") Integer featureId, @PathParam("recordId") String recordId) {
    String complexKey = FeatureProcessFieldNames.FEATURE_ID + "=" + featureId + "~" + FeatureProcessFieldNames.FEATURE_PROCESS_ID + "=" + recordId;
    entityEndpointAdapter.deleteRecordById(complexKey);
    return Response.ok().build();
  }
}
