import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

interface Stats {
    playerName: string;
    timestamp: string;
    blocksMined: Record<string, number>;
    itemsCrafted: Record<string, number>;
    mobsKilled: Record<string, number>;
    playTime: number;
    blockScore: number;
    craftScore: number;
    mobScore: number;
    timeScore: number;
    totalScore: number;
}

export function PlayerStats() {
    const { player } = useParams<{ player: string }>();
    const navigate = useNavigate();

    // L’état peut être de type Stats ou null
    const [stats, setStats] = useState<Stats | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!player) return;

        fetch(`/api/stats/${player}/latest`)
            .then((res) => {
                if (!res.ok) {
                    throw new Error(`Erreur HTTP : ${res.status}`);
                }
                return res.json();
            })
            .then((data: Stats) => {
                setStats(data);
                setLoading(false);
            })
            .catch((err) => {
                setError(err.message);
                setLoading(false);
            });
    }, [player]);

    if (loading) {
        return <div className="text-black">Chargement...</div>;
    }

    if (error) {
        return <div className="text-black">Erreur : {error}</div>;
    }

    // Si pas de stats => on ne peut pas afficher
    if (!stats) {
        return <div className="text-black">Aucune statistique à afficher.</div>;
    }

    return (
        <div className="max-w-md mx-auto p-6 bg-white rounded shadow-md text-center text-black">
            <h2 className="text-2xl font-bold mb-4">Statistiques de {player}</h2>
            <img
                src={`https://mineskin.eu/headhelm/${player}/100.png`}
                alt={player}
                className="w-32 h-32 mx-auto rounded-lg mb-4"
            />
            <div className="text-left">
                <p>
                    <strong>Timestamp :</strong> {stats.timestamp}
                </p>
                <p>
                    <strong>Temps de jeu :</strong> {stats.playTime} minutes
                </p>
                <p>
                    <strong>Score total :</strong> {stats.totalScore}
                </p>
                <p>
                    <strong>Score Bloc :</strong> {stats.blockScore}
                </p>
                <p>
                    <strong>Score Craft :</strong> {stats.craftScore}
                </p>
                <p>
                    <strong>Score Mob :</strong> {stats.mobScore}
                </p>
                <p>
                    <strong>Score Temps :</strong> {stats.timeScore}
                </p>
            </div>
            <button
                onClick={() => navigate("/players")}
                className="mt-4 px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition"
            >
                Retour
            </button>
        </div>
    );
}
