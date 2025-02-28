import { Outlet } from "react-router-dom";
import { NavBar } from "./NavBar";

export function Layout() {
    return (
        <div>
            <NavBar />
            <main className="p-4">
                <Outlet /> 
            </main>
        </div>
    );
}
