import { useState, useEffect } from "react";
import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
} from "recharts";

type PlayerData = {
    playerName: string;
    timestamp: string; // Exemple : "2025-03-06T12:52:56.697379092Z"
    totalScore: number;
};

type FormattedData = {
    isoString: string; // Timestamp ISO brut
    label: string; // Label lisible pour l'axe X (ex: heure locale)
    [playerName: string]: number | string;
};

// Calcule la couleur moyenne de l'image passée en paramètre
function computeAverageColor(img: HTMLImageElement): string {
    const canvas = document.createElement("canvas");
    canvas.width = img.width;
    canvas.height = img.height;
    const ctx = canvas.getContext("2d");
    if (!ctx) return "#000000"; // Couleur de secours
    ctx.drawImage(img, 0, 0, img.width, img.height);
    const imageData = ctx.getImageData(0, 0, img.width, img.height);
    const data = imageData.data;
    let r = 0,
        g = 0,
        b = 0;
    const totalPixels = data.length / 4;

    for (let i = 0; i < data.length; i += 4) {
        r += data[i];
        g += data[i + 1];
        b += data[i + 2];
    }

    r = Math.round(r / totalPixels);
    g = Math.round(g / totalPixels);
    b = Math.round(b / totalPixels);

    return `rgb(${r}, ${g}, ${b})`;
}

// Charge l'image du joueur et calcule la couleur moyenne
async function loadPlayerColor(playerName: string): Promise<string> {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.crossOrigin = "anonymous"; // Nécessaire pour lire les pixels
        img.src = `https://mineskin.eu/helm/${encodeURIComponent(playerName)}/100.png`;

        img.onload = () => {
            const color = computeAverageColor(img);
            resolve(color);
        };
        img.onerror = reject;
    });
}

export function LeaderboardChart() {
    const [data, setData] = useState<FormattedData[]>([]);
    const [players, setPlayers] = useState<string[]>([]);
    // Stocke la couleur calculée pour chaque joueur
    const [playerColors, setPlayerColors] = useState<Record<string, string>>({});

    const chartMargin = { top: 10, right: 80, left: 10, bottom: 20 };
    const chartHeight = 500;
    const imagesWidth = 80;

    // Récupération des joueurs et de leur historique de scores
    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Récupérer la liste des joueurs
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
                    const scoreResponse = await fetch(
                        `/api/score/history/${encodedName}`
                    );
                    if (!scoreResponse.ok) {
                        throw new Error(`Failed to fetch score history for ${playerName}`);
                    }
                    return scoreResponse.json();
                });

                const scoresData = await Promise.all(scoresPromises);

                // 3. Fusionner les données par timestamp
                const mergedData: Record<string, FormattedData> = {};
                scoresData.forEach((playerData, index) => {
                    const currentPlayer = playerNames[index];
                    playerData.forEach((entry: PlayerData) => {
                        const isoString = entry.timestamp;
                        if (!mergedData[isoString]) {
                            mergedData[isoString] = {
                                isoString,
                                label: new Date(isoString).toLocaleTimeString(),
                            };
                        }
                        mergedData[isoString][currentPlayer] = entry.totalScore;
                    });
                });

                // 4. Conversion en tableau et tri par ordre chronologique
                const sortedArray = Object.keys(mergedData)
                    .sort(
                        (a, b) => new Date(a).getTime() - new Date(b).getTime()
                    )
                    .map((key) => mergedData[key]);

                // 5. Imputation des valeurs manquantes pour chaque joueur
                for (let i = 0; i < sortedArray.length; i++) {
                    for (let j = 0; j < playerNames.length; j++) {
                        const player = playerNames[j];
                        if (sortedArray[i][player] === undefined) {
                            sortedArray[i][player] =
                                i === 0 ? 0 : sortedArray[i - 1][player] || 0;
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

    // Charger et calculer la couleur moyenne pour chaque joueur
    useEffect(() => {
        let isCancelled = false;

        async function loadAllColors() {
            const newColors: Record<string, string> = {};

            for (const player of players) {
                try {
                    newColors[player] = await loadPlayerColor(player);
                } catch (err) {
                    newColors[player] = "#000000"; // Couleur de fallback
                }
                if (isCancelled) return;
            }

            setPlayerColors(newColors);
        }

        if (players.length) {
            loadAllColors();
        }

        return () => {
            isCancelled = true;
        };
    }, [players]);

    // Calculer yMin et yMax sur l'ensemble des données
    let yMin = Number.POSITIVE_INFINITY;
    let yMax = Number.NEGATIVE_INFINITY;
    if (data.length && players.length) {
        data.forEach((entry) => {
            players.forEach((player) => {
                const value = Number(entry[player]);
                if (!isNaN(value)) {
                    yMin = Math.min(yMin, value);
                    yMax = Math.max(yMax, value);
                }
            });
        });
    }
    if (yMin === Number.POSITIVE_INFINITY) yMin = 0;
    if (yMax === Number.NEGATIVE_INFINITY) yMax = 100;

    const effectiveHeight = chartHeight - chartMargin.top - chartMargin.bottom;
    const getPixelY = (score: number) => {
        return (
            chartMargin.top +
            ((yMax - score) / (yMax - yMin)) * effectiveHeight
        );
    };

    // Calculer la position verticale (pixelY) pour afficher les images à droite
    const playerImagePositions = players
        .map((player) => {
            if (!data.length) return null;
            const lastEntry = data[data.length - 1];
            const lastValue = Number(lastEntry[player]);
            const pixelY = getPixelY(lastValue);
            return { player, pixelY };
        })
        .filter(
            (item): item is { player: string; pixelY: number } => item !== null
        );

    return (
        <div style={{ position: "relative", display: "flex" }}>
            <div style={{ flex: 1 }}>
                <ResponsiveContainer width="100%" height={chartHeight}>
                    <LineChart data={data} margin={chartMargin}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="label" stroke="#000" />
                        <YAxis stroke="#000" domain={[yMin, yMax]} />
                        <Tooltip />
                        {players.map((player) => (
                            <Line
                                key={player}
                                type="monotone"
                                dataKey={player}
                                // Utilise la couleur moyenne calculée pour la courbe
                                stroke={playerColors[player] || "#000"}
                                strokeWidth={3}
                                dot={false}
                            />
                        ))}
                    </LineChart>
                </ResponsiveContainer>
            </div>
            <div style={{ width: imagesWidth, position: "relative" }}>
                {playerImagePositions.map(({ player, pixelY }) => {
                    const imageSize = 30;
                    players.indexOf(player);
                    const borderColor = playerColors[player] || "#000";
                    return (
                        <img
                            key={player}
                            src={`https://mineskin.eu/helm/${player}/100.png`}
                            alt={player}
                            style={{
                                position: "absolute",
                                left: -60,
                                top: pixelY - imageSize,
                                width: imageSize,
                                height: imageSize,
                                border: `2px solid ${borderColor}`,
                            }}
                        />
                    );
                })}
            </div>
        </div>
    );
}
