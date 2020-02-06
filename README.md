# Développement REST avec Jersey

## Mise en plance de l'environnement
Ce tutoriel utilise Maven. Si votre machine se trouve derrière un
proxy, vous devrez mettre la configuration suivante dans le fichier
`~/.m2/settings.xml` (à adapter à votre environnement) :

	<settings>
		<proxies>
			<proxy>
				<id>lille1-proxy</id>
				<active>true</active>
				<protocol>http</protocol>
				<host>cache.univ-lille.fr</host>
				<port>3128</port>
			</proxy>
			<proxy>
				<id>lille1-proxy-sec</id>
				<active>true</active>
				<protocol>https</protocol>
				<host>cache.univ-lille.fr</host>
				<port>3128</port>
			</proxy>
		</proxies>
	</settings>

En local, vous pouvez également recopier le fichier `/home/public/peter/maven/settings.xml`.

## Récupération du projet initial

.
├── pom.xml
└── src
    ├── main
    │   ├── java
    │   │   └── fr
    │   │       └── ulille
    │   │           └── iut
    │   │               └── pizzaland
    │   │                   ├── ApiV1.java
    │   │                   ├── BDDFactory.java
    │   │                   ├── beans
    │   │                   ├── dao
    │   │                   ├── dto
    │   │                   └── resources
    │   ├── resources
    │   │   ├── ingredients.json
    │   │   └── logback.xml
    │   └── webapp
    │       └── WEB-INF
    └── test
        ├── java
        │   └── fr
        │       └── ulille
        │           └── iut
        │               └── pizzaland
        │                   └── IngredientResourceTest.java
        └── resources
            └── logback-test.xml

## Développement d'une ressource `Ingredient`
Nous pouvons tout d'abord réfléchir à l'API REST que nous allons offrir pour la ressource ~Ingredient~. Celle-ci devrait répondre aux URI suivantes :

| Opération | URI         | Action réalisée                               | Retour                                        |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|
| GET       | /ingredients | récupère l'ensemble des ingrédients          | 200 et un tableau d'ingrédients               |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|
| GET       | /ingredients/{id} | récupère l'ingrédient d'identifiant id  | 200 et l'ingrédient                           |
|           |             |                                               | 404 si id est inconnu                         |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|
| GET       | /ingredients/{id}/name | récupère le nom de l'ingrédient    | 200 et le nom de l'ingrédient                 |
|           |             | d'identifiant id                              | 404 si id est inconnu                         |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|
| POST      | /ingredients | création d'un ingrédient                     | 201 et l'URI de la ressource créée + représentation |
|           |             |                                               | 400 si les informations ne sont pas correctes |
|           |             |                                               | 409 si l'ingrédient existe déjà (même nom)    |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|
| DELETE    | /ingredients/{id} | destruction de l'ingrédient d'identifiant id | 204 si l'opération à réussi                   |
|           |             |                                               | 404 si id est inconnu                         |
|-----------|-------------|-----------------------------------------------|-----------------------------------------------|

## Mise en œuvre

### Une première implémentation : récupérer les ingrédients existants
Nous allons réaliser un développement dirigé par les tests. Dans un
premier temps, nous allons commencer par un test qui récupère une
liste d'ingrédients vide qui sera matérialisée par un tableau JSON
vide `[]`.

Le code suivant qui se trouve dans la classe `IngredientResourceTest`
montre la mise en place de l'environnement (`configure()`) et l'amorce
d'un premier test.

    public class IngredientResourceTest extends JerseyTest {
        
      @Override
      protected Application configure() {
        return new ApiV1();
      }

	  @Test
      public void testGetEmptyList() {
        Response response = target("/ingredients").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
      }
