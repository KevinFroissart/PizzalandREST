# Développement REST avec Jersey

## Mise en plance de l'environnement
Ce tutoriel utilise [Apache Maven](http://maven.apache.org/) pour
l'automatisation des tâches de développement (compilation, tests,
déploiement...).

Si votre machine se trouve derrière un
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
Pour récupérer le projet vous pouvez utiliser la commande `git clone
https://gitlab.univ-lille.fr/yvan.peter/m4102_tp3.git`

L'arborescence ci-dessous vous montre le contenu du projet qui vous
servira de point de départ. Maven est configuré grâce au fichier
`pom.xml` qui permet entre autre de spécifier les dépendances du
projet.

La classe `ApiV1` sera le point d'entrée de notre application REST qui
permet de configurer le chemin de l'URI (`@ApplicationPath`) ainsi que
les paquetages Java qui contiennent les ressources.
	
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

## Développement d'une ressource *ingredients*

### API et représentation des données

Nous pouvons tout d'abord réfléchir à l'API REST que nous allons offrir pour la ressource *ingredients*. Celle-ci devrait répondre aux URI suivantes :

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

### Architecture logicielle de la solution

La figure ci-dessous présente l'architecture globale qui devra être
mise en place pour notre développement :

![Architecture de la solution](architecture.svg "Architecture")

#### JavaBeans
JavaBean est un standard pour les objets Java permettant de les créer
et de les initialiser et de les manipuler facilement. Pour cela ces
objets doivent respecter un ensemble de conventions :

  - la classe est sérialisable
  - elle fournit au moins un constructeur vide
  - les attributs privés de la classe sont manipulables via des
    méthodes publiques **get**_Attribut_ et **set**_Attribut

Les DTO et la classe `Ingredient`décrits dans la suite sont des
JavaBeans.

#### Data Transfer Object (DTO)
Les DTO correspondent à la représentation des données qui sera
transportée par HTTP. Ce sont des Javabeans qui possèdent les même
propriétés que la représentation (avec les getter/setter
correspondants).

Jersey utilisera les *setter* pour initialiser l'objet à partir
de la représentation JSON ou XML et les *getter* pour créer la
représentation correspondante.

#### Data Access Object (DAO)
Le DAO permet de faire le lien entre la représentation objet et le
contenu de la base de données.

Nous utiliserons la [librairie JDBI](http://jdbi.org/) qui permet
d'associer une interface à des requêtes SQL.
La classe `BDDFactory` qui vous est fournie permet un accès facilité
aux fonctionnalités de JDBI.

#### La représentation des données manipulées par la ressource
La classe `Ingredient` est un JavaBean qui représente ce qu'est un
ingrédient. Elle porte des méthodes pour passer de cette
représentation aux DTO.

Cela permet de découpler l'implémentation de la ressource qui traite
les requêtes HTTP et la donnée manipulée.

Cette classe pourrait
porter des comportements liés à cette donnée (par ex. calcul de TVA).

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

En héritant de JerseyTest, votre classe de test se comporte comme un
[`Client`
JAX-RS](https://docs.oracle.com/javaee/7/api/javax/ws/rs/client/Client.html). La
méthode `target()` notamment permet de préparer une requête sur une
URI particulière.


Vous pouvez compiler votre code ainsi que les tests au moyen
des commandes `mvn compile` et `mvn test-compile`. La compilation du
code et des tests se fera automatiquement si nécessaire quand vous
faites un `mvn test`.

Pour pouvoir compiler ce premier test, il faut au minimum fournir le
DTO `IngredientDto`.
Pour commencer,  on se contentera de l'implémentation minimale
suivante :

	package fr.ulille.iut.pizzaland.dto;

	public class IngredientDto {

	  public IngredientDto() {
        
      }
	}

A ce stade, vous pouvez lancer un premier test au moyen de la commande
`mvn test`. Évidemment, ce test va échouer.

    $ mvn test
	Results :

    Failed tests:   testGetEmptyList(fr.ulille.iut.pizzaland.IngredientResourceTest): expected:<200> but was:<404>

    Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
	
Pour réussir, ce premier test, nous allons mettre en place la
ressource `IngredientResource`.

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
	
Avec cette première implémentation, on va pouvoir tester notre
ressource : 

    $ mvn test
	
	Results :

	Tests run: 1, Failures: 0, Errors: 0, Skipped: 0

### Récupérer un ingrédient existant
Nous allons continuer en ajoutant la possibilité de récupérer un
ingrédient particulier à partir de son identifiant.
Pour cela voici un premier test qui permettra de vérifier cela :
	 
	 import fr.ulille.iut.pizzaland.beans.Ingredient;
	 
	 @Test
     public void testGetExistingIngredient() {
       Ingredient ingredient = new Ingredient();
	   ingredient.setId(1);
	   ingredient.setName("mozzarella");
	 
	   Response response = target("/ingredients/1").request().get();
       assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

       Ingredient result  = Ingredient.fromDto(response.readEntity(IngredientDto.class));
       assertEquals(ingredient, result);
    }

Vous pourrez vérifier que le test échoue au moyen de la commande `mvn test`

Afin de réussir ce test, nous devons compléter la classe IngredientDto
avec les getter/setter correspondant aux propriétés de l'object JSON.

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

Du côté de la ressource, on peut fournir une première implémentation :

    import javax.ws.rs.PathParam;
	import fr.ulille.iut.pizzaland.beans.Ingredient;
	
    @GET
    @Path("{id}")
    public IngredientDto getOneIngredient(@PathParam("id") long id) {
	  Ingredient ingredient = new Ingredient();
	  ingredient.setId(1);
	  ingredient.setName("mozzarella");
	  
	  return Ingredient.toDto(ingredient);
    }

Pour cette méthode, nous avons introduit la classe `Ingredient`. Ce
JavaBean représente un ingrédient manipulé par la ressource.
Voici une implémentation pour cette classe :

	package fr.ulille.iut.pizzaland.beans;
	
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
