import players from "../data/players.json";

export function Players() {
    return (
        <div className="max-w-screen-lg mx-auto p-6">
            <h1 className="text-3xl text-black font-bold text-center mb-6">Liste des Joueurs</h1>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {players.map((player) => (
                    <a 
                        key={player.name}
                        href={player.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex flex-col items-center p-4 bg-white shadow-md rounded-lg hover:shadow-lg transition"
                    >
                        <img 
                            src={player.img} 
                            alt={player.name} 
                            className="w-24 h-24 rounded-lg object-cover"
                        />
                        <span className="mt-2 text-lg font-semibold text-black">{player.name}</span>
                    </a>
                ))}
            </div>
        </div>
    );
}
