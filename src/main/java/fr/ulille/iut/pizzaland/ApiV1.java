package fr.ulille.iut.pizzaland;

import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("api/v1/")
public class ApiV1 extends ResourceConfig {
    private static final Logger LOGGER = Logger.getLogger(ApiV1.class.getName());

    public ApiV1() {
        packages("fr.ulille.iut.pizzaland");
    }
}
