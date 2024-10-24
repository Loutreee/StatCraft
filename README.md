# StatCraft récupération de données

Récupération des statistiques de joueurs Minecraft

## Plusieurs cas d'usage
### 1. Mode de jeu avec reset
L'outil récupère toute les 30 secondes les statistiques de tout les joueurs. Pour le Hardcore on récupère les
données les plus intéressante, monstres tués, dégâts reçus, certains minerais minés, fondus, etc.

On définit un score global, **créer la doc* en fonction des actions effectué.

### 2. Mode de jeu classique
L'outil récupère toute les 30 secondes les statistiques de tout les joueurs, on récupère ici TOUTE les
statistique.

## Affichage des données
### StatCraft serveur 

StatCraft serveur peut être utilisé pour afficher les données récolté par StatCraft dans un interface web.

## Avancement

Voici la mise à jour avec les tableaux séparés :

### Tableau des étapes complètes et en cours

| **Étape**                                  | **Description**                                                                                                                                           | **Statut**            | **Commentaires**                                                                                                  |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|:-----------------------:|--------------------------------------------------------------------------------------------------------------------|
| **1. Spécifications Fonctionnelles**       | Définir précisément les fonctionnalités de l'outil : récupération des statistiques, types de graphiques, modes de jeu, etc.                                  | ✅ | Cas d'usage définis : Hardcore, mode classique, comparaison, score global, envoi Discord.                         |
| **2. Récupération des statistiques**       | Mettre en place la récupération des statistiques des joueurs (monstres tués, dégâts reçus, minerais minés, etc.).                                           | ✅ | Récupération de stats en Hardcore, test sur quelques stats pour évaluer la faisabilité.                           |
| **3. Calcul du "score" global**            | Développer une équation pour générer un score global basé sur les statistiques pondérées.                                                                   | ✅ | Besoin de définir les pondérations des stats (ex: monstres tués, dégâts).                                         |
| **4. Mode Classique - Stats Complètes**    | Étendre la récupération des statistiques pour inclure **toutes** les statistiques disponibles pour chaque joueur dans le mode classique.                     | 🔜 | Implémenter le traitement des données pour afficher toutes les statistiques.                                      |
| **5. Tests et Validation**                | Effectuer des tests complets pour s'assurer du bon fonctionnement dans les deux modes de jeu, Hardcore et Classique.                                        | 🔜 | Test de bout en bout sur un serveur avec plusieurs joueurs.                                                       |

✅ Terminé
🟡 En cours
🔜 À venir
