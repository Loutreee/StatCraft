import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from "recharts";
import { useState, useEffect } from "react";

type PlayerData = {
    timestamp: string;
    totalScore: number;
};

export function LeaderboardChart() {
    const [data, setData] = useState<PlayerData[]>([]);

    useEffect(() => {
        const fetchData = async () => {
            try {
                const response = await fetch("/api/stats/loutreee/latest");
                if (!response.ok) {
                    throw new Error("Failed to fetch data");
                }
                const result = await response.json();

                // Transformer les données pour une courbe de progression
                const formattedData = result.map((player: any) => ({
                    timestamp: new Date(player.timestamp).toLocaleTimeString(), // Formater l'heure
                    totalScore: player.totalScore
                }));

                setData(formattedData);
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
                    <XAxis dataKey="timestamp" stroke="#000" />
                    <YAxis stroke="#000" />
                    <Tooltip />
                    <Line type="monotone" dataKey="totalScore" stroke="#FF00FF" strokeWidth={3} dot={{ r: 5 }} />
                </LineChart>
            </ResponsiveContainer>
        </div>
    );
}
