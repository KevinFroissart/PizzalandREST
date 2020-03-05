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
import fr.ulille.iut.pizzaland.beans.Commande;
import fr.ulille.iut.pizzaland.dao.CommandeDao;
import fr.ulille.iut.pizzaland.dto.CommandeCreateDto;
import fr.ulille.iut.pizzaland.dto.CommandeDto;


@Path("/commandes")
public class CommandeResource {
	private static final Logger LOGGER = Logger.getLogger(CommandeResource.class.getName());

	private CommandeDao commandes;

	@Context
	public UriInfo uriInfo;

	public CommandeResource() {
		commandes = BDDFactory.buildDao(CommandeDao.class);
		commandes.createTable();
	}

	@GET
	public List<CommandeDto> getAll() {
		LOGGER.info("CommandeRessource:getAll");

		List<CommandeDto> l = commandes.getAll().stream().map(Commande::toDto).collect(Collectors.toList());
		return l;
	}

	@GET
	@Path("{id}")
	public CommandeDto getOneCommande(@PathParam("id") long id) {
		LOGGER.info("getOneCommande(" + id + ")");
		try {
			Commande commande = commandes.findById(id);
			return Commande.toDto(commande);
		}
		catch ( Exception e ) {
			// Cette exception générera une réponse avec une erreur 404
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

	@POST
	public Response createCommande(CommandeCreateDto commandeCreateDto) {
		Commande existing = commandes.findByName(commandeCreateDto.getName());
		LOGGER.info("tentative creation");

		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Commande commande = Commande.fromCommandeCreateDto(commandeCreateDto);
			LOGGER.info("tentative creation " + commande.getName());
			long id = commandes.insert(commande.getName());
			commande.setId(id);
			if(commande.getListePizza() != null) {
				for(int i=0; i<commande.getListePizza().length; i++) {
					commandes.insertPizza(id, commande.getListePizza()[i].getId());
				}
			}
			CommandeDto commandeDto = Commande.toDto(commande);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(commandeDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}

	@DELETE
	@Path("{id}")
	public Response deleteCommande(@PathParam("id") long id) {
		if ( commandes.findById(id) == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		commandes.remove(id);

		return Response.status(Response.Status.ACCEPTED).build();
	}

	@GET
	@Path("{id}/name")
	public String getCommandeName(@PathParam("id") long id) {
		Commande commande = commandes.findById(id);
		if ( commande == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		return commande.getName();
	}

	@POST
	@Consumes("application/x-www-form-urlencoded")
	public Response createCommande(@FormParam("name") String name) {
		Commande existing = commandes.findByName(name);
		if ( existing != null ) {
			throw new WebApplicationException(Response.Status.CONFLICT);
		}

		try {
			Commande commande = new Commande();
			commande.setName(name);

			long id = commandes.insert(commande.getName());
			commande.setId(id);
			if(commande.getListePizza() != null) {
				for(int i=0; i<commande.getListePizza().length; i++) {
					commandes.insertPizza(id, commande.getListePizza()[i].getId());
				}
			}
			CommandeDto commandeDto = Commande.toDto(commande);

			URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

			return Response.created(uri).entity(commandeDto).build();
		}
		catch ( Exception e ) {
			e.printStackTrace();
			throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
		}
	}
}