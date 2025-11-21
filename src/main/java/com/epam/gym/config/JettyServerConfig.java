package com.epam.gym.config;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class JettyServerConfig {

    public static Server createServer(int port) {
        Server server = new Server(port);

        // Create the servlet context
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Create root application context
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);

        // Create web application context
        AnnotationConfigWebApplicationContext webApplicationContext = new AnnotationConfigWebApplicationContext();
        webApplicationContext.setParent(rootContext);
        webApplicationContext.register(WebConfig.class);
        webApplicationContext.setConfigLocation("com.epam.gym.config");

        // Add Spring context loader listener
        context.addEventListener(new ContextLoaderListener(rootContext));

        // Add Spring dispatcher servlet
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webApplicationContext);
        ServletHolder servletHolder = new ServletHolder("dispatcher", dispatcherServlet);
        servletHolder.setInitOrder(1);
        context.addServlet(servletHolder, "/*");

        server.setHandler(context);

        return server;
    }
}

