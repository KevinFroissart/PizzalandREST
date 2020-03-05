### API et représentation des données

| Opération | URI         | Action réalisée                               | Retour                                        |
|:----------|:------------|:----------------------------------------------|:----------------------------------------------|
| GET       | /commandes | récupère l'ensemble des commandes          | 200 et un tableau de commandes               |
| GET       | /commandes/{id} | récupère la commande d'identifiant id  | 200 et la commande                           |
|           |             |                                               | 404 si id est inconnu                         |
| GET       | /commandes/{id}/name | récupère la commande associée au nom    | 200 et le nom de la commande                 |
|           |             | d'identifiant id                              | 404 si id est inconnu                         |
| POST      | /commandes | création d'une commande                     | 201 et l'URI de la ressource créée + représentation |
|           |             |                                               | 400 si les informations ne sont pas correctes |
|           |             |                                               | 409 si la commande existe déjà (même nom)    |
| DELETE    | /commandes/{id} | destruction de la commande d'identifiant id | 204 si l'opération à réussi                   |
|           |             |                                               | 404 si id est inconnu                         |


Une commande comporte un identifiant, le nom et prénom du client et la liste des commandes commandées. 
Sa représentation JSON prendra donc la forme suivante :

    {
	  "id": 1,
      "firstName": "Kévin",
      "name": "Froissart",
      "pizzas":
      		[{ "name": "margarita" }, { 
	  		   "name": "cannibale" }, {
	  		   "name": "reine" }
	  		]}
	}

Lors de la création, l'identifiant n'est pas connu car il sera fourni
par la base de données. Aussi on aura une
représentation JSON qui comporte la liste de commandes et le nom et prénom du client :

	{
      "firstName": "Kévin",
      "name": "Froissart",
      "pizzas":
      		[{ "name": "margarita" }, { 
	  		   "name": "cannibale" }, {
	  		   "name": "reine" }
	  		]}
	}
