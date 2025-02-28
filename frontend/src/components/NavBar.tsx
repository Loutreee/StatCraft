import { useState } from "react";
import { useNavigate } from "react-router-dom";

export function NavBar() {
    const [activeButton, setActiveButton] = useState("Home");
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const navigate = useNavigate();

    const handleClick = (buttonName: string, path: string) => {
        setActiveButton(buttonName);
        navigate(path);
    };

    return (
            <nav className="bg-green border-b-2 border-red">
                <div className="flex flex-wrap items-center justify-between p-4">
                    <a href="#" className="flex items-center space-x-3">
                    <img src="/logo.png" className="h-8" alt="StatCraft Logo" />
                        <span className="self-center text-2xl font-semibold text-white-dark">
                            StatCraft
                        </span>
                    </a>
                    <button
                        data-collapse-toggle="navbar-default"
                        type="button"
                        className="inline-flex items-center p-2 w-10 h-10 justify-center md:hidden"
                        aria-controls="navbar-default"
                        aria-expanded={isMenuOpen}
                        onClick={() => setIsMenuOpen(!isMenuOpen)}
                    >
                        <span className="sr-only">Open main menu</span>
                        <svg
                            className="w-5 h-5"
                            aria-hidden="true"
                            xmlns="http://www.w3.org/2000/svg"
                            fill="none"
                            viewBox="0 0 17 14"
                        >
                            <path
                                stroke="currentColor"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth="2"
                                d="M1 1h15M1 7h15M1 13h15"
                            />
                        </svg>
                    </button>
                    {/* Menu (visible sur desktop, toggle sur mobile) */}
                    <div
                        className={`w-full md:flex md:w-auto transition-all duration-300 ease-in-out ${isMenuOpen ? "block" : "hidden"
                            }`}
                        id="navbar-default"
                    >
                        <ul className="font-medium flex flex-col p-4 md:p-0 mt-4 border rounded-lg md:flex-row md:space-x-8 md:mt-0 md:border-0">
                        {[
                            { name: "Home", path: "/" },
                            { name: "Setup", path: "/setup" },
                            { name: "Players", path: "/players" },
                            { name: "Status", path: "/status" }
                        ].map(({ name, path }) => (
                                <li key={name}>
                                    <button
                                        className={`block py-2 px-3 rounded-sm transition-colors duration-200 ${activeButton === name
                                                ? "text-black bg-green-light"
                                                : "text-black hover:bg-green-light"
                                            }`}
                                        onClick={() => handleClick(name, path)}
                                    >
                                        {name}
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </div>
                </div>
            </nav>
    );
}
