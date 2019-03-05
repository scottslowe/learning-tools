package org.aws.samples.compute.greeting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import static org.aws.samples.compute.greeting.MyApplication.APP_ROOT;

/**
 * @author arungupta
 */
@ApplicationPath(APP_ROOT)
public class MyApplication extends Application {

    public static final String APP_ROOT = "/resources";
    private static final Logger logger = LoggerFactory.getLogger(MyApplication.class);

    @Override
    public Set<Class<?>> getClasses() {
        logger.info("getClasses");
        Set<Class<?>> classes = new java.util.HashSet<>();
        classes.add(GreetingEndpoint.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        logger.info("getSingletons");
        Set<Object> resources = new java.util.HashSet<>();
        resources.add(StartupBean.getInstance());
        return resources;
    }
}
