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


## Conclusion