import {useAuth0} from "@auth0/auth0-react";
import {Navigate} from "react-router-dom";
import React from "react";

interface ContainerProps {
  children: React.ReactNode;
}

export default function Protected({children}: ContainerProps) {
  const { isAuthenticated, isLoading } = useAuth0();

  if (isLoading) return <div>Loading ...</div>;

  if (isAuthenticated) return children;

  else return <Navigate to='/' />;
}
