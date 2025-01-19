# heig-vd-dai-projet-pratique3
### Chollet Florian - Delétraz Alexandre

## Introduction
L'objectif de ce troisième laboratoire est de créer une application web qui utilise le protocole HTTP.  
L'application sera définie par une API (application protocol interface) qui fonctionnera sur une machine virtuelle qui 
doit faire office de serveur. L'application doit être accessible en utilisant le nom de domaine.  
L'interaction avec notre application doit pouvoir être faite soit avec un outil de ligne de commande (comme curl), soit 
directement sur le navigateur internet.  
  
L'application que nous avons décidé d'implémenter permet de stocker et consulter des notes de plusieurs matières et pour 
plusieurs utilisateurs.  
Il est aussi possible de faire les opérations suivantes :
- Pour un utilisateur spécifique :
  - calculer la moyenne d'une branche
  - calculer la moyenne générale
  - ajouter une branche dans une matière
  - supprimer une note
  - modifier une note
- En utilisation globale :
  - calculer la moyenne de tous les élèves d'une branche
  - calculer la moyenne générale de tous les élèves.

## Implémentation
Nous avons décidé de stocker les données de tous les élèves dans un seul fichier json. Ce choix nous permet de simplifier  
notre implémentation, mais pourrait causer des problèmes de performance si le fichier devient très grand. Étant donné 
que nous n'aurons probablement jamais assez d'élève pour ralentir une machine moderne, ce nous choix nous semble le plus 
adapter pour ce laboratoire.  
  
Pour ce qui est des librairies que nous utilisons, pour le web, nous nous servons de javalin, car c'est la librairie que 
nous avons vue durant les cours. 
Tout notre code est dans le fichier _Main_ du projet.  
  

Tout d'abord, nous initialisons le _Javalin app_ avec la méthode _Javalin.create()_, afin de pouvoir utiliser les 
méthodes de _Javalin_ en utilisant le port 5000.  
Dans la première partie du code, la page d'accueil est créé avec un message de bienvenue. Le message indiquant de cliquer 
sur _ici_ permet d'accéder à la page de connexion.
Sur cette seconde page de connexion, l'utilisateur est invité à se connecter en entrant son nom d'utilisateur. 
Nous avons prévu d'afficher un message d'erreur au cas où quelque chose ne se passerait pas comme prévu.  
À ce stade, les erreurs qui sont gérées sont :  
- l'erreur 404, si la page n'est pas trouvé
- l'erreur 403, si l'utilisateur n'a pas accès à la ressource demandée
- l'erreur 500, si une erreur interne au serveur survient.  
- l'erreur 401, au cas où l'utilisateur essaye d'accéder à une ressource sans être connecté.
  
La page de connexion _POST_ est ensuite créée, cette fois-ci avec la méthode _app.post()_. Cette partie permet de récupérer
le nom d'utilisateur, de créer un cookie et de rédiriger sur la page de note correspondante. Le cookie et la redirection 
se font uniquement si le nom d'utilisateur existe et s'il est valide. Autrement, un massage d'erreur s'affiche à l'écran. 
De plus, si l'utilisateur est déjà connecté depuis un autre appareil, un message sera affiché à l'écran. Cette vérification 
est possible, car nous utilisons une _HashMap_ appelée _session_. Si la connexion à pu être établie, la _HashMap_ est mise 
à jour, afin que l'utilisateur soit désormais notifié comme connecté.
  
