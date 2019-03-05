package org.aws.samples.compute.webapp;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

import static org.aws.samples.compute.webapp.MyApplication.APP_ROOT;

/**
 * @author arungupta
 */
@ApplicationPath(APP_ROOT)
public class MyApplication extends Application {

    public static final String APP_ROOT = "/";

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(WebappController.class);
        return resources;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> resources = new java.util.HashSet<>();
        resources.add(StartupBean.getInstance());
        return resources;
    }
}
