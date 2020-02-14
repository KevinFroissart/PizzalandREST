package fr.ulille.iut.pizzaland.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import fr.ulille.iut.pizzaland.beans.Ingredient;

public interface IngredientDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name VARCHAR UNIQUE NOT NULL)")
	void createTable();

	@SqlUpdate("DROP TABLE IF EXISTS ingredients")
	void dropTable();

	@SqlUpdate("INSERT INTO ingredients (name) VALUES (:name)")
	@GetGeneratedKeys
	long insert(String name);

	@SqlQuery("SELECT * FROM ingredients")
	@RegisterBeanMapper(Ingredient.class)
	List<Ingredient> getAll();

	@SqlQuery("SELECT * FROM ingredients WHERE id = :id")
	@RegisterBeanMapper(Ingredient.class)
	Ingredient findById(long id);
	
	@SqlQuery("SELECT * FROM ingredients WHERE name = :name")
	@RegisterBeanMapper(Ingredient.class)
	Ingredient findByName(String name);
	
	@SqlUpdate("DELETE FROM ingredients WHERE id = :id")
	void remove(long id);
}