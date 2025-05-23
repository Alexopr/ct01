import React from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const AdminRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();
  if (loading) return <div>Loading...</div>;
  if (!isAuthenticated()) return <Navigate to="/login" />;
  return isAdmin() ? <>{children}</> : <Navigate to="/" />;
};

export default AdminRoute; 