Ensuite, si tout se passe bien, l'utilisateur accède à la page des notes.  
L'affichage des notes se fait grâce à la méthode _app.get()_, qui va chercher les informations au _path_ _/notes"_. 
Cette recherche ne se fait pas sans condition. Premièrement, le cookie associé à l'utilisateur est récupéré avec 
_ctx.cookie()_. Si aucun cookie n'est trouvé, l'utilisateur est redirigé sur la page de login, avec la méthode 
_ctx.redirect("/login")_. Si le cookie est présent, un objet de type _ObjectMapper_ est créé, afin de lire le _json_. 
Puis, une variable de type _file_ est créée et initialisée avec le fichier _json_ souhaité. L'existence du fichier est 
vérifiée avec la méthode _exists()_. Si le fichier n'existe pas, un message indiquant ce problème s'affiche, et un 
_return_ est fait.
Une fois que le fichier est ouvert, un _InputStream_ est initialisé, afin d'utiliser le fichier ouvert comme flux 
d'entrée. Ensuite, un _JsonNode_ appelé _rootNode_, est initialisé en lisant le flux d'entrée et en utilisant la méthode
_mapper.readTree()_, qui nous permet d'avoir une vue en arbre du _json_.  

Une deuxième vue en arbre est faite, qui sera utile pour chaque utilisateur. Ce deuxième arbre n'est pas initialisé en 
utilisant le fichier _json_, comme pour l'arbre précédent, mais en utilisant la méthode _get()_ sur le premier _rootNode_ 
avec le nom de l'utilisateur. À partir de ce moment, les informations d'un utilisateur sont récupérée. Si ce _JsonNode_ 
contient ne contient rien, cela signifie que l'utilisateur n'existe pas. Un message est alors affiché, indiquant le 
problème, et l'utilisateur est redirigé à la page d'accueil. Si l'utilisateur existe, en partant de son arbre, il est 
désormais possible de récupérer ses notes en faisant un nouveau _JsonNode_, qui contient cette fois les notes de 
l'utilisateur avec la méthode _get("notes")_ sur l'arbre.  
Le code suivant vient de la documentation Jackson, et permet de regrouper et récupérer les notes en parcourant le 
fichier _json_.  

Afin de regrouper les notes par branches, l'initialisation d'un type _Map_, appelé _notesParBranche_ est effectuée. 
Cette _Map_ met en relation un _String_ et un _StringBuilder_. Ensuite, l'initialisation d'un 
_Iterator <Map.Entry<String, JsonNode>>_, démarrant sur le premier champ _fields_ des notes.  
Ensuite, une boucle qui continue de tourner tant qu'il existe un _fields_ suivant. Dans cette boucle, les informations 
sur chaque branche et note par branche sont récupérée. De plus, si le nom de la branche n'est pas encore présent, il est 
ajouté.  

Suite à cette boucle, il est nécessaire de construire l'affichage, ce qui est fait en initialisant un _StringBuilder_ 
qui est mis à jour dans une boucle _for_ avec des méthodes _append()_. Pour rappel, l'utilisation des _StringBuilder_ 
permet d'optimiser la gestion de _String_ dans les boucles (dans le cas d'un _String_, l'objet est recréé à chaque fois, 
ce qui n'est pas le cas avec un _StringBuilder).  
  
Ensuite, le résultat de la création de l'affichage est affiché avec la ligne _ctx.result(result.toString());_.

## Utilisation du projet
Afin d'utiliser correctement le projet, il faudra en premier se connecter sur la page web suivante :
https://app.projetdai3florian.duckdns.org/

Une fois là-bas, nous pouvons nous connecter avec un user (par exemple Florian), puis voir les notes ou la moyenne d'une branche.

Il est égualement possible de faire cela avec curl :

Pour se log :
```
curl -X POST https://app.projetdai3florian.duckdns.org/login \
-d "username=<nomUser>"
```


