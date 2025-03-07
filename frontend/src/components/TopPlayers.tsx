import { useEffect, useState } from "react";

// Structure de données renvoyée par l'API, ex: { "playerName": "Loutreee", "value": 5496124265162 }
type TopPlayerData = {
    playerName: string;
    value: number;
};

export function TopPlayers() {
    // États pour chaque catégorie
    const [dirtiest, setDirtiest] = useState<TopPlayerData | null>(null);
    const [highestScore, setHighestScore] = useState<TopPlayerData | null>(null);
    const [adventurer, setAdventurer] = useState<TopPlayerData | null>(null);
    const [serialKiller, setSerialKiller] = useState<TopPlayerData | null>(null);
    const [goldDigger, setGoldDigger] = useState<TopPlayerData | null>(null);
    const [enlighter, setEnlighter] = useState<TopPlayerData | null>(null);

    // États d'erreur/chargement
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchAll = async () => {
            try {
                // On lance les fetchs en parallèle
                const [
                    dirtiestRes,
                    highestScoreRes,
                    adventurerRes,
                    serialKillerRes,
                    goldDiggerRes,
                    enlighterRes,
                ] = await Promise.all([
                    fetch("/api/top-players/dirtiest"),
                    fetch("/api/top-players/highest-score"),
                    fetch("/api/top-players/adventurer"),
                    fetch("/api/top-players/serial-killer"),
                    fetch("/api/top-players/gold-digger"),
                    fetch("/api/top-players/enlighter"),
                ]);

                // Vérifier si tous sont OK
                if (!dirtiestRes.ok) throw new Error("Failed to fetch dirtiest");
                if (!highestScoreRes.ok) throw new Error("Failed to fetch highest score");
                if (!adventurerRes.ok) throw new Error("Failed to fetch adventurer");
                if (!serialKillerRes.ok) throw new Error("Failed to fetch serial killer");
                if (!goldDiggerRes.ok) throw new Error("Failed to fetch gold digger");
                if (!enlighterRes.ok) throw new Error("Failed to fetch enlighter");

                // Convertir en JSON
                const [dirtiestData, highestScoreData, adventurerData, serialKillerData, goldDiggerData, enlighterData] =
                    await Promise.all([
                        dirtiestRes.json(),
                        highestScoreRes.json(),
                        adventurerRes.json(),
                        serialKillerRes.json(),
                        goldDiggerRes.json(),
                        enlighterRes.json(),
                    ]);

                // Mettre à jour les états
                setDirtiest(dirtiestData);
                setHighestScore(highestScoreData);
                setAdventurer(adventurerData);
                setSerialKiller(serialKillerData);
                setGoldDigger(goldDiggerData);
                setEnlighter(enlighterData);

                setLoading(false);
            } catch (err: any) {
                setError(err.message || "Unknown error");
                setLoading(false);
            }
        };

        fetchAll();
    }, []);

    if (loading) {
        return <div className="text-black">Chargement des top players...</div>;
    }

    if (error) {
        return <div className="text-black">Erreur: {error}</div>;
    }

    // Fonction utilitaire pour afficher une carte
    const renderCard = (title: string, data: TopPlayerData | null, subtitle: string) => {
        if (!data) {
            return (
                <div className="flex items-center p-4 bg-white rounded shadow">
                    <p className="text-black">Aucune donnée</p>
                </div>
            );
        }
        return (
            <div className="flex items-center p-4 bg-white rounded shadow hover:shadow-md transition">
                <img
                    src={`https://mineskin.eu/helm/${data.playerName}/100.png`}
                    alt={data.playerName}
                    className="w-16 h-16 rounded mr-4"
                />
                <div>
                    <h3 className="text-xl font-semibold text-black">{title}</h3>
                    <p className="text-black">
                        {data.value} {subtitle}
                    </p>
                </div>
            </div>
        );
    };

    return (
        <div className="bg-[#fdf1da] p-6 rounded-lg shadow-lg">
            <h2 className="text-3xl font-bold mb-6 text-black">Top player</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {/* 1) Dirtiest player */}
                {renderCard("Dirtiest player", dirtiest, "dirt mined")}
                {/* 2) Highest score */}
                {renderCard("Highest score", highestScore, "Global Score")}
                {/* 3) The adventurer */}
                {renderCard("The adventurer", adventurer, "km travelled")}
                {/* 4) Serial killer */}
                {renderCard("Serial Killer", serialKiller, "players killed")}
                {/* 5) Gold digger */}
                {renderCard("Gold digger", goldDigger, "gold ore mined")}
                {/* 6) Enlighter */}
                {renderCard("Enlighter", enlighter, "torch placed")}
            </div>
        </div>
    );
}
