package fr.ulille.iut.pizzaland.dto;

import fr.ulille.iut.pizzaland.beans.Ingredient;

public class PizzaCreateDto {
	private String name;
	private Ingredient[] listeIngredient;

	public PizzaCreateDto() {}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public Ingredient[] getListeIngredient() {
		return listeIngredient;
	}

	public void setListeIngredient(Ingredient[] listeIngredient) {
		this.listeIngredient = listeIngredient;
	}
}