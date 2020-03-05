package fr.ulille.iut.pizzaland.dto;

import fr.ulille.iut.pizzaland.beans.Pizza;

public class CommandeDto {
	private long id;
	private String name;
	private String firstname;
	private Pizza[] listePizza;

	public CommandeDto() {}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public void setFirstame(String firstname) {
		this.firstname = firstname;
	}

	public String getFirstname() {
		return firstname;
	}

	public Pizza[] getListePizza() {
		return listePizza;
	}

	public void setListePizza(Pizza[] listePizza) {
		this.listePizza = listePizza;
	}
}