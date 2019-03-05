package org.aws.samples.compute.webapp;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorder;
import com.amazonaws.xray.entities.Namespace;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.entities.TraceHeader;
import com.mashape.unirest.http.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/{id:([^/]+?)?}")
public class WebappController {

    private static final Logger logger = LoggerFactory.getLogger(WebappController.class);

    @Produces(MediaType.TEXT_PLAIN)
    @GET
    public String getMessage(@Context UriInfo uri, @PathParam("id") String id) {
        String greetingEndpoint = getEndpoint("GREETING", uri.getRequestUri().getScheme(), null);
        logger.info("ID Query is: " + id);
        String pathQuery = (id.equals("")) ? "/1" : ("/" + id);
        String nameEndpoint = getEndpoint("NAME", uri.getRequestUri().getScheme(), pathQuery);

        Segment segment = AWSXRay.getCurrentSegment();
        AWSXRayRecorder xrayRecorder = AWSXRay.getGlobalRecorder();
        if (AWSXRay.getGlobalRecorder().getTraceEntity() != null)
            segment.putAnnotation("parentId", xrayRecorder.getTraceEntity().getId());
        Subsegment subsegment = xrayRecorder.beginSubsegment("greeting");
        subsegment.setNamespace(Namespace.REMOTE.toString());

        String greetingMessage = "";
        try {
            greetingMessage = Unirest
                    .get(greetingEndpoint)
                    .header("accept", "text/plain")
                    .header("x-amzn-trace-id", getTraceHeader(segment, subsegment).toString())
                    .asString()
                    .getBody();
            logger.info("Greeting is: " + greetingMessage);
        } catch (Exception e) {
            logger.error("Failed connecting Greeting API: " + e);
        }
        xrayRecorder.endSubsegment();

        subsegment = xrayRecorder.beginSubsegment("name");
        subsegment.setNamespace(Namespace.REMOTE.toString());

        String nameMessage = "";
        try {
            nameMessage = Unirest
                    .get(nameEndpoint)
                    .header("accept", "text/plain")
                    .header("x-amzn-trace-id", getTraceHeader(segment, subsegment).toString())
                    .asString()
                    .getBody();
            logger.info("Name is: " + nameMessage);
        } catch (Exception e) {
            logger.error("Failed connecting Name API: " + e);
        }
        xrayRecorder.endSubsegment();

        String lambdaEndpoint = "https://0pnavsn5uk.beta.execute-api.us-east-1.amazonaws.com/prod";
        try {
            String response = Unirest
                    .get(lambdaEndpoint)
                    .header("accept", "text/plain")
                    .header("x-amzn-trace-id", getTraceHeader(segment, subsegment).toString())
                    .queryString("username", nameMessage)
                    .queryString("message", greetingMessage)
                    .asString()
                    .getBody();
            logger.info("API Gateway: " + response);
        } catch (Exception e) {
            logger.error("Failed connecting Name API: " + e);
        }

        return greetingMessage + " " + nameMessage;
    }

    private String getEndpoint(String type, String scheme, String pathQuery) {
        logger.info("getEndpoint: " + pathQuery);
        String host = System.getenv(type + "_SERVICE_HOST");
        if (null == host) {
            throw new RuntimeException(type + "_SERVICE_HOST environment variable not found");
        }

        String port = System.getenv(type + "_SERVICE_PORT");
        if (null == port) {
            throw new RuntimeException(type + "_SERVICE_PORT environment variable not found");
        }

        String path = System.getenv(type + "_SERVICE_PATH");
        if (null == path) {
            throw new RuntimeException(type + "_SERVICE_PATH environment variable not found");
        }
        if (null != pathQuery) {
            path = path + pathQuery;
        }
        logger.info("pathQuery: " + pathQuery);
        logger.info("path: " + path);

        /**
         * Note: Due to AWS Serverless Java Container assume all requests to API Gateway
         * are using HTTPS, so it hardcoded context URL to use "https". This assumption
         * doesn't work in SAM local. TODO: create an issue in AWS Serverless Java Container Github Repo
         */
        String schemeOverride = System.getenv(type + "_SERVICE_SCHEME");
        logger.info("scheme override is: " + schemeOverride);
        String endpoint;
        if (null == schemeOverride) {
            endpoint = scheme + "://" + host + ":" + port + path;
        } else {
            endpoint = schemeOverride + "://" + host + ":" + port + path;
        }

        logger.info(type + " endpoint: " + endpoint);
        return endpoint;
    }

    private TraceHeader getTraceHeader(Segment segment, Subsegment subsegment) {
        if (segment == null || subsegment == null)
            return new TraceHeader();

        return new TraceHeader(segment.getTraceId(),
                segment.isSampled() ? subsegment.getId() : null,
                segment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
    }

}
