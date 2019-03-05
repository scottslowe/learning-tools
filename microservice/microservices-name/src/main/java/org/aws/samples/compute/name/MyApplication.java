package org.aws.samples.compute.name;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.Set;

import static org.aws.samples.compute.name.MyApplication.APP_ROOT;

/**
 * @author arungupta
 */
@ApplicationPath(APP_ROOT)
public class MyApplication extends Application {

    public static final String APP_ROOT = "/resources";

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new java.util.HashSet<>();
        classes.add(NameEndpoint.class);
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> resources = new java.util.HashSet<>();
        resources.add(StartupBean.getInstance());
        return resources;
    }
}
