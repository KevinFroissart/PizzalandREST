package fr.ulille.iut.pizzaland.beans;

import fr.ulille.iut.pizzaland.dto.PizzaCreateDto;
import fr.ulille.iut.pizzaland.dto.PizzaDto;

public class Pizza {
	private long id;
	private String name;
	private Ingredient[] listeIngredient;

	public Pizza() {
	}

	public Pizza(long id, String name, Ingredient[] listeIngredient) {
		this.id = id;
		this.name = name;
		this.listeIngredient = listeIngredient;
	}

	public Pizza(long id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Ingredient[] getListeIngredient() {
		return listeIngredient;
	}

	public void setListeIngredient(Ingredient[] listeIngredient) {
		this.listeIngredient = listeIngredient;
	}

	public static PizzaDto toDto(Pizza i) {
		PizzaDto dto = new PizzaDto();
		dto.setId(i.getId());
		dto.setName(i.getName());
		dto.setListeIngredient(i.getListeIngredient());

		return dto;
	}

	public static Pizza fromDto(PizzaDto dto) {
		Pizza Pizza = new Pizza();
		Pizza.setId(dto.getId());
		Pizza.setName(dto.getName());
		Pizza.setListeIngredient(dto.getListeIngredient());

		return Pizza;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pizza other = (Pizza) obj;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Pizza [id=" + id + ", name=" + name + ", ingrédients=" + listeIngredient.toString() + "]";
	}

	public static PizzaCreateDto toCreateDto(Pizza pizza) {
		PizzaCreateDto dto = new PizzaCreateDto();
		dto.setName(pizza.getName());

		return dto;
	}

	public static Pizza fromPizzaCreateDto(PizzaCreateDto dto) {
		Pizza Pizza = new Pizza();
		Pizza.setName(dto.getName());

		return Pizza;
	}
}