import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export function PlayersList() {
    const [players, setPlayers] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        // Appel à l'API pour récupérer la liste des joueurs
        fetch("/api/players")
            .then((res) => {
                if (!res.ok) {
                    throw new Error(`Erreur HTTP: ${res.status}`);
                }
                return res.json();
            })
            .then((data) => {
                // data doit être un tableau d'objets { name: "..." }
                setPlayers(data);
            })
            .catch((err) => {
                console.error("Erreur lors de la récupération des joueurs:", err);
            });
    }, []);

    // @ts-ignore
    // @ts-ignore
    return (
        <div className="max-w-screen-lg mx-auto p-6">
            <h1 className="text-3xl text-black font-bold text-center mb-6">
                Liste des Joueurs
            </h1>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {players.map(({name}) => (
                    <button
                        key={name}
                        onClick={() => navigate(`/players/${name}`)}
                        className="flex flex-col items-center p-4 bg-white shadow-md rounded-lg hover:shadow-lg transition"
                    >
                        <img
                            src={`https://mineskin.eu/helm/${name}/100.png`}
                            alt={name}
                            className="w-24 h-24 rounded-lg object-cover"
                        />
                        <span className="mt-2 text-lg font-semibold text-black">
                            {name}
                        </span>
                    </button>
                ))}
            </div>
        </div>
    );
}
