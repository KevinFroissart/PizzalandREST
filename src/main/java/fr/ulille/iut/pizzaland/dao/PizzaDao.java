package fr.ulille.iut.pizzaland.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import fr.ulille.iut.pizzaland.beans.Pizza;

public interface PizzaDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS pizzas (id INTEGER PRIMARY KEY, name VARCHAR UNIQUE NOT NULL)")
	void createTable();

	@SqlUpdate("CREATE TABLE IF NOT EXISTS pizzaIngredientAssociation (pizza INTEGER NOT NULL, ingredient INTEGER NOT NULL, PRIMARY KEY(pizza, ingredient), FOREIGN KEY(pizza) REFERENCES pizzas(id), FOREIGN KEY(ingredient) REFERENCES ingredients(id))")
	void createAssociationTable();

	@Transaction
	default void createPizzaAndAssociationTable() {
		createAssociationTable();
		createTable();
	}

	@SqlUpdate("DROP TABLE IF EXISTS pizzas; DROP TABLE IF EXISTS pizzaIngredientAssociation")
	void dropTable();

	@SqlUpdate("INSERT INTO pizzas (name) VALUES (:name)")
	@GetGeneratedKeys
	long insert(String name);

	@SqlQuery("SELECT * FROM pizzas")
	@RegisterBeanMapper(Pizza.class)
	List<Pizza> getAll();

	@SqlQuery("SELECT * FROM pizzas WHERE id = :id")
	@RegisterBeanMapper(Pizza.class)
	Pizza findById(long id);

	@SqlQuery("SELECT * FROM pizzas WHERE name = :name")
	@RegisterBeanMapper(Pizza.class)
	Pizza findByName(String name);

	@SqlUpdate("DELETE FROM pizzas WHERE id = :id")
	void remove(long id);

	@SqlUpdate("INSERT INTO pizzaIngredientAssociation (idPizza, idIngredient) VALUES (:pizza, :ingredient)")
	@GetGeneratedKeys
	long insertIngredient(long idPizza, long idIngredient);

	@SqlQuery("SELECT * FROM pizzaIngredientAssociation WHERE pizza = :pizza AND ingredient = :ingredient")
	@RegisterBeanMapper(Pizza.class)
	Pizza findAssociationByIds(long idPizza, long idIngredient);
}