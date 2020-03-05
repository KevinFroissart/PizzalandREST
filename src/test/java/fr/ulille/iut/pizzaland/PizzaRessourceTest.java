package fr.ulille.iut.pizzaland;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import fr.ulille.iut.pizzaland.beans.Pizza;
import fr.ulille.iut.pizzaland.dao.PizzaDao;
import fr.ulille.iut.pizzaland.dto.PizzaCreateDto;
import fr.ulille.iut.pizzaland.dto.PizzaDto;

/*
 * JerseyTest facilite l'écriture des tests en donnant accès aux
 * méthodes de l'interface javax.ws.rs.client.Client.
 * la méthode configure() permet de démarrer la ressource à tester
 */
public class PizzaRessourceTest extends JerseyTest {
	private static final Logger LOGGER = Logger.getLogger(PizzaRessourceTest.class.getName());


	private PizzaDao dao;

	@Override
	protected Application configure() {
		BDDFactory.setJdbiForTests();

		return new ApiV1();
	}

	@Before
	public void setEnvUp() {
		dao = BDDFactory.buildDao(PizzaDao.class);
		dao.createTable();
	}

	@After
	public void tearEnvDown() throws Exception {
		dao.dropTable();
	}

	@Test
	public void testGetExistingPizza() {

		Pizza pizza = new Pizza();
		pizza.setName("margarita");

		long id = dao.insert(pizza.getName());
		pizza.setId(id);

		Response response = target("/Pizzas/" + id).request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		Pizza result = Pizza.fromDto(response.readEntity(PizzaDto.class));
		assertEquals(pizza, result);
	}


	@Test
	public void testGetEmptyList() {
		// La méthode target() permet de préparer une requête sur une URI.
		// La classe Response permet de traiter la réponse HTTP reçue.
		Response response = target("/Pizzas").request().get();

		// On vérifie le code de la réponse (200 = OK)
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


		// On vérifie la valeur retournée (liste vide)
		// L'entité (readEntity() correspond au corps de la réponse HTTP.
		// La classe javax.ws.rs.core.GenericType<T> permet de définir le type
		// de la réponse lue quand on a un type complexe (typiquement une liste).
		List<PizzaDto> pizzas;
		pizzas = response.readEntity(new GenericType<List<PizzaDto>>(){});

		assertEquals(0, pizzas.size());
	}

	@Test
	public void testGetNotExistingPizza() {
		Response response = target("/pizzas/125").request().get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatus());
	}

	@Test
	public void testCreatePizza() {
		PizzaCreateDto pizzaCreateDto = new PizzaCreateDto();
		pizzaCreateDto.setName("Reine");

		Response response = target("/Pizzas")
				.request()
				.post(Entity.json(pizzaCreateDto));

		// On vérifie le code de status à 201
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

		PizzaDto returnedEntity = response.readEntity(PizzaDto.class);

		// On vérifie que le champ d'entête Location correspond à
		// l'URI de la nouvelle entité
		assertEquals(target("/Pizzas/" +
				returnedEntity.getId()).getUri(), response.getLocation());

		// On vérifie que le nom correspond
		assertEquals(returnedEntity.getName(), pizzaCreateDto.getName());
	}

	@Test
	public void testCreateSamePizza() {
		PizzaCreateDto pizzaCreateDto = new PizzaCreateDto();
		pizzaCreateDto.setName("reine");
		dao.insert(pizzaCreateDto.getName());

		Response response = target("/Pizzas")
				.request()
				.post(Entity.json(pizzaCreateDto));

		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
	}

	@Test
	public void testCreatePizzaWithoutName() {
		PizzaCreateDto pizzaCreateDto = new PizzaCreateDto();

		Response response = target("/Pizzas")
				.request()
				.post(Entity.json(pizzaCreateDto));

		assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDeleteExistingPizza() {
		Pizza pizza = new Pizza();
		pizza.setName("margarita");
		long id = dao.insert(pizza.getName());
		pizza.setId(id);

		Response response = target("/Pizzas/" + id).request().delete();

		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

		Pizza result = dao.findById(id);
		assertEquals(result, null);
	}

	@Test
	public void testDeleteNotExistingPizza() {
		Response response = target("/Pizzas/125").request().delete();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
				response.getStatus());
	}

	@Test
	public void testGetPizzaName() {
		Pizza pizza = new Pizza();
		pizza.setName("margarita");
		long id = dao.insert(pizza.getName());

		Response response = target("Pizzas/" + id + "/name").request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		assertEquals("reine", response.readEntity(String.class));
	}

	@Test
	public void testGetNotExistingPizzaName() {
		Response response = target("Pizzas/125/name").request().get();

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void testCreateWithForm() {
		Form form = new Form();
		form.param("name", "chorizo");

		Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		Response response = target("Pizzas").request().post(formEntity);

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		String location = response.getHeaderString("Location");
		long id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));
		Pizza result = dao.findById(id);

		assertNotNull(result);
	}
}