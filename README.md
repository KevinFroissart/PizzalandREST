# Développement REST avec Jersey

## Mise en place de l'environnement
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

Pour cela nous allons devoir implémenter le DAO (Data Access Object) `IngredientDao` :

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

JDBI fonctionne par annotations :
  - Les annotations `sqlUpdate` et `SqlQuery` correspondent à des
  requêtes SQL en modification ou non.
  - `@GetGeneratedKeys` permet de renvoyer la clé primaire générée par
  la base de données.
  - `@RegisterBeanMapper` permet d'associer une classe à un résultat
  (les champs de la table sont associés aux propriétés du bean).
  
Reprenons maintenant le code déjà écrit pour aller chercher les
ingrédients dans une base de données (nous utiliserons `Sqlite`).

### Les tests avec la base de données
Nous allons utiliser le DAO pour insérer des données dans la table
afin de réaliser nos tests. Nous utiliserons une base de données de
tests qui est définie via la classe `BDDFactory`.

Les méthodes `setEnvUp` et `tearEnvDown` permettent de créer et
détruire la base de données entre chaque test.
	
	import fr.ulille.iut.pizzaland.dao.IngredientDao;
	
	public class IngredientResourceTest extends JerseyTest {
	  private IngredientDao dao;
	  
	  @Override
      protected Application configure() {
       BDDFactory.setJdbiForTests();

       return new ApiV1();
    }
	
	@Before
    public void setEnvUp() {
        dao = BDDFactory.buildDao(IngredientDao.class);
        dao.createTable();
    }

    @After
    public void tearEnvDown() throws Exception {
       dao.dropTable();
    }
	
	@Test
    public void testGetExistingIngredient() {

        Ingredient ingredient = new Ingredient();
        ingredient.setName("mozzarella");

	    long id = dao.insert(ingredient.getName());
        ingredient.setId(id);

        Response response = target("/ingredients/" + id).request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Ingredient result = Ingredient.fromDto(response.readEntity(IngredientDto.class));
        assertEquals(ingredient, result);
    }

### La ressource avec la base de données

	import fr.ulille.iut.pizzaland.BDDFactory;
	import fr.ulille.iut.pizzaland.dao.IngredientDao;
	
	import java.util.stream.Collectors;
	
	import javax.ws.rs.WebApplicationException;
	
	public class IngredientResource {
	  private IngredientDao ingredients;
	  
	  public IngredientResource() {
        ingredients = BDDFactory.buildDao(IngredientDao.class);
        ingredients.createTable();
      }
	  
	  @GET
      public List<IngredientDto> getAll() {
        LOGGER.info("IngredientResource:getAll");

        List<IngredientDto> l = ingredients.getAll().stream().map(Ingredient::toDto).collect(Collectors.toList());
        return l;
	  }

	  @GET
      @Path("{id}")
      public IngredientDto getOneIngredient(@PathParam("id") long id) {
        LOGGER.info("getOneIngredient(" + id + ")");
        try {
            Ingredient ingredient = ingredients.findById(id);
			return Ingredient.toDto(ingredient);
        }
        catch ( Exception e ) {
			// Cette exception générera une réponse avec une erreur 404
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
      }
	  
	}

### Les tests fonctionnent avec la base de données
Nous pouvons maintenant vérifier que la base fonctionne avec la base
de données :

	$ mvn test
	
	Results :

	Tests run: 2, Failures: 0, Errors: 0, Skipped: 0

## Complétons maintenant les différents tests
L'implémentation de la classe devrait fonctionner avec le test suivant
:


	@Test
    public void testGetNotExistingPizza() {
	  Response response = target("/pizzas/125").request().get();
      assertEquals(Response.Status.NOT_FOUND.getStatusCode(),response.getStatus());
    }


	$ mvn test
	
	Results :

	Tests run: 3, Failures: 0, Errors: 0, Skipped: 0

### Implementation de la méthode POST
Il va falloir implémenter la méthode POST pour la création des
ingrédients. Commençons par les différents tests : création, création
de deux ingrédients identiques et création d'ingrédient sans nom.

	import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;

	@Test
    public void testCreateIngredient() {
        IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();
        ingredientCreateDto.setName("mozzarella");

        Response response = target("/ingredients")
                .request()
                .post(Entity.json(ingredientCreateDto));

	    // On vérifie le code de status à 201
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        IngredientDto returnedEntity = response.readEntity(IngredientDto.class);

        // On vérifie que le champ d'entête Location correspond à
        // l'URI de la nouvelle entité
        assertEquals(target("/ingredients/" +
			returnedEntity.getId()).getUri(), response.getLocation());
		
		// On vérifie que le nom correspond
        assertEquals(returnedEntity.getName(), ingredientCreateDto.getName());
    }
		
	@Test
    public void testCreateSameIngredient() {
        IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();
        ingredientCreateDto.setName("mozzarella");
        dao.insert(ingredientCreateDto.getName());

        Response response = target("/ingredients")
                .request()
                .post(Entity.json(ingredientCreateDto));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCreateIngredientWithoutName() {
        IngredientCreateDto ingredientCreateDto = new IngredientCreateDto();

        Response response = target("/ingredients")
                .request()
                .post(Entity.json(ingredientCreateDto));

        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), response.getStatus());
    }

Nous utiliserons un DTO spécifique `IngredientCreateDto` dans la
mesure où nous n'aurons que le nom de l'ingrédient pour la création.

La classe [`javax.ws.rs.client.Entity<T>`](https://docs.oracle.com/javaee/7/api/javax/ws/rs/client/Entity.html) permet de définir le corps de
la requête POST et le type de données associée (ici `application/json`).

Nous devons également fournir une implémentation de
`IngredientCreateDto` pour pouvoir compiler notre code :

	package fr.ulille.iut.pizzaland.dto;
	
	public class IngredientCreateDto {
		private String name;
		
		public IngredientCreateDto() {}
		
		public void setName(String name) {
			this.name = name;
 		}
 		
		public String getName() {
 			return name;
 		}
	}

Nous pouvons maintenant compiler notre code de test et constater que
ceux-ci échouent.

	$ mvn test

	Results :

	Failed tests:   testCreateSameIngredient(fr.ulille.iut.pizzaland.IngredientResourceTest): expected:<409> but was:<405>
		testCreateIngredientWithoutName(fr.ulille.iut.pizzaland.IngredientResourceTest): expected:<406> but was:<405>
		testCreateIngredient(fr.ulille.iut.pizzaland.IngredientResourceTest): expected:<201> but was:<405>
	
	Tests run: 6, Failures: 3, Errors: 0, Skipped: 0

Nous pouvons maintenant implémenter notre méthode POST dans la
	ressource :
	
	import javax.ws.rs.POST;
	
	import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;
	
	@POST
    public Response createIngredient(IngredientCreateDto ingredientCreateDto) {
        Ingredient existing = ingredients.findByName(ingredientCreateDto.getName());
        if ( existing != null ) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        
        try {
            Ingredient ingredient = Ingredient.fromIngredientCreateDto(ingredientCreateDto);
            long id = ingredients.insert(ingredient.getName());
            ingredient.setId(id);
            IngredientDto ingredientDto = Ingredient.toDto(ingredient);

            URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

            return Response.created(uri).entity(ingredientDto).build();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
    }

Comme nous vérifions qu'il n'y a pas déjà un ingrédient avec le nom
fourni, nous devont ajouter une méthode `findbyName` à notre DAO

	@SqlQuery("SELECT * FROM ingredients WHERE name = :name")
    @RegisterBeanMapper(Ingredient.class)
    Ingredient findByName(String name);

Nous avons également besoin de rajouter les méthodes de conversion
	pour ce DTO à notre bean `Ingredient` :
	
	import fr.ulille.iut.pizzaland.dto.IngredientCreateDto;
		
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

Nous pouvons maintenant vérifier nos tests :

	$ mvn test
	
	Results :
	
	Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

Vous aurez peut-être un affichage d'exception liée au test de création
de doublon, toutefois le test est réussi puisqu'il a levé une
exception qui a été traduite par un code d'erreur HTTP 406.

### Implémentation de la méthode DELETE
Les tests liés à la méthode DELETE sont les suivants :

	@Test
    public void testDeleteExistingIngredient() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("mozzarella");
        long id = dao.insert(ingredient.getName());
        ingredient.setId(id);

        Response response = target("/ingredients/" + id).request().delete();

        assertEquals(Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
	
	    Ingredient result = dao.findById(id);
		assertEquals(result, null);
	}

    @Test
    public void testDeleteNotExistingIngredient() {
        Response response = target("/ingredients/125").request().delete();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
		response.getStatus());
	}
	
Après avoir constaté que ces tests échouent, nous pouvons fournir une
implémentation pour la méthode DELETE :

	import javax.ws.rs.DELETE;
	
	@DELETE
    @Path("{id}")
    public Response deleteIngredient(@PathParam("id") long id) {
		if ( ingredients.findById(id) == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

	    ingredients.remove(id);

	    return Response.status(Response.Status.ACCEPTED).build();
    }

Nous devons également implémenter la méthode remove dans
`IngredientDao` :

	@SqlUpdate("DELETE FROM ingredients WHERE id = :id")
	void remove(long id);

Avec cette implémentation, nos tests réussissent.

### Implémentation de la méthode GET pour récupérer le nom de l'ingrédient
Commençons par les tests correspondant à cette URI (GET
/ingredients/{id}/name)

	@Test
    public void testGetIngredientName() {
        Ingredient ingredient = new Ingredient();
        ingredient.setName("mozzarella");
        long id = dao.insert(ingredient.getName());

        Response response = target("ingredients/" + id + "/name").request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        assertEquals("mozzarella", response.readEntity(String.class));
    }

    @Test
    public void testGetNotExistingIngredientName() {
        Response response = target("ingredients/125/name").request().get();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

L'implémentation correspondant à ce test est simple :

	@GET
    @Path("{id}/name")
    public String getIngredientName(@PathParam("id") long id) {
	    Ingredient ingredient = ingredients.findById(id);
		if ( ingredient == null ) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
            
		return ingredient.getName();
    }

### Implémentation d'une méthode de création avec des données de formulaire
La création d'un ingrédient pourrait également se faire via un
formulaire Web. Dans ce cas, le type de représentation sera
`application/x-www-form-urlencoded`. 

On peut déjà préparer un test pour cette méthode de création :

	import javax.ws.rs.core.MediaType;
	import javax.ws.rs.core.Form;

	import static org.junit.Assert.assertNotNull;

	@Test
    public void testCreateWithForm() {
        Form form = new Form();
        form.param("name", "chorizo");

        Entity<Form> formEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        Response response = target("ingredients").request().post(formEntity);
        
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        String location = response.getHeaderString("Location");
        long id = Integer.parseInt(location.substring(location.lastIndexOf('/') + 1));
        Ingredient result = dao.findById(id);
		
        assertNotNull(result);
    }

On peut maintenant fournir une implémentation pour cette méthode :

	@POST
    @Consumes("application/x-www-form-urlencoded")
    public Response createIngredient(@FormParam("name") String name) {
        Ingredient existing = ingredients.findByName(name);
        if ( existing != null ) {
            throw new WebApplicationException(Response.Status.CONFLICT);
        }
        
        try {
            Ingredient ingredient = new Ingredient();
            ingredient.setName(name);
            
            long id = ingredients.insert(ingredient.getName());
            ingredient.setId(id);
            IngredientDto ingredientDto = Ingredient.toDto(ingredient);

            URI uri = uriInfo.getAbsolutePathBuilder().path("" + id).build();

            return Response.created(uri).entity(ingredientDto).build();
        }
        catch ( Exception e ) {
            e.printStackTrace();
            throw new WebApplicationException(Response.Status.NOT_ACCEPTABLE);
        }
    }
	

# Créer une base de données de test
Nous avons maintenant implémenté et testé toutes les méthodes prévues
par notre API. Si nous voulons tester avec des clients, il serait bien
d'avoir quelques ingrédients dans la base de données. Pour cela, nous
allons donner la possibilité de créer des ingrédients au démarrage sur la base
d'une variable d'environnement : `PIZZAENV`.

Quand cette variable aura la valeur `withdb`, nous allons remplir la
base au démarrage avec le code suivant :

	import fr.ulille.iut.pizzaland.beans.Ingredient;
	import fr.ulille.iut.pizzaland.dao.IngredientDao;

	import java.io.FileReader;
	import java.io.IOException;
	import java.nio.file.Paths;
	import java.util.ArrayList;
	import java.util.List;

	import javax.json.bind.Jsonb;
	import javax.json.bind.JsonbBuilder;

	@ApplicationPath("api/v1/")
	public class ApiV1 extends ResourceConfig {
	
	  public ApiV1() {
	    packages("fr.ulille.iut.pizzaland");
	
        String environment = System.getenv("PIZZAENV");

	    if ( environment != null && environment.equals("withdb") ) {
		  LOGGER.info("Loading with database");
          Jsonb jsonb = JsonbBuilder.create();
          try {
		  	FileReader reader = new FileReader( getClass().getClassLoader().getResource("ingredients.json").getFile() );
            List<Ingredient> ingredients = JsonbBuilder.create().fromJson(reader, new ArrayList<Ingredient>(){}.getClass().getGenericSuperclass());
                
            IngredientDao ingredientDao = BDDFactory.buildDao(IngredientDao.class);
            ingredientDao.dropTable();
            ingredientDao.createTable();
            for ( Ingredient ingredient: ingredients) {
                 ingredientDao.insert(ingredient.getName());              
            }
		  } catch ( Exception ex ) {
			throw new IllegalStateException(ex);
          }
        } 
      }
    }
Dans un terminal, nous pouvons maintenant fixer la variable
d'environnemnet et démarrer notre serveur REST au moyen de la
commande `mvn jetty:run` :
	
	$ export PIZZAENV=withdb
	$ mvn jetty:run
	
Dans un autre terminal, nous pouvons utiliser `curl` pour tester nos
différentes méthodes :

	$ curl -i localhost:8080/api/v1/ingredients
	
	HTTP/1.1 200 OK
	Date: Sun, 09 Feb 2020 22:08:05 GMT
	Content-Type: application/json
	Content-Length: 319
	Server: Jetty(9.4.26.v20200117)

	[{"id":1,"name":"mozzarella"},{"id":2,"name":"jambon"},{"id":3,"name":"champignons"},{"id":4,"name":"olives"},{"id":5,"name":"tomate"},{"id":6,"name":"merguez"},{"id":7,"name":"lardons"},{"id":8,"name":"fromage"},{"id":9,"name":"oeuf"},{"id":10,"name":"poivrons"},{"id":11,"name":"ananas"},{"id":12,"name":"reblochon"}]

# Implémentation de la ressource Pizza
Une pizza comprend des ingrédients. Pour développer cette ressource,
vous aurez donc besoin d'un table d'association au niveau de la base
de données. Cela pourra être géré au niveau du DAO grâce à
[https://jdbi.org/#_default_methods](JDBI). Cet extrait de code montre
comment faire :

	public interface PizzaDao {
	
      @SqlUpdate("CREATE TABLE IF NOT EXISTS Pizzas ....")
      void createPizzaTable();

      @SqlUpdate("CREATE TABLE IF NOT EXISTS PizzaIngredientsAssociation .....")
      void createAssociationTable();

      default void createTableAndIngredientAssociation() {
        createAssociationTable();
        createPizzaTable();
    }

Vous écrivez les différentes méthodes annotées avec `@SqlUpdate` ou
`@SqlQuery`. Vous utilisez ensuite ces méthodes au sein d'une méthode
ayant le mot clé `default`. C'est cette méthode que vous utiliserez
dans votre ressource.
