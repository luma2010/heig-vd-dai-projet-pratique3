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
méthodes de _Javalin_.  
Avec _app.get()_, la page d'accueil est créé avec un message de bienvenue.  
Puis, toujours avec _app.get()_, la page de connexion _GET_ est créée. Elle contient un formulaire permettant de login de 
l'utilisateur, en demandant son nom d'utilisateur. Lorsque l'on soumet ce formulaire, cette information est transmise au 
serveur.  
  
La page de connexion _POST_ est ensuite créée, cette fois-ci avec la méthode _app.post()_. Cette partie permet d'entrer 
le nom d'utilisateur, de créer un cookie et de rédiriger sur la page de note correspondante. Le cookie et la redirection 
se font uniquement si le nom d'utilisateur existe et s'il est valide. Autrement, un massage d'erreur s'affiche à l'écran.  
  
L'affichage des notes se fait grâce à la méthode _app.get()_, qui va chercher les informations au _path_ _/notes"_. 
Cette recherche ne se fait pas sans condition. Premièrement, le cookie associé à l'utilisateur est récupéré avec 
_ctx.cookie()_. Si aucun cookie n'est trouvé, l'utilisateur est redirigé sur la page de login, avec la méthode 
_ctx.redirect("/login")_. Si le cookie est présent, un objet de type _ObjectMapper_ est créé, afin de lire le _JSON_. 
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
  


## Conclusion