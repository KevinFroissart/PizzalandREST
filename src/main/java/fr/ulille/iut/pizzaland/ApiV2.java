package fr.ulille.iut.pizzaland;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import fr.ulille.iut.pizzaland.beans.Pizza;
import fr.ulille.iut.pizzaland.dao.PizzaDao;


@ApplicationPath("api/v2/")
public class ApiV2 extends ResourceConfig {
	private static final Logger LOGGER = Logger.getLogger(ApiV1.class.getName());

	public ApiV2() {
		packages("fr.ulille.iut.pizzaland");

		String environment = System.getenv("PIZZAENV");

		if ( environment != null && environment.equals("withdb") ) {
			LOGGER.info("Loading with database");
			Jsonb jsonb = JsonbBuilder.create();
			try {
				FileReader reader = new FileReader( getClass().getClassLoader().getResource("pizzas.json").getFile() );
				List<Pizza> pizzas = JsonbBuilder.create().fromJson(reader, new ArrayList<Pizza>(){}.getClass().getGenericSuperclass());

				PizzaDao PizzaDao = BDDFactory.buildDao(PizzaDao.class);
				PizzaDao.dropTable();
				PizzaDao.createTable();
				for ( Pizza pizza: pizzas) {
					PizzaDao.insert(pizza.getName());              
				}
			} catch ( Exception ex ) {
				throw new IllegalStateException(ex);
			}
		} 
	}
}