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

En local, vous pouvez également recopier le fichier ~/home/public/peter/maven/settings.xml~.

** Récupération du projet initial

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
