### API et représentation des données

| Opération | URI         | Action réalisée                               | Retour                                        |
|:----------|:------------|:----------------------------------------------|:----------------------------------------------|
| GET       | /pizzas | récupère l'ensemble des pizzas          | 200 et un tableau de pizzas               |
| GET       | /pizzas/{id} | récupère la pizza d'identifiant id  | 200 et la pizza                           |
|           |             |                                               | 404 si id est inconnu                         |
| GET       | /pizzas/{id}/name | récupère le nom de la pizza    | 200 et le nom de la pizza                 |
|           |             | d'identifiant id                              | 404 si id est inconnu                         |
| GET       | /pizzas/search?ingredient=name | récupère les pizzas qui possèdent cet ingrédient    | 200 et une liste de pizza                 |
| POST      | /pizzas | création d'une pizza                     | 201 et l'URI de la ressource créée + représentation |
|           |             |                                               | 400 si les informations ne sont pas correctes |
|           |             |                                               | 409 si la pizza existe déjà (même nom)    |
| DELETE    | /pizzas/{id} | destruction de la pizza d'identifiant id | 204 si l'opération à réussi                   |
|           |             |                                               | 404 si id est inconnu                         |


Une pizza comporte un identifiant, un nom et des ingrédients.
Sa représentation JSON prendra donc la forme suivante :

    {
	  "id": 1,
	  "name": "margarita",
	  "ingredients":
	  		[{ "name": "jambon" }, { 
	  		   "name": "mozzarella" }
	  		]}
	}
	
Lors de la création, l'identifiant n'est pas connu car il sera fourni
par la base de données. Aussi on aura une
représentation JSON qui comporte uniquement le nom :
	
	{ "name": "margarita", "ingredients": [{ "name": "jambon" }, { "name": "mozzarella" }]},
	