package fr.ulille.iut.pizzaland;

import fr.ulille.iut.pizzaland.ApiV1;
import fr.ulille.iut.pizzaland.dto.IngredientDto;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Logger;

/*
 * JerseyTest facilite l'écriture des tests en donnant accès aux
 * méthodes de l'interface javax.ws.rs.client.Client.
 * la méthode configure() permet de démarrer la ressource à tester
 */
public class IngredientResourceTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(IngredientResourceTest.class.getName());
    
    @Override
    protected Application configure() {
       return new ApiV1();
    }

    // Les méthodes setEnvUp() et tearEnvDown() serviront à terme à initialiser la base de données
    // et les DAO
    
    // https://stackoverflow.com/questions/25906976/jerseytest-and-junit-throws-nullpointerexception
    @Before
    public void setEnvUp() {
	
    }

    @After
    public void tearEnvDown() throws Exception {

    }

    @Test
    public void testGetEmptyList() {
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
}
