package fr.ulille.iut.pizzaland.dto;

import java.util.List;

import fr.ulille.iut.pizzaland.beans.Ingredient;

public class PizzaDto {
	private long id;
	private String name;
	private List<Ingredient> listeIngredient;

	public PizzaDto() {}

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

	public List<Ingredient> getListeIngredient() {
		return listeIngredient;
	}

	public void setListeIngredient(List<Ingredient> listeIngredient) {
		this.listeIngredient = listeIngredient;
	}
}