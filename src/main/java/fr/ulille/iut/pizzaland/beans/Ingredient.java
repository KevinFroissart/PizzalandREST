package fr.ulille.iut.pizzaland.beans;

import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;
import fr.ulille.iut.pizzaland.dto.IngredientDto;

public class Ingredient {
	private long id;
	private String name;

	public Ingredient() {
	}

	public Ingredient(long id, String name) {
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

	public static IngredientDto toDto(Ingredient i) {
		IngredientDto dto = new IngredientDto();
		dto.setId(i.getId());
		dto.setName(i.getName());

		return dto;
	}

	public static Ingredient fromDto(IngredientDto dto) {
		Ingredient ingredient = new Ingredient();
		ingredient.setId(dto.getId());
		ingredient.setName(dto.getName());

		return ingredient;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ingredient other = (Ingredient) obj;
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
		return "Ingredient [id=" + id + ", name=" + name + "]";
	}

	public static IngredientCreateDto toCreateDto(Ingredient ingredient) {
		IngredientCreateDto dto = new IngredientCreateDto();
		dto.setName(ingredient.getName());

		return dto;
	}

	public static Ingredient fromIngredientCreateDto(IngredientCreateDto dto) {
		Ingredient ingredient = new Ingredient();
		ingredient.setName(dto.getName());

		return ingredient;
	}
}