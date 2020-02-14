package fr.ulille.iut.pizzaland;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import fr.ulille.iut.pizzaland.beans.Ingredient;
import fr.ulille.iut.pizzaland.dao.IngredientDao;


@ApplicationPath("api/v1/")
public class ApiV1 extends ResourceConfig {
	private static final Logger LOGGER = Logger.getLogger(ApiV1.class.getName());

	public ApiV1() {
		packages("fr.ulille.iut.pizzaland");

		String environment = System.getenv("PIZZAENV");

		if ( environment != null && environment.equals("withdb") ) {
			LOGGER.info("Loading with database");
			Jsonb jsonb = JsonbBuilder.create();
			try {
				FileReader reader = new FileReader( getClass().getClassLoader().getResource("ingredients.json").getFile() );
				List<Ingredient> ingredients = JsonbBuilder.create().fromJson(reader, new ArrayList<Ingredient>(){}.getClass().getGenericSuperclass());

				IngredientDao ingredientDao = BDDFactory.buildDao(IngredientDao.class);
				ingredientDao.dropTable();
				ingredientDao.createTable();
				for ( Ingredient ingredient: ingredients) {
					ingredientDao.insert(ingredient.getName());              
				}
			} catch ( Exception ex ) {
				throw new IllegalStateException(ex);
			}
		} 
	}
}