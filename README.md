# StatCraft

Récupération des statistiques de joueurs Minecraft et affichage sous différentes formes.
## Plusieurs cas d'usage
### 1. Mode de jeu avec reset
L'outil récupère toute les 30 secondes les statistiques de tout les joueurs. Pour le Hardcore on récupère les
données les plus intéressante, monstres tués, dégâts reçus, certains minerais minés, fondus, etc.
Le script génère automatiquement des graphs représentant chaque joueurs ou un graph avec tout les
joueurs pour les comparer.
On peut aussi définir un "score" global en fonction des ces stats en pondant un petite équation.
Le script pourrait envoyer dans un channel discord des png des graphs générés.
### 2. Mode de jeu classique
L'outil récupère toute les 30 secondes les statistiques de tout les joueurs, on récupère ici TOUTE les
statistique.
Les statistiques sont mises en formes dans un streamlit (ou équivalent) qui se lance en parallèle au serveur
avec la même IP
On affiche un dashboard d'accueil, avec les joueurs les plus actifs, on peut même insérer la DynMap dans un
onglet.
Un onglet avec l'accès à toute les stats de tout les joueurs et des onglets avec les stats les plus intéressante
à définir qui compare les différents joueurs sur différents aspects du jeu.
