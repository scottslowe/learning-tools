package org.aws.samples.compute.greeting;

import com.amazonaws.xray.AWSXRay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Arun Gupta
 */
@Path("greeting")
public class GreetingEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(GreetingEndpoint.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() {
        logger.info("get");
        if (AWSXRay.getGlobalRecorder().getTraceEntity() != null)
            AWSXRay.getCurrentSegment().putAnnotation("parentId",
                    AWSXRay.getGlobalRecorder().getTraceEntity().getId());
        String response = "Hello";

        return response;
    }
}
