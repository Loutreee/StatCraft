import React, { useEffect, useState } from 'react';
import axios from 'axios';

const PlayersList = () => {
    const [players, setPlayers] = useState([]);

    useEffect(() => {
        // Récupère la liste des joueurs depuis votre API REST
        axios.get('/api/players')
            .then(response => {
                setPlayers(response.data);
            })
            .catch(error => {
                console.error('Erreur lors de la récupération des joueurs:', error);
            });
    }, []);

    return (
        <div>
            <h1>Liste des joueurs</h1>
            {players.length === 0 ? (
                <p>Aucun joueur trouvé.</p>
            ) : (
                <ul>
                    {players.map(player => (
                        <li key={player.name}>{player.name}</li>
                    ))}
                </ul>
            )}
        </div>
    );
};

export default PlayersList;
