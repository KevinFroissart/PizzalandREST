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

import fr.ulille.iut.pizzaland.beans.Ingredient;
import fr.ulille.iut.pizzaland.dao.IngredientDao;
import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;
import fr.ulille.iut.pizzaland.dto.IngredientDto;

/*
 * JerseyTest facilite l'écriture des tests en donnant accès aux
 * méthodes de l'interface javax.ws.rs.client.Client.
 * la méthode configure() permet de démarrer la ressource à tester
 */
public class IngredientResourceTest extends JerseyTest {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logger.getLogger(IngredientResourceTest.class.getName());


	private IngredientDao dao;

	@Override
	protected Application configure() {
		BDDFactory.setJdbiForTests();

		return new ApiV1();
	}

	@Before
	public void setEnvUp() {
		dao = BDDFactory.buildDao(IngredientDao.class);
		dao.createTable();
	}

	@After
	public void tearEnvDown() throws Exception {
		dao.dropTable();
	}

	@Test
	public void testGetExistingIngredient() {

		Ingredient ingredient = new Ingredient();
		ingredient.setName("mozzarella");

		long id = dao.insert(ingredient.getName());
		ingredient.setId(id);

		Response response = target("/ingredients/" + id).request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		Ingredient result = Ingredient.fromDto(response.readEntity(IngredientDto.class));
		assertEquals(ingredient, result);
	}

	@Test
	public void testGetEmptyList() {
		dao.dropTable();
		// La méthode target() permet de préparer une requête sur une URI.
		// La classe Response permet de traiter la réponse HTTP reçue.
		Response response = target("/ingredients").request().get();

		// On vérifie le code de la réponse (200 = OK)
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());


		// On vérifie la valeur retournée (liste vide)
		// L'entité (readEntity() correspond au corps de la réponse HTTP.
		// La classe javax.ws.rs.core.GenericType<T> permet de définir le type
		// de la réponse lue quand on a un type complexe (typiquement une liste).
		List<IngredientDto> ingredients;
		ingredients = response.readEntity(new GenericType<List<IngredientDto>>(){});

		assertEquals(0, ingredients.size());
	}

	@Test
	public void testGetNotExistingPizza() {
		Response response = target("/pizzas/125").request().get();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatus());
	}

	@Test
	public void testCreateIngredient() {
		IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();
		ingredientCreateDto.setName("mozzarella");

		Response response = target("/ingredients")
				.request()
				.post(Entity.json(ingredientCreateDto));

		// On vérifie le code de status à 201
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

		IngredientDto returnedEntity = response.readEntity(IngredientDto.class);

		// On vérifie que le champ d'entête Location correspond à
		// l'URI de la nouvelle entité
		assertEquals(target("/ingredients/" +
				returnedEntity.getId()).getUri(), response.getLocation());

		// On vérifie que le nom correspond
		assertEquals(returnedEntity.getName(), ingredientCreateDto.getName());
	}

	@Test
	public void testCreateSameIngredient() {
		IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();
		ingredientCreateDto.setName("mozzarella");
		dao.insert(ingredientCreateDto.getName());

		Response response = target("/ingredients")
				.request()
				.post(Entity.json(ingredientCreateDto));

		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
	}

	@Test
	public void testCreateIngredientWithoutName() {
		IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();

		Response response = target("/ingredients")
				.request()
				.post(Entity.json(ingredientCreateDto));

		assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
	}

	@Test
	public void testDeleteExistingIngredient() {
		Ingredient ingredient = new Ingredient();
		ingredient.setName("mozzarella");
		long id = dao.insert(ingredient.getName());
		ingredient.setId(id);

		Response response = target("/ingredients/" + id).request().delete();

		assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());

		Ingredient result = dao.findById(id);
		assertEquals(result, null);
	}

	@Test
	public void testDeleteNotExistingIngredient() {
		Response response = target("/ingredients/125").request().delete();
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
				response.getStatus());
	}

	@Test
	public void testGetIngredientName() {
		Ingredient ingredient = new Ingredient();
		ingredient.setName("mozzarella");
		long id = dao.insert(ingredient.getName());

		Response response = target("ingredients/" + id + "/name").request().get();

		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

		assertEquals("mozzarella", response.readEntity(String.class));
	}

	@Test
	public void testGetNotExistingIngredientName() {
		Response response = target("ingredients/125/name").request().get();

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	@Test
	public void testCreateWithForm() {
		Form form = new Form();
		form.param("name", "chorizo");

		Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		Response response = target("ingredients").request().post(formEntity);

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
		String location = response.getHeaderString("Location");
		long id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));
		Ingredient result = dao.findById(id);

		assertNotNull(result);
	}
}