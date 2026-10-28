import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Navbar, Nav, ProgressBar, Spinner } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Dashboard.css';

interface MonitorDiscipline {
  id: number;
  name: string;
  code: string;
  studentCount?: number;
  color?: string;
}

const MonitorDashboard: React.FC = () => {
  const [disciplines, setDisciplines] = useState<MonitorDiscipline[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    const fetchDisciplines = async () => {
      try {
        setLoading(true);
        // Busca as disciplinas que este monitor especificamente monitora
        const response = await api.get(`/disciplines/monitor/${user.id}`);

        const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'];
        const dataWithColors = response.data.map((d: any, index: number) => ({
          ...d,
          color: colors[index % colors.length],
          studentCount: Math.floor(Math.random() * 100) + 10 // Simulando contagem de alunos
        }));

        setDisciplines(dataWithColors);
      } catch (error) {
        console.error('Erro ao carregar disciplinas do monitor:', error);
      } finally {
        setLoading(false);
      }
    };

    if (user.id) {
      fetchDisciplines();
    } else {
      setLoading(false);
    }
  }, [user.id]);

  return (
    <div className="dashboard-layout">
      <Navbar bg="dark" variant="dark" expand="lg" className="px-4 py-3 shadow-sm">
        <Navbar.Brand href="/" className="fw-bold d-flex align-items-center">
          <span className="border border-light rounded me-2 px-1 py-0" style={{fontSize: '0.8rem'}}>UNIFOR</span>
          Plataforma de Monitoria
        </Navbar.Brand>
        <Navbar.Toggle />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="ms-auto align-items-center gap-3">
            <Button variant="light" size="sm" className="fw-bold me-2" onClick={() => navigate('/dashboard')}>
              Voltar para Visão de Aluno
            </Button>
            <div className="d-flex align-items-center gap-3 ms-3">
              <span className="text-light opacity-75 small">{user.nome || 'Monitor'} (Monitor/Admin)</span>
              <Link to="/login" className="btn btn-outline-light btn-sm">Sair</Link>
            </div>
          </Nav>
        </Navbar.Collapse>
      </Navbar>

      <main className="main-content">
        <Container className="py-5">
          <header className="content-header mb-5 text-center">
            <div className="header-title text-center">
              <h1 className="fw-bold">Painel do Monitor</h1>
              <p className="text-muted">Gerencie suas disciplinas, alunos e dúvidas pendentes.</p>
            </div>
          </header>

          <section className="stats-grid mb-5">
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-warning border-4 text-center">
              <span className="stat-label text-muted small fw-bold">ALUNOS ATENDIDOS</span>
              <span className="stat-value h2 fw-bold">128</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-primary border-4 text-center">
              <span className="stat-label text-muted small fw-bold">HORAS DE MONITORIA</span>
              <span className="stat-value h2 fw-bold">42h</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-danger border-4 text-center">
              <span className="stat-label text-muted small fw-bold">DÚVIDAS PENDENTES</span>
              <span className="stat-value h2 fw-bold">12</span>
            </div>
          </section>

          <section className="disciplines-section text-center">
            <div className="section-header d-flex justify-content-between align-items-center mb-4 text-start">
              <h2 className="fw-bold mb-0">Minhas Disciplinas</h2>
            </div>

            {loading ? (
              <div className="text-center py-5">
                <Spinner animation="border" variant="dark" />
                <p className="mt-2 text-muted">Carregando suas monitorias...</p>
              </div>
            ) : (
              <Row className="g-4 justify-content-center">
                {disciplines.length > 0 ? (
                  disciplines.map(disc => (
                    <Col key={disc.id} xs={12} sm={6} lg={4} xl={3}>
                      <Card className="h-100 border-0 shadow-sm overflow-hidden discipline-card-modern">
                        <div className="banner-disciplina" style={{ backgroundColor: disc.color, height: '80px' }}>
                           <div className="banner-overlay"></div>
                           <div className="banner-text text-end p-3 text-white fw-bold small">
                             {disc.code}
                           </div>
                        </div>
                        <Card.Body className="p-4">
                          <div className="mb-3">
                            <h5 className="fw-bold mb-1">{disc.name}</h5>
                          </div>
                          <div className="mb-4">
                            <div className="d-flex justify-content-between small text-muted mb-1">
                              <span>Alunos Matriculados</span>
                              <span>{disc.studentCount}</span>
                            </div>
                            <ProgressBar now={Math.min(disc.studentCount || 0, 100)} variant="dark" style={{ height: '6px' }} className="mb-2" />
                          </div>
                          <div className="text-muted small mb-4">
                            Gerenciar suporte e dúvidas
                          </div>
                          <div className="d-grid gap-2">
                            <Button
                              variant="dark"
                              className="w-100 btn-sm py-2 fw-bold"
                              onClick={() => navigate(`/monitor-discipline/${disc.id}`)}
                            >
                              Gerenciar Disciplina
                            </Button>
                          </div>
                        </Card.Body>
                      </Card>
                    </Col>
                  ))
                ) : (
                  <div className="text-center py-5">
                    <p className="text-muted">Você ainda não é monitor de nenhuma disciplina.</p>
                  </div>
                )}
              </Row>
            )}
          </section>

        </Container>
      </main>
    </div>
  );
};

export default MonitorDashboard;
