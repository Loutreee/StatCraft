type TopPlayerCard = {
    title: string;
    description: string;
    imgUrl: string;
};

export function TopPlayers() {
    const topPlayersData: TopPlayerCard[] = [
        {
            title: "Dirtiest player",
            description: "5496124265162 dirt mined",
            imgUrl: "https://mineskin.eu/helm/DirtiestPlayer/100.png",
        },
        {
            title: "Highest score",
            description: "290465 Global Score",
            imgUrl: "https://mineskin.eu/helm/HighScorePlayer/100.png",
        },
        {
            title: "The adventurer",
            description: "456281 km travelled",
            imgUrl: "https://mineskin.eu/helm/AdventurerPlayer/100.png",
        },
        {
            title: "Serial Killer",
            description: "263 players killed",
            imgUrl: "https://mineskin.eu/helm/SerialKiller/100.png",
        },
        {
            title: "Gold digger",
            description: "568255 gold ore mined",
            imgUrl: "https://mineskin.eu/helm/GoldDigger/100.png",
        },
        {
            title: "Enlighter",
            description: "468562 torch placed",
            imgUrl: "https://mineskin.eu/helm/Enlighter/100.png",
        },
    ];

    return (
        <div className="bg-[#fdf1da] p-6 rounded-lg shadow-lg">
            <h2 className="text-3xl font-bold mb-6">Top player</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                {topPlayersData.map((card, index) => (
                    <div
                        key={index}
                        className="flex items-center p-4 bg-white rounded shadow hover:shadow-md transition text-black-dark"
                    >
                        <img
                            src={card.imgUrl}
                            alt={card.title}
                            className="w-16 h-16 rounded mr-4"
                        />
                        <div>
                            <h3 className="text-xl font-semibold">{card.title}</h3>
                            <p className="text-gray-700">{card.description}</p>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
