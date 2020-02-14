package fr.ulille.iut.pizzaland.dao;

import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

public interface PizzaDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS Pizzas ....")
	void createPizzaTable();

	@SqlUpdate("CREATE TABLE IF NOT EXISTS PizzaIngredientsAssociation .....")
	void createAssociationTable();

	@Transaction
	default void createTableAndIngredientAssociation() {
		createAssociationTable();
		createPizzaTable();
	}
}