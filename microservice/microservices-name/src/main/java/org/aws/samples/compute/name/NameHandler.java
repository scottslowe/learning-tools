package org.aws.samples.compute.name;


import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.jersey.JerseyLambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameHandler implements RequestStreamHandler {
    private static final ResourceConfig jerseyApplication = new ResourceConfig()
                                                             .packages(NameHandler.class.getPackage().getName())
                                                             .register(JacksonFeature.class);
    private static final JerseyLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler
            = JerseyLambdaContainerHandler.getAwsProxyHandler(jerseyApplication);

    private static final Logger logger = LoggerFactory.getLogger(NameHandler.class);

    static {
      handler.stripBasePath("/resources");
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {

        AwsProxyRequest request = LambdaContainerHandler.getObjectMapper().readValue(inputStream, AwsProxyRequest.class);


        AwsProxyResponse resp = handler.proxy(request, context);

        LambdaContainerHandler.getObjectMapper().writeValue(outputStream, resp);

        // just in case it wasn't closed by the mapper
        outputStream.close();
    }
}
