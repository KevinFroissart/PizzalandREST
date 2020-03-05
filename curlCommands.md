## Ingrédients

### Ajouter les ingrédients :

	curl -d "id=1&name=reblochon" -i localhost:8080/api/v1/ingredients --noproxy localhost

### Afficher la liste d'ingrédients :

	curl -i localhost:8080/api/v1/ingredients/ --noproxy localhost

### Afficher un ingrédient particulié :

	curl -i localhost:8080/api/v1/ingredients/1 --noproxy localhost

### Détruire un ingrédient :

	curl -X DELETE -vi localhost:8080/api/v1/ingredients/1 --noproxy localhost


## Pizzas

### Ajouter une pizza :

	curl -d "id=1&name=napolitaine" -i localhost:8080/api/v1/pizzas --noproxy localhost

### Afficher les pizzas :

	curl -i localhost:8080/api/v1/pizzas/ --noproxy localhost

### Afficher une pizza en particulier :

	curl -i localhost:8080/api/v1/pizzas/1 --noproxy localhost

### Détruire une pizza :

	curl -X DELETE -vi localhost:8080/api/v1/pizzas/1 --noproxy localhost

## Commandes

### Ajouter une commande :

	curl -d "id=1&name=Froissart" -i localhost:8080/api/v1/commandes --noproxy localhost

### Afficher les commande :

	curl -i localhost:8080/api/v1/commandes/ --noproxy localhost

### Afficher une commande en particulier :

	curl -i localhost:8080/api/v1/commandes/1 --noproxy localhost

### Détruire une commande :

	curl -X DELETE -vi localhost:8080/api/v1/commandes/1 --noproxy localhost
