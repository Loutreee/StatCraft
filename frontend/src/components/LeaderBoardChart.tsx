import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { useState, useEffect } from "react";

const colors = ["#FF00FF", "#00FF00", "#0000FF", "#FFA500", "#FF4500", "#9400D3"];

type PlayerData = {
    playerName: string;
    timestamp: string;  // Exemple : "2025-03-06T12:52:56.697379092Z"
    totalScore: number;
};

type FormattedData = {
    isoString: string;  // Clé brute (timestamp ISO)
    label: string;      // Label lisible pour l'axe X (ex: heure locale)
    [playerName: string]: number | string;
};

export function LeaderboardChart() {
    const [data, setData] = useState<FormattedData[]>([]);
    const [players, setPlayers] = useState<string[]>([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Récupérer la liste des joueurs depuis l'API
                const playersResponse = await fetch("/api/players");
                if (!playersResponse.ok) {
                    throw new Error("Failed to fetch players");
                }
                const playersList = await playersResponse.json();
                const playerNames = playersList.map((p: { name: string }) => p.name);
                setPlayers(playerNames);

                // 2. Récupérer l'historique des scores pour chaque joueur
                const scoresPromises = playerNames.map(async (playerName: string) => {
                    const encodedName = encodeURIComponent(playerName);
                    const scoreResponse = await fetch(`/api/score/history/${encodedName}`);
                    if (!scoreResponse.ok) {
                        throw new Error(`Failed to fetch score history for ${playerName}`);
                    }
                    return scoreResponse.json(); // On s'attend à un tableau d'objets PlayerData
                });

                const scoresData = await Promise.all(scoresPromises);

                // 3. Fusionner les données par timestamp ISO
                const mergedData: Record<string, FormattedData> = {};
                scoresData.forEach((playerData, index) => {
                    const currentPlayer = playerNames[index];
                    playerData.forEach((entry: PlayerData) => {
                        const isoString = entry.timestamp; // La valeur ISO
                        if (!mergedData[isoString]) {
                            mergedData[isoString] = {
                                isoString,
                                label: new Date(isoString).toLocaleTimeString(), // Label lisible
                            };
                        }
                        mergedData[isoString][currentPlayer] = entry.totalScore;
                    });
                });

                // 4. Convertir mergedData en tableau et trier par ordre chronologique (ascendant)
                const sortedArray = Object.keys(mergedData)
                    .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
                    .map((key) => mergedData[key]);

                // 5. Imputer les valeurs manquantes pour chaque joueur
                // Pour chaque snapshot, pour chaque joueur, si la valeur est absente, utiliser la dernière connue (ou 0 si aucune).
                for (let i = 0; i < sortedArray.length; i++) {
                    for (let j = 0; j < playerNames.length; j++) {
                        const player = playerNames[j];
                        if (sortedArray[i][player] === undefined) {
                            // Si c'est le premier snapshot, on met 0
                            sortedArray[i][player] = i === 0 ? 0 : sortedArray[i - 1][player] || 0;
                        }
                    }
                }

                setData(sortedArray);
            } catch (error) {
                console.error("Error fetching data:", error);
            }
        };

        fetchData();
    }, []);

    return (
        <div className="bg-[#fdf1da] p-6 rounded-lg shadow-lg">
            <h2 className="text-3xl font-bold mb-4">Leaderboard</h2>
            <ResponsiveContainer width="100%" height={500}>
                <LineChart data={data} margin={{ top: 10, right: 50, left: 10, bottom: 20 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="label" stroke="#000" />
                    <YAxis stroke="#000" />
                    <Tooltip />
                    {players.map((player, index) => (
                        <Line
                            key={player}
                            type="monotone"
                            dataKey={player}
                            stroke={colors[index % colors.length]}
                            strokeWidth={3}
                            dot={false}  // Pas de points individuels
                        />
                    ))}
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}
