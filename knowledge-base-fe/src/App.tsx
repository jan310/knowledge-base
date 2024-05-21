import './App.css';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./components/Home.tsx";
import Protected from "./components/Protected.tsx";
import DocOverview from "./components/DocOverview.tsx";

export default function App() {

  return (
    <BrowserRouter>
      <Routes>
        <Route path='/' element={<Home />} />
        <Route path='/protected' element={<Protected><DocOverview/></Protected>} />
      </Routes>
    </BrowserRouter>
  );

}
