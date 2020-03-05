package fr.ulille.iut.pizzaland;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.ulille.iut.pizzaland.beans.Commande;
import fr.ulille.iut.pizzaland.beans.Ingredient;
import fr.ulille.iut.pizzaland.beans.Pizza;
import fr.ulille.iut.pizzaland.dao.CommandeDao;
import fr.ulille.iut.pizzaland.dao.PizzaDao;
import fr.ulille.iut.pizzaland.dto.CommandeCreateDto;
import fr.ulille.iut.pizzaland.dto.CommandeDto;
import fr.ulille.iut.pizzaland.dto.PizzaCreateDto;
import fr.ulille.iut.pizzaland.dto.PizzaDto;

/*
 * JerseyTest facilite l'écriture des tests en donnant accès aux
 * méthodes de l'interface javax.ws.rs.client.Client.
 * la méthode configure() permet de démarrer la ressource à tester
 */
public class CommandeResourceTest extends JerseyTest {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(CommandeResourceTest.class.getName());

	private CommandeDao dao;
	private PizzaDao pizzaDao;

	@Override
	protected Application configure() {
		BDDFactory.setJdbiForTests();
		return new ApiV1();
	}

	@Before
	public void setEnvUp() {
		dao = BDDFactory.buildDao(CommandeDao.class);
		pizzaDao = BDDFactory.buildDao(PizzaDao.class);
		pizzaDao.createTable();
		dao.createCommandeAndAssociationTable();
	}

	@After
	public void tearEnvDown() throws Exception {
		dao.dropTable();
		pizzaDao.dropTable();
	}

	@Test
	public void testGetExistingCommande() {
		Commande commande = new Commande();
		commande.setName("Froissart");

		long id = dao.insert(commande.getName());
		commande.setId(id);

		Response response = target("/commandes/" + id).request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		Commande result = Commande.fromDto(response.readEntity(CommandeDto.class));
		assertEquals(commande, result);
	}


	@Test
	public void testGetEmptyList() {
		// La méthode target() permet de préparer une requête sur une URI.
		// La classe Response permet de traiter la réponse HTTP reçue.
		Response response = target("/commandes").request().get();

		// On vérifie le code de la réponse (200 = OK)
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


		// On vérifie la valeur retournée (liste vide)
		// L'entité (readEntity() correspond au corps de la réponse HTTP.
		// La classe javax.ws.rs.core.GenericType<T> permet de définir le type
		// de la réponse lue quand on a un type complexe (typiquement une liste).
		List<CommandeDto> commandes;
		commandes = response.readEntity(new GenericType<List<CommandeDto>>(){});

		assertEquals(0, commandes.size());
	}

	@Test
	public void testGetNotExistingCommande() {
		Response response = target("/commandes/125").request().get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatus());
	}

	@Test
	public void testCreateCommande() {
		CommandeCreateDto commandeCreateDto = new CommandeCreateDto();
		commandeCreateDto.setName("Jean-Marie");

		Response response = target("/commandes")
				.request()
				.post(Entity.json(commandeCreateDto));

		// On vérifie le code de status à 201
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

		CommandeDto returnedEntity = response.readEntity(CommandeDto.class);

		// On vérifie que le champ d'entête Location correspond à
		// l'URI de la nouvelle entité
		assertEquals(target("/commandes/" +
				returnedEntity.getId()).getUri(), response.getLocation());

		// On vérifie que le nom correspond
		assertEquals(returnedEntity.getName(), commandeCreateDto.getName());
	}
	
	@Test
	public void testCreateCommandeWithPizzas() {
		CommandeCreateDto commandeCreateDto = new CommandeCreateDto();
		commandeCreateDto.setName("Jean-Marie");
		List<Pizza> pizzas = new ArrayList<Pizza>();
		Pizza pizza1 = pizzaDao.findByName("reine");
		if(pizza1 == null) {
			long id = pizzaDao.insert("reine");
			pizzas.add(new Pizza(id, "reine"));
		}
		else pizzas.add(pizza1);

		Response response = target("/commandes")
				.request()
				.post(Entity.json(commandeCreateDto));

		// On vérifie le code de status à 201
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

		CommandeDto returnedEntity = response.readEntity(CommandeDto.class);

		// On vérifie que le champ d'entête Location correspond à
		// l'URI de la nouvelle entité
		assertEquals(target("/commandes/" +
				returnedEntity.getId()).getUri(), response.getLocation());

		// On vérifie que le nom correspond
		assertEquals(returnedEntity.getName(), commandeCreateDto.getName());
	}

	@Test
	public void testCreateCommandeWithoutName() {
		CommandeCreateDto commandeCreateDto = new CommandeCreateDto();

		Response response = target("/commandes")
				.request()
				.post(Entity.json(commandeCreateDto));

		assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDeleteExistingCommande() {
		Commande commande = new Commande();
		commande.setName("Froissart");
		long id = dao.insert(commande.getName());
		commande.setId(id);

		Response response = target("/commandes/" + id).request().delete();

		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

		Commande result = dao.findById(id);
		assertEquals(result, null);
	}

	@Test
	public void testDeleteNotExistingCommande() {
		Response response = target("/commandes/125").request().delete();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
				response.getStatus());
	}

	@Test
	public void testGetCommandeName() {
		Commande commande = new Commande();
		commande.setName("Froissart");
		long id = dao.insert(commande.getName());

		Response response = target("commandes/" + id + "/name").request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		assertEquals("Froissart", response.readEntity(String.class));
	}

	@Test
	public void testGetNotExistingCommandeName() {
		Response response = target("commande/125/name").request().get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void testCreateWithForm() {
		Form form = new Form();
		form.param("name", "Froissart");

		Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		Response response = target("commandes").request().post(formEntity);

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		String location = response.getHeaderString("Location");
		long id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));
		Commande result = dao.findById(id);

		assertNotNull(result);
	}
}