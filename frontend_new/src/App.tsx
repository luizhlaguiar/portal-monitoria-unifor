import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Discipline from './pages/Discipline';
import MonitorDashboard from './pages/MonitorDashboard';
import MonitorDisciplineDetail from './pages/MonitorDisciplineDetail';
import ManageDiscipline from './pages/ManageDiscipline';
import Messages from './pages/Messages';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/index.css';

const App: React.FC = () => {
  // Função simples para proteger rotas
  const PrivateRoute = ({ children }: { children: JSX.Element }) => {
    const user = localStorage.getItem('user');
    return user ? children : <Navigate to="/login" />;
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />

        <Route path="/dashboard" element={
          <PrivateRoute>
            <Dashboard />
          </PrivateRoute>
        } />

        <Route path="/discipline/:id" element={
          <PrivateRoute>
            <Discipline />
          </PrivateRoute>
        } />

        <Route path="/monitor-dashboard" element={
          <PrivateRoute>
            <MonitorDashboard />
          </PrivateRoute>
        } />

        <Route path="/monitor-discipline/:id" element={
          <PrivateRoute>
            <MonitorDisciplineDetail />
          </PrivateRoute>
        } />

        <Route path="/manage-discipline" element={
          <PrivateRoute>
            <ManageDiscipline />
          </PrivateRoute>
        } />

        <Route path="/messages" element={
          <PrivateRoute>
            <Messages />
          </PrivateRoute>
        } />

        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="*" element={<Navigate to="/dashboard" />} />
      </Routes>
    </Router>
  );
};

export default App;
