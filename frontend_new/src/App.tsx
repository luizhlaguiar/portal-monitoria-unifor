import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Discipline from './pages/Discipline';
import MonitorDashboard from './pages/MonitorDashboard';
import MonitorDisciplineDetail from './pages/MonitorDisciplineDetail';
import ManageDiscipline from './pages/ManageDiscipline';
import Messages from './pages/Messages';
import api from './services/api';
import 'bootstrap/dist/css/bootstrap.min.css';
import './styles/index.css';

const PrivateRoute = ({ children }: { children: JSX.Element }) => {
  const [status, setStatus] = useState<'checking' | 'ok' | 'denied'>('checking');

  useEffect(() => {
    let active = true;
    if (!localStorage.getItem('token') || !localStorage.getItem('user')) {
      setStatus('denied');
      return;
    }
    api.get('/auth/me')
      .then((res) => {
        if (!active) return;
        const me = res.data;
        const role = String(me.role).toUpperCase();
        const stored = JSON.parse(localStorage.getItem('user') || '{}');
        localStorage.setItem('user', JSON.stringify({ ...stored, id: me.id, nome: me.nome, role }));
        localStorage.setItem('role', role);
        setStatus('ok');
      })
      .catch((err) => {
        if (!active) return;
        if (err.response?.status === 401 || err.response?.status === 403) {
          localStorage.clear();
        }
        setStatus('denied');
      });
    return () => { active = false; };
  }, []);

  if (status === 'checking') return null;
  return status === 'ok' ? children : <Navigate to="/login" />;
};

const App: React.FC = () => {
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
