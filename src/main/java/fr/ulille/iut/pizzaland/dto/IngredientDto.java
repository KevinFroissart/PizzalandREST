package fr.ulille.iut.pizzaland.dto;

public class IngredientDto {
	private long id;
	private String name;

	public IngredientDto() {}

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
}