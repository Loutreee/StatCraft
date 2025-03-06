import "./App.css"
import { Routes, Route } from "react-router-dom";
import { PlayersList } from './components/Players';
import { PlayerStats } from './components/PlayerStats';
import { Setup } from './components/Setup';
import { Status } from './components/Status';
import { Home } from './components/Home';
import { Layout } from './components/Layout';

function App() {
  return (
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="setup" element={<Setup />} />
          <Route path="players">
            <Route index element={<PlayersList />} />
            <Route path=":player" element={<PlayerStats />} />
          </Route>
          <Route path="status" element={<Status />} />
        </Route>
      </Routes>
  );
}

export default App;
