package fr.ulille.iut.pizzaland.resources;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import fr.ulille.iut.pizzaland.dto.IngredientDto;

@Path("/ingredients")
public class IngredientResource {
    private static final Logger LOGGER = Logger.getLogger(IngredientResource.class.getName());

    @Context
    public UriInfo uriInfo;

    public IngredientResource() {
    }

    @GET
    public List<IngredientDto> getAll() {
        LOGGER.info("IngredientResource:getAll");

	return null;
    }
}
