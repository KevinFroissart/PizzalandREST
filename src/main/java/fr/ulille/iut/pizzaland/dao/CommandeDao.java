package fr.ulille.iut.pizzaland.dao;

import java.util.List;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import fr.ulille.iut.pizzaland.beans.Commande;

public interface CommandeDao {

	@SqlUpdate("CREATE TABLE IF NOT EXISTS commandes (id INTEGER PRIMARY KEY, name VARCHAR UNIQUE NOT NULL)")
	void createTable();

	@SqlUpdate("CREATE TABLE IF NOT EXISTS commandePizzaAssociation (commande INTEGER NOT NULL, pizza INTEGER NOT NULL, PRIMARY KEY(commande, pizza), FOREIGN KEY(commande) REFERENCES commandes(id), FOREIGN KEY(pizza) REFERENCES pizzas(id))")
	void createAssociationTable();

	@Transaction
	default void createCommandeAndAssociationTable() {
		createAssociationTable();
		createTable();
	}

	@SqlUpdate("DROP TABLE IF EXISTS commandes; DROP TABLE IF EXISTS commandePizzaAssociation")
	void dropTable();

	@SqlUpdate("INSERT INTO commandes (name) VALUES (:name)")
	@GetGeneratedKeys
	long insert(String name);

	@SqlQuery("SELECT * FROM commandes")
	@RegisterBeanMapper(Commande.class)
	List<Commande> getAll();

	@SqlQuery("SELECT * FROM commandes WHERE id = :id")
	@RegisterBeanMapper(Commande.class)
	Commande findById(long id);

	@SqlQuery("SELECT * FROM commandes WHERE name = :name")
	@RegisterBeanMapper(Commande.class)
	Commande findByName(String name);

	@SqlUpdate("DELETE FROM commandes WHERE id = :id")
	void remove(long id);

	@SqlUpdate("INSERT INTO commandePizzaAssociation (idCommande, idPizza) VALUES (:commande, :pizza)")
	@GetGeneratedKeys
	long insertPizza(long idcommande, long idPizza);

	@SqlQuery("SELECT * FROM commandeIngredientAssociation WHERE commande = :commande AND pizza = :pizza")
	@RegisterBeanMapper(Commande.class)
	Commande findAssociationByIds(long idcommande, long idPizza);
}