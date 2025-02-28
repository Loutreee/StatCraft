import { LeaderboardChart } from "./LeaderBoardChart";

export function Home() {
    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold text-center text-black mt-6">Bienvenue sur StatCraft</h1>;
            <LeaderboardChart />
        </div>
    );
}
