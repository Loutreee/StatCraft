import { useState, useEffect, useRef } from "react";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";

const colors = ["#FF00FF", "#00FF00", "#0000FF", "#FFA500", "#FF4500", "#9400D3"];

type PlayerData = {
  playerName: string;
  timestamp: string; // Exemple : "2025-03-06T12:52:56.697379092Z"
  totalScore: number;
};

type FormattedData = {
  isoString: string; // Clé brute (timestamp ISO)
  label: string;     // Label lisible pour l'axe X (ex: heure locale)
  [playerName: string]: number | string;
};

export function LeaderboardChart() {
  const [data, setData] = useState<FormattedData[]>([]);
  const [players, setPlayers] = useState<string[]>([]);
  // Dimensions du conteneur du graphique
  const containerRef = useRef<HTMLDivElement>(null);
  const [containerHeight, setContainerHeight] = useState(0);

  // Marges utilisées dans le LineChart
  const chartMargin = { top: 10, right: 80, left: 10, bottom: 20 };
  const chartHeight = 500; // Tel que défini dans ResponsiveContainer

  useEffect(() => {
    // Mesurer la hauteur du conteneur une fois le composant monté
    if (containerRef.current) {
      setContainerHeight(containerRef.current.clientHeight);
    }
    // Vous pouvez ajouter ici un listener pour le redimensionnement si nécessaire.
  }, [data]);

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

        // 4. Convertir mergedData en tableau et trier par ordre chronologique (ascendant)
        const sortedArray = Object.keys(mergedData)
          .sort((a, b) => new Date(a).getTime() - new Date(b).getTime())
          .map((key) => mergedData[key]);

        // 5. Imputer les valeurs manquantes pour chaque joueur
        for (let i = 0; i < sortedArray.length; i++) {
          for (let j = 0; j < playerNames.length; j++) {
            const player = playerNames[j];
            if (sortedArray[i][player] === undefined) {
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

  // Calculer yMin et yMax sur l'ensemble des données
  let yMin = Number.POSITIVE_INFINITY;
  let yMax = Number.NEGATIVE_INFINITY;
  if (data.length && players.length) {
    data.forEach(entry => {
      players.forEach(player => {
        const value = Number(entry[player]);
        if (!isNaN(value)) {
          yMin = Math.min(yMin, value);
          yMax = Math.max(yMax, value);
        }
      });
    });
  }
  // Si aucune donnée n'est disponible, on fixe des valeurs par défaut
  if (yMin === Number.POSITIVE_INFINITY) yMin = 0;
  if (yMax === Number.NEGATIVE_INFINITY) yMax = 100;

  // Calculer la position en pixels pour un score donné
  // On utilise chartHeight (500) et on tient compte des marges top et bottom
  const effectiveHeight = chartHeight - chartMargin.top - chartMargin.bottom;
  const getPixelY = (score: number) => {
    // Plus le score est élevé, plus y doit être proche du haut (chartMargin.top)
    return chartMargin.top + ((yMax - score) / (yMax - yMin)) * effectiveHeight;
  };

  // Pour chaque joueur, récupérer la dernière valeur et calculer sa position verticale
  const playerImagePositions = players.map(player => {
    if (!data.length) return null;
    const lastEntry = data[data.length - 1];
    const lastValue = Number(lastEntry[player]);
    const pixelY = getPixelY(lastValue);
    return { player, pixelY };
  }).filter(Boolean);

  // Largeur réservée pour le rendu des images à droite (peut être ajustée)
  const imagesWidth = 80;

  return (
    <div ref={containerRef} style={{ position: "relative", display: "flex" }}>
      {/* Conteneur du graphique */}
      <div style={{ flex: 1 }}>
        <ResponsiveContainer width="100%" height={chartHeight}>
          <LineChart data={data} margin={chartMargin}>
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
                dot={false}
              />
            ))}
          </LineChart>
        </ResponsiveContainer>
      </div>
      {/* Colonne à droite pour afficher les images alignées aux courbes */}
      <div style={{ width: imagesWidth, position: "relative" }}>
        {playerImagePositions.map(({ player, pixelY }) => {
          const imageSize = 30;
          const playerIndex = players.indexOf(player);
          const borderColor = colors[playerIndex % colors.length];
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
                border: `2px solid ${borderColor}`
              }}
            />
          );
        })}
      </div>
    </div>
  );
}
