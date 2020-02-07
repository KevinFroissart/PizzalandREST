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

Un ingrédient comporte uniquement un identifiant et un nom. Ça
représentation JSON prendra donc la forme suivante :

    {
	  "id": 1,
	  "name": "mozzarella"
	}
	
Lors de la création, l'identifiant n'est pas connu. Aussi on aura une
représentation JSON qui comporte uniquement le nom :

	{ "name": "mozzarella" }
	
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

        // Vérification de la valeur de retour (200)
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	  
	    // Vérification de la valeur retournée (liste vide)
	    List<IngredientDto> ingredients;
        ingredients = response.readEntity(new GenericType<List<IngredientDto>>(){});

        assertEquals(0, ingredients.size());

      }

A ce stade, vous pouvez lancer un premier test au moyen de la commande
`mvn test`. Évidemment, ce test va échouer.

    $ mvn test
	Results :

    Failed tests:   testGetEmptyList(fr.ulille.iut.pizzaland.IngredientResourceTest): expected:<200> but was:<404>

    Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
	
Vous pouvez compiler votre code et les tests sans les lancer au moyen
des commandes `mvn compile` et `test-compile`.

Pour réussir, ce premier test, nous allons mettre en place la
ressource `Ingrédient` dans la classe `IngredientResource` ainsi que
le DTO (Data Transfer Object) qui représentera les données
transportées dans les requêtes et réponses HTTP.

Une première implémentation de la ressource pourra avoir la forme
suivante :

    @Path("/ingredients")
    public class IngredientResource {

    @Context
    public UriInfo uriInfo;

    public IngredientResource() {
    }

    @GET
    public List<IngredientDto> getAll() {

        return new ArrayList<IngredientDto>();
    }
}

Pour réussir ce premier test, on se contentera de l'implémentation
suivante du DTO :

	package fr.ulille.iut.pizzaland.dto;

	public class IngredientDto {

	  public IngredientDto() {
        
      }
	}
	
Avec cette première implémentation, on va pouvoir tester notre
ressource : 

    $ mvn test
	
	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

### Récupérer un ingrédient existant
Nous allons continuer en ajoutant la possibilité de récupérer un
ingrédient particulier à partir de son identifiant.
Pour cela voici un premier test qui permettra de vérifier cela :
	 
	 @Test
     public void testGetExistingIngredient() {
       IngredientDto ingredient = new IngredientDto()
	   ingredient.setId(1);
	   ingredient.setName("mozzarella");
	 
	   Response response = target("/ingredients/1).request().get();
       assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

       IngredienDto result = response.readEntity(IngredientDto.class);
       assertEquals(ingredient, result);
    }

Vous pourrez vérifier que le test échoue au moyen de la commande `mvn test`

Afin de réussir ce test, nous devons compléter la classe IngredientDto
avec les getter/setter correspondant aux propriétés de l'object JSON.

    public class IngredientDto {
	  private long id;
	  private String nom;
	
	  public IngredientDto() {}
	
      public long getId() {
	    return id;
	  }
	
	  public void setId(long id) {
	    this.id = id;
	  }
	
	  public void setNom(String nom) {
	    this.nom = nom;
      }
	
      public String getNom() {
		return nom;
      }
	}

Du côté de la ressource, on peut fournir une première implémentation :

    @GET
    @Path("{id}")
    public IngredientDto getOneIngredient(@PathParam("id") long id) {
	  Ingredient ingredient = new Ingredient()
	  ingredient.setId(1);
	  ingredient.setName("mozzarella");
	  
	  return Ingredient.toDto(ingredient);
    }

Pour cette méthode, nous avons introduit la classe `Ingredient`. Ce
Java Bean représente un ingrédient manipulé par la ressource.
Voici une implémentation pour cette classe :

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
	

Le test devrait maintenant réussir :

    $ mvn test
	
## Introduction de la persistence
Pour aller plus loin et mettre en place la création des ingrédients il
va falloir introduire la persistence. Pour cela, nous allons utiliser
la librairie JDBI qui permet d'associer un modèle objet aux tables de
base de données.

Pour cela nous allons devoir implémenter un DAO (Data Access Object) :


	package fr.ulille.iut.pizzaland.dao;

	import java.util.List;

	import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
	import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
	import org.jdbi.v3.sqlobject.statement.SqlQuery;
	import org.jdbi.v3.sqlobject.statement.SqlUpdate;

	import fr.ulille.iut.pizzaland.beans.Ingredient;

	public interface IngredientDao {

      @SqlUpdate("CREATE TABLE IF NOT EXISTS ingredients (id INTEGER PRIMARY KEY, name VARCHAR UNIQUE NOT NULL)")
      void createTable();

      @SqlUpdate("DROP TABLE IF EXISTS ingredients")
      void dropTable();

      @SqlUpdate("INSERT INTO ingredients (name) VALUES (:name)")
      @GetGeneratedKeys
      long insert(String name);

      @SqlQuery("SELECT * FROM ingredients")
      @RegisterBeanMapper(Ingredient.class)
      List<Ingredient> getAll();

      @SqlQuery("SELECT * FROM ingredients WHERE id = :id")
      @RegisterBeanMapper(Ingredient.class)
      Ingredient findById(long id);
}
