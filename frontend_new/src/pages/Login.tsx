import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { Container, Card, Button, Form } from 'react-bootstrap';
import './Login.css';

const Login: React.FC = () => {
  const [form, setForm] = useState({ matricula: '', senha: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', form);
      const userData = response.data;

      // LIMPEZA TOTAL DO STORAGE PARA EVITAR CONFLITOS
      localStorage.clear();

      // SALVAMENTO EXPLICITO
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('role', userData.role.toUpperCase());
      localStorage.setItem('token', 'simulated-token-123');

      // REDIRECIONAMENTO FORÇADO
      window.location.href = '/dashboard';
    } catch (err: any) {
      // Garantir que a mensagem de erro seja sempre uma string para não quebrar o React
      const errorData = err.response?.data;
      const message = typeof errorData === 'string'
        ? errorData
        : errorData?.message || errorData?.error || 'Credenciais inválidas. Tente novamente.';

      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <Container>
        <div className="d-flex justify-content-center">
          <Card className="login-card border-0 shadow-lg">
            <Card.Body className="p-5 text-center">
              <div className="mb-4">
                <div className="bg-light border rounded-circle mx-auto mb-3 d-flex align-items-center justify-content-center" style={{ width: '60px', height: '60px' }}>
                  <span className="fw-bold text-secondary">UNIFOR</span>
                </div>
                <h3 className="fw-bold">Plataforma de Monitoria UNIFOR</h3>
                <p className="text-muted small">Sistema de gestão e apoio acadêmico para alunos e monitores.</p>
              </div>

              <div className="text-start mb-4">
                <Form onSubmit={handleLogin}>
                  <Form.Group className="mb-3">
                    <Form.Label className="small fw-bold text-uppercase text-muted">Matrícula</Form.Label>
                    <Form.Control
                      type="text"
                      placeholder="Ex: 12345"
                      value={form.matricula}
                      onChange={e => setForm({...form, matricula: e.target.value})}
                      required
                    />
                  </Form.Group>

                  <Form.Group className="mb-4">
                    <Form.Label className="small fw-bold text-uppercase text-muted">Senha</Form.Label>
                    <Form.Control
                      type="password"
                      placeholder="••••••••"
                      value={form.senha}
                      onChange={e => setForm({...form, senha: e.target.value})}
                      required
                    />
                  </Form.Group>

                  {error && (
                    <div className="alert alert-danger py-2 small text-center mb-3" role="alert">
                      {error}
                    </div>
                  )}

                  <div className="d-grid gap-2">
                    <Button variant="dark" type="submit" disabled={loading} className="fw-bold py-2">
                      {loading ? 'Autenticando...' : 'Entrar no Portal'}
                    </Button>
                  </div>
                </Form>
              </div>

              <div className="login-footer mt-4">
                <p className="text-muted small">© 2026 Universidade de Fortaleza - UNIFOR</p>
              </div>
            </Card.Body>
          </Card>
        </div>
      </Container>
    </div>
  );
};

export default Login;