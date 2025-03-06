import { useParams, useNavigate } from "react-router-dom";

export function PlayerStats() {
    const { player } = useParams(); 
    const navigate = useNavigate();

    return (
        <div className="max-w-md mx-auto p-6 bg-white rounded shadow-md text-center">
            <h2 className="text-2xl  text-black font-bold mb-4">Statistiques de {player}</h2>
            <img 
                src={`https://mineskin.eu/headhelm/${player}/100.png`} 
                alt={player} 
                className="w-32 h-32 mx-auto rounded-lg mb-4"
            />
            <p className="text-black">📊 Infos sur {player}...</p>
            <button 
                onClick={() => navigate("/players")} 
                className="mt-4 px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition"
            >
                Retour
            </button>
        </div>
    );
}
