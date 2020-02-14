package fr.ulille.iut.pizzaland.resources;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import fr.ulille.iut.pizzaland.BDDFactory;
import fr.ulille.iut.pizzaland.beans.Ingredient;
import fr.ulille.iut.pizzaland.dao.IngredientDao;
import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;
import fr.ulille.iut.pizzaland.dto.IngredientDto;


@Path("/ingredients")
public class IngredientResource {
	private static final Logger LOGGER = Logger.getLogger(IngredientResource.class.getName());

	private IngredientDao ingredients;


	@Context
	public UriInfo uriInfo;

	public IngredientResource() {
		ingredients = BDDFactory.buildDao(IngredientDao.class);
		ingredients.createTable();
	}


	@GET
	public List<IngredientDto> getAll() {
		LOGGER.info("IngredientResource:getAll");

		List<IngredientDto> l = ingredients.getAll().stream().map(Ingredient::toDto).collect(Collectors.toList());
		return l;
	}


	@GET
	@Path("{id}")
	public IngredientDto getOneIngredient(@PathParam("id") long id) {
		LOGGER.info("getOneIngredient(" + id + ")");
		try {
			Ingredient ingredient = ingredients.findById(id);
			return Ingredient.toDto(ingredient);
		}
		catch ( Exception e ) {
			// Cette exception générera une réponse avec une erreur 404
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	@POST
	public Response createIngredient(IngredientCreateDto ingredientCreateDto) {
		Ingredient existing = ingredients.findByName(ingredientCreateDto.getName());
		LOGGER.info("tentative creation");

		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Ingredient ingredient = Ingredient.fromIngredientCreateDto(ingredientCreateDto);
			LOGGER.info("tentative creation " + ingredient.getName());
			long id = ingredients.insert(ingredient.getName());
			ingredient.setId(id);
			IngredientDto ingredientDto = Ingredient.toDto(ingredient);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(ingredientDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}

	@DELETE
	@Path("{id}")
	public Response deleteIngredient(@PathParam("id") long id) {
		if ( ingredients.findById(id) == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		ingredients.remove(id);

		return Response.status(Response.Status.ACCEPTED).build();
	}

	@GET
	@Path("{id}/name")
	public String getIngredientName(@PathParam("id") long id) {
		Ingredient ingredient = ingredients.findById(id);
		if ( ingredient == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return ingredient.getName();
	}


	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createIngredient(@FormParam("name") String name) {
		Ingredient existing = ingredients.findByName(name);
		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Ingredient ingredient = new Ingredient();
			ingredient.setName(name);

			long id = ingredients.insert(ingredient.getName());
			ingredient.setId(id);
			IngredientDto ingredientDto = Ingredient.toDto(ingredient);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(ingredientDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}
}