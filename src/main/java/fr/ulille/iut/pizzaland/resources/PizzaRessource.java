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
import fr.ulille.iut.pizzaland.beans.Pizza;
import fr.ulille.iut.pizzaland.dao.PizzaDao;
import fr.ulille.iut.pizzaland.dto.PizzaCreateDto;
import fr.ulille.iut.pizzaland.dto.PizzaDto;


@Path("/pizzas")
public class PizzaRessource {
	private static final Logger LOGGER = Logger.getLogger(PizzaRessource.class.getName());

	private PizzaDao pizzas;

	@Context
	public UriInfo uriInfo;

	public PizzaRessource() {
		pizzas = BDDFactory.buildDao(PizzaDao.class);
		pizzas.createTable();
	}

	@GET
	public List<PizzaDto> getAll() {
		LOGGER.info("PizzaRessource:getAll");

		List<PizzaDto> l = pizzas.getAll().stream().map(Pizza::toDto).collect(Collectors.toList());
		return l;
	}

	@GET
	@Path("{id}")
	public PizzaDto getOnePizza(@PathParam("id") long id) {
		LOGGER.info("getOnePizza(" + id + ")");
		try {
			Pizza pizza = pizzas.findById(id);
			return Pizza.toDto(pizza);
		}
		catch ( Exception e ) {
			// Cette exception générera une réponse avec une erreur 404
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	@POST
	public Response createPizza(PizzaCreateDto PizzaCreateDto) {
		Pizza existing = pizzas.findByName(PizzaCreateDto.getName());
		LOGGER.info("tentative creation");

		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Pizza pizza = Pizza.fromPizzaCreateDto(PizzaCreateDto);
			LOGGER.info("tentative creation " + pizza.getName());
			long id = pizzas.insert(pizza.getName());
			pizza.setId(id);
			if(pizza.getListeIngredient() != null) {
				for(int i=0; i<pizza.getListeIngredient().length; i++) {
					pizzas.insertIngredient(id, pizza.getListeIngredient()[i].getId());
				}
			}
			PizzaDto pizzaDto = Pizza.toDto(pizza);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(pizzaDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}

	@DELETE
	@Path("{id}")
	public Response deletePizza(@PathParam("id") long id) {
		if ( pizzas.findById(id) == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		pizzas.remove(id);

		return Response.status(Response.Status.ACCEPTED).build();
	}

	@GET
	@Path("{id}/name")
	public String getPizzaName(@PathParam("id") long id) {
		Pizza Pizza = pizzas.findById(id);
		if ( Pizza == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return Pizza.getName();
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createPizza(@FormParam("name") String name) {
		Pizza existing = pizzas.findByName(name);
		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Pizza pizza = new Pizza();
			pizza.setName(name);

			long id = pizzas.insert(pizza.getName());
			pizza.setId(id);
			if(pizza.getListeIngredient() != null) {
				for(int i=0; i<pizza.getListeIngredient().length; i++) {
					pizzas.insertIngredient(id, pizza.getListeIngredient()[i].getId());
				}
			}
			PizzaDto PizzaDto = Pizza.toDto(pizza);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(PizzaDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}
}