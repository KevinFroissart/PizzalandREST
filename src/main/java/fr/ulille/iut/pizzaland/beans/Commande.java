package fr.ulille.iut.pizzaland.beans;

import fr.ulille.iut.pizzaland.dto.CommandeCreateDto;
import fr.ulille.iut.pizzaland.dto.CommandeDto;
import fr.ulille.iut.pizzaland.dto.PizzaDto;

public class Commande {
	private long id;
	private String name;
	private String firstname;
	private Pizza[] listePizza;

	public Commande() {
	}

	public Commande(long id, String name, String firstname, Pizza[] listePizza) {
		this.id = id;
		this.name = name;
		this.firstname = firstname;
		this.listePizza = listePizza;
	}

	public Commande(long id, String name, String firstname) {
		this.id = id;
		this.name = name;
		this.firstname = firstname;
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
	
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Pizza[] getListePizza() {
		return listePizza;
	}

	public void setListePizza(Pizza[] listePizza) {
		this.listePizza = listePizza;
	}

	public static CommandeDto toDto(Commande i) {
		CommandeDto dto = new CommandeDto();
		dto.setId(i.getId());
		dto.setName(i.getName());
		dto.setListePizza(i.getListePizza());

		return dto;
	}

	public static Commande fromDto(CommandeDto dto) {
		Commande commande = new Commande();
		commande.setId(dto.getId());
		commande.setName(dto.getName());
		commande.setListePizza(dto.getListePizza());

		return commande;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Commande other = (Commande) obj;
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
		return "Commande [id=" + id + ", name=" + name + ", firstname=" + firstname + ", pizzas=" + listePizza.toString() + "]";
	}

	public static CommandeCreateDto toCreateDto(Commande commande) {
		CommandeCreateDto dto = new CommandeCreateDto();
		dto.setName(commande.getName());

		return dto;
	}

	public static Commande fromCommandeCreateDto(CommandeCreateDto dto) {
		Commande commande = new Commande();
		commande.setName(dto.getName());

		return commande;
	}
}