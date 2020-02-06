package fr.ulille.iut.pizzaland;

import fr.ulille.iut.pizzaland.ApiV1;

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

public class IngredientResourceTest extends JerseyTest {
    private static final Logger LOGGER = Logger.getLogger(IngredientResourceTest.class.getName());
    
    @Override
    protected Application configure() {
       return new ApiV1();
    }

    // https://stackoverflow.com/questions/25906976/jerseytest-and-junit-throws-nullpointerexception
    @Before
    public void setEnvUp() {
	
    }

    @After
    public void tearEnvDown() throws Exception {

    }

    @Test
    public void testGetEmptyList() {
        Response response = target("/ingredients").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
}