Pour se delog :
```
curl -X GET https://app.projetdai3florian.duckdns.org/logout \
--cookie username=<nomUser>

```
Pour toutes les prochaines commandes, il faudra être connecter (sinon, vous recevrez un message d'erreur)

Pour voir les notes :
```
curl -X GET https://app.projetdai3florian.duckdns.org/notes \
--cookie username=<nomUser>
```

Pour ajouter une note :
```
curl -X POST https://app.projetdai3florian.duckdns.org/notes \
--cookie username=<nomUser> \
-d "branch=<nomBranch>" \
-d "nom=<nomTest>" \
-d "note=<valeurNote>"
```

Pour supprimer une note :
```
curl -X DELETE https://app.projetdai3florian.duckdns.org/notes \
--cookie username=Alexandre \
-d "branch=<nomBranch>" \
-d "nom=<nomTest>"
```

Pour modifier une note :
```
curl -X PUT https://app.projetdai3florian.duckdns.org/notes \
--cookie username=Alexandre \
-d "branch=<nomBranch>" \
-d "nom=<nomTest>" \
-d "note=<valeurNote>"
```

## Récupération du projet en local et configuration pour internet

### Récupération du projet
Afin de récupérer le projet depuis gitHub, il faut se rendre sur notre repo git à l'adresse suivante :  
[lien_repo](https://github.com/luma2010/heig-vd-dai-projet-pratique3)  
  
Depuis cette page, il vous suffit de cliquer sur le bouton vert, de copier le lien https ou ssh, de vous rendre à 
l'endroit souhaité sur votre machine et de faire la commande git clone avec le lien que vous avez copié, dans votre 
terminal.  
  
Une fois que vous avez le projet localement, depuis l'ide de votre choix, il vous faudra le compiler avec maven.  
Si vous utilisez IntelliJ de jet Brains, vous pouvez ajouter une configuration avec la commande suivante :  

```text
dependency:go-offline clean compile package  
```  
Puis avec la commande ci-dessous, afin de lancer le fichier .jar    
```text
java -jar target/heig-vd-dai-projet-pratique3.jar
```  

### Docker
Afin de faire fonctionner le projet sur docker, il vous faudra suivre les étapes du docker file qui se trouve dans 
le _main_.
Premièrement, vous allez devoir créer le docker avec la commande ci-après :  

```text
docker built -t nomDeImage .
```
Cette commande permet de créer un container docker dans lequel le fichier jar du projet va pouvoir fonctionner.


Ensuite, avec la commande suivante :  

```text
docker push nomDeImage
```

### Obtenir un nom de domaine
Pour obtenir un nom de domaine, nous avons utiliser duckDNS, qui fourni un nom de domaine en se connectant simplement sur leur site.
Une fois connecter, il suffi de donner un nom de domaine que l'on souhaite. Une fois ceci fait, duckDNS nous donnera un token qu'il faudra rentrer dans le dns-challenge.env

### Création de la VM
Pour le projet, nous avons utiliser Azure et nous avons suivis le practical content ssh et scp afin de crée la VM.

### Docker-compose

Afin de crée un site web fonctionnel avec notre code, il suffit de faire 3 fichier :
- docker-compose.yaml : va crée les 2 containers (traefik et l'application) avec les options du fichier
- .env : permet de faire un fichier "générique" du .yaml puis de spécifier ce que l'on souhaite dans le .env (exemple, quel image on veut utilisé)
- dns-challenge.env : permet de spécifier comment traefik va utiliser le nom de domaine (en fonction du fourniseur DNS, ce fichier changera)

Ces trois fichier sont présent dans le git, il suffis juste de faire la commande suivante sur la VM afin de lancer le programme :
```
docker-compose up
```


## Conclusion
Ce travail nous a permis de pratiquer le développement d'une application web simple, ce qui nous a permis de mieux nous 
rendre compte de tous les détails qu'il ne faut pas omettre lors de la création d'une API. Que ce soit, l'authentification 
d'un utilisateur, la création d'une session, la concurrence, la gestion des requêtes, rien ne doit être laissé au hasard.  
Nous n'avons pas fait d'interface graphique avec du CSS, car nous n'avions pas le temps d'acquérir les connaissances pour 
faire quelque chose d'esthétique.  
Si un jour, nous reprenons ce projet, les améliorations que nous pourrions apporter sont : 
- une interface plus agréable
- la possibilité d'avoir la moyenne de l'année sur la page web
- pouvoir ajouter des notes directement depuis l'interface web
- et encore beaucoup d'autre

Nous sommes toutefois assez satisfait du résultat de ce projet.  
Nous vous remercions pour votre enseignement durant ce semestre et vous souhaitons une excellente continuation.