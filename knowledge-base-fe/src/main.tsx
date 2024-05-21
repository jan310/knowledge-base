import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import {Auth0Provider} from "@auth0/auth0-react";

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <Auth0Provider
      domain="jan-ondra.eu.auth0.com"
      clientId="L82NHZWgVuwXUwZZLGSMtd1je78ExHlb"
      authorizationParams={{
        redirect_uri: 'http://localhost:5173/protected',
        audience: "https://knowledge-base-api/"
      }}
    >
      <App />
    </Auth0Provider>
  </React.StrictMode>,
);
