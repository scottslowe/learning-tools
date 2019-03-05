package org.aws.samples.compute.name;

import com.amazonaws.xray.AWSXRay;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Arun Gupta
 */
@Path("names")
public class NameEndpoint {
    @GET
    @Produces({MediaType.APPLICATION_XML + "; qs=0.50",
            MediaType.APPLICATION_JSON + "; qs=0.75"})
    public Name[] get() {
        if (AWSXRay.getGlobalRecorder().getTraceEntity() != null)
            AWSXRay.getCurrentSegment().putAnnotation("parentId",
                AWSXRay.getGlobalRecorder().getTraceEntity().getId());

        return Names.findAll();
    }
    
    @GET
    @Path("{id}")
    @Produces({MediaType.APPLICATION_XML + "; qs=0.50",
            MediaType.APPLICATION_JSON + "; qs=0.75",
            MediaType.TEXT_PLAIN + "; qs=1.0"})
    public Name get(@PathParam("id") int id) {
        if (AWSXRay.getGlobalRecorder().getTraceEntity() != null)
            AWSXRay.getCurrentSegment().putAnnotation("parentId",
                AWSXRay.getGlobalRecorder().getTraceEntity().getId());

        return Names.findById(id);
    }
}
