# StatCraft - Récupération et Affichage des Données

StatCraft est un outil de récupération des statistiques de joueurs Minecraft, avec une interface web intégrée pour afficher en temps réel les données collectées.

## Cas d'usage

### 1. Mode de jeu avec reset (Hardcore)
- **Récupération périodique :** Toutes les 30 secondes, l'outil collecte les statistiques des joueurs.
- **Données ciblées :** Pour le mode Hardcore, seules les statistiques les plus pertinentes sont récupérées (monstres tués, dégâts reçus, certains minerais minés, fondus, etc.).
- **Score global configurable :** Un score global est calculé à partir des statistiques pondérées, selon les paramètres définis dans le `config.yml`.

### 2. Mode de jeu classique
- **Récupération complète :** Dans le mode classique, toutes les statistiques disponibles de chaque joueur sont récupérées toutes les 30 secondes.

## Affichage des Données

StatCraft intègre désormais directement un serveur web (basé sur Javalin) pour afficher les données collectées. Plus besoin d'utiliser un serveur Streamlit externe.  
Vous pouvez consulter les statistiques en temps réel via l'interface web en vous connectant à l'adresse configurée (par défaut : [http://localhost:7070](http://localhost:7070)).

Pour plus de fonctionnalités d'affichage ou d'extensions, consultez également le projet [StatCraft-server](https://github.com/Loutreee/StatCraft-server).

## Avancement

Voici un tableau récapitulatif des étapes du projet :

| **Étape**                                  | **Description**                                                                                                                                           | **Statut**            | **Commentaires**                                                                                                  |
|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|:---------------------:|-------------------------------------------------------------------------------------------------------------------|
| **1. Spécifications Fonctionnelles**       | Définir précisément les fonctionnalités de l'outil : récupération des statistiques, types de graphiques, modes de jeu, etc.                               | ✅                    | Cas d'usage définis : Hardcore, mode classique, comparaison, score global, notifications Discord.                |
| **2. Récupération des Statistiques**        | Mise en place de la collecte des statistiques des joueurs (monstres tués, dégâts reçus, minerais minés, etc.).                                              | ✅                    | Récupération testée en mode Hardcore pour certaines statistiques clés.                                           |
| **3. Calcul du Score Global**               | Développement d'une formule pour générer un score global basé sur des pondérations configurables.                                                         | ✅                    | Pondérations définies pour les différents types de statistiques.                                                 |
| **4. Mode Classique - Statistiques Complètes**| Extension de la récupération afin d'inclure l'ensemble des statistiques disponibles pour chaque joueur en mode classique.                                  | 🔜                    | Traitement complet des données à implémenter pour l'affichage de toutes les statistiques.                         |
| **5. Intégration de l'Interface Web**       | Intégration directe d'un serveur web (Javalin) pour l'affichage des données collectées, supprimant ainsi la nécessité d'un serveur Streamlit externe.    | ✅                    | L'interface web démarre automatiquement avec le plugin sur le port configuré (par défaut : 27800).                   |
| **6. Tests et Validation**                  | Réalisation de tests complets pour valider le bon fonctionnement des modes Hardcore et Classique.                                                        | 🔜                    | Tests de bout en bout sur un serveur en conditions réelles (multi-joueurs, charge, etc.).                          |

Légende :  
✅ Terminé  
🟡 En cours  
🔜 À venir
