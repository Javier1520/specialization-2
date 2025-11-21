package com.epam.gym;

import com.epam.gym.config.JettyServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;

@Slf4j
public class RestApplication {

    private static final int PORT = 8080;

    public static void main(String[] args) {
        log.info("Starting REST Application on port {}", PORT);

        try {
            Server server = JettyServerConfig.createServer(PORT);
            server.start();
            log.info("REST Application started successfully on port {}", PORT);
            server.join();
        } catch (Exception e) {
            log.error("Error starting REST Application", e);
            System.exit(1);
        }
    }
}


