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

## Avancement

| **Étape**                                  | **Description**                                                                                                                                           | **Statut**            | **Commentaires**                                                                                                  |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------:|--------------------------------------------------------------------------------------------------------------------|
| **1. Spécifications Fonctionnelles**       | Définir précisément les fonctionnalités de l'outil : récupération des statistiques, types de graphiques, modes de jeu, etc.                                  | ✅ | Cas d'usage définis : Hardcore, mode classique, comparaison, score global, envoi Discord.                         |
| **2. Récupération des statistiques**       | Mettre en place la récupération des statistiques des joueurs (monstres tués, dégâts reçus, minerais minés, etc.).                                           | 🟡 | Récupération de stats en Hardcore, test sur quelques stats pour évaluer la faisabilité.                           |
| **3. Calcul du "score" global**            | Développer une équation pour générer un score global basé sur les statistiques pondérées.                                                                   | 🔜 | Besoin de définir les pondérations des stats (ex: monstres tués, dégâts).                                         |
| **4. Génération des graphiques**           | Générer des graphiques comparant les joueurs individuellement et entre eux.                                                                                | 🔜 | Utilisation de Matplotlib ou Plotly pour créer les PNG des graphs.                                                |
| **5. Envoi des graphiques sur Discord**    | Automatiser l'envoi des PNG des graphiques dans un channel Discord.                                                                                        | 🔜 | Utiliser une bibliothèque Python comme discord.py pour l'envoi.                                                   |
| **6. Dashboard Streamlit**                 | Créer une interface Streamlit pour afficher les statistiques des joueurs, incluant un tableau de bord avec des informations détaillées et la DynMap.        | 🔜 | Streamlit en parallèle du serveur Minecraft pour un accès en temps réel aux stats.                                |
| **7. Intégration des statistiques en direct** | Envisager la récupération des statistiques en temps réel pendant le jeu, pour visualiser l'évolution des stats durant une session en cours.                 | 🔜 | Vérifier la faisabilité technique (accès aux stats via des API pendant le jeu).                                   |
| **8. Mode Classique - Stats Complètes**    | Étendre la récupération des statistiques pour inclure **toutes** les statistiques disponibles pour chaque joueur dans le mode classique.                     | 🔜 | Implémenter le traitement des données pour afficher toutes les statistiques.                                      |
| **9. Comparaison Joueurs & Stats Personnalisées** | Permettre des comparaisons poussées entre joueurs et créer des onglets spécifiques dans Streamlit pour les stats les plus intéressantes.                    | 🔜 | Définir les stats à comparer (ex : minerais minés, monstres tués) et les afficher dans des onglets dédiés.         |
| **10. Tests et Validation**                | Effectuer des tests complets pour s'assurer du bon fonctionnement dans les deux modes de jeu, Hardcore et Classique.                                        | 🔜 | Test de bout en bout sur un serveur avec plusieurs joueurs.                                                       |
