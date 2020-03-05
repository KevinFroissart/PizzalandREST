### API et représentation des données

| Opération | URI         | Action réalisée                               | Retour                                        |
|:----------|:------------|:----------------------------------------------|:----------------------------------------------|
| GET       | /ingredients | récupère l'ensemble des ingrédients          | 200 et un tableau d'ingrédients               |
| GET       | /ingredients/{id} | récupère l'ingrédient d'identifiant id  | 200 et l'ingrédient                           |
|           |             |                                               | 404 si id est inconnu                         |
| GET       | /ingredients/{id}/name | récupère le nom de l'ingrédient    | 200 et le nom de l'ingrédient                 |
|           |             | d'identifiant id                              | 404 si id est inconnu                         |
| POST      | /ingredients | création d'un ingrédient                     | 201 et l'URI de la ressource créée + représentation |
|           |             |                                               | 400 si les informations ne sont pas correctes |
|           |             |                                               | 409 si l'ingrédient existe déjà (même nom)    |
| DELETE    | /ingredients/{id} | destruction de l'ingrédient d'identifiant id | 204 si l'opération à réussi                   |
|           |             |                                               | 404 si id est inconnu                         |


Un ingrédient comporte uniquement un identifiant et un nom. Sa
représentation JSON prendra donc la forme suivante :

    {
	  "id": 1,
	  "name": "mozzarella"
	}
	
Lors de la création, l'identifiant n'est pas connu car il sera fourni
par la base de données. Aussi on aura une
représentation JSON qui comporte uniquement le nom :

	{ "name": "mozzarella" }
