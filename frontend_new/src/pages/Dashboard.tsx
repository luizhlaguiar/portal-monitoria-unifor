import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, ProgressBar, Navbar, Nav, Button, Spinner } from 'react-bootstrap';
import api from '../services/api';
import './Dashboard.css';

interface Discipline {
  id: number;
  code: string;
  name: string;
  department: string;
  description: string;
  monitorCount?: number;
  status?: string;
  progress?: number;
  color?: string;
}

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [disciplines, setDisciplines] = useState<Discipline[]>([]);
  const [loading, setLoading] = useState(true);

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const role = localStorage.getItem('role') || 'aluno';
  const isStaff = role.toUpperCase() === 'ADMIN' || role.toUpperCase() === 'MONITOR';

  useEffect(() => {
    const fetchDisciplines = async () => {
      try {
        setLoading(true);
        // Busca as disciplinas do aluno logado usando o ID do usuário vindo do banco
        const response = await api.get(`/disciplines/student/${user.id}`);

        // Como o banco agora retorna os dados reais, vamos adicionar cores aleatórias
        // para manter o visual bonito da dashboard
        const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'];
        const dataWithColors = response.data.map((d: any, index: number) => ({
          ...d,
          color: colors[index % colors.length],
          progress: Math.floor(Math.random() * 100), // Simulando progresso
          monitorCount: 1 // Valor padrão
        }));

        setDisciplines(dataWithColors);
      } catch (error) {
        console.error('Erro ao carregar disciplinas:', error);
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

  const handleMonitorView = () => {
    navigate('/monitor-dashboard');
  };

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
            {isStaff && (
              <Button variant="warning" size="sm" className="fw-bold me-2" onClick={handleMonitorView}>
                Visão de Monitor
              </Button>
            )}
            <div className="d-flex align-items-center gap-3 ms-3">
              <span className="text-light opacity-75 small">{user.nome || 'Usuário'} ({role})</span>
              <Link to="/login" className="btn btn-outline-light btn-sm">Sair</Link>
            </div>
          </Nav>
        </Navbar.Collapse>
      </Navbar>

      <main className="main-content">
        <Container className="py-5">
          <header className="content-header mb-5 text-center">
            <div className="header-title text-center">
              <h1 className="fw-bold">Olá, {user.nome || 'Luiz'}!</h1>
              <p className="text-muted">Bem-vindo ao seu portal de monitoria. Gerencie suas disciplinas e dúvidas aqui.</p>
            </div>
          </header>

          <section className="stats-grid mb-5">
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-primary border-4 text-center">
              <span className="stat-label text-muted small fw-bold">DISCIPLINAS</span>
              <span className="stat-value h2 fw-bold">{disciplines.length}</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-success border-4 text-center">
              <span className="stat-label text-muted small fw-bold">MONITORIAS AGENDADAS</span>
              <span className="stat-value h2 fw-bold">2</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-info border-4 text-center">
              <span className="stat-label text-muted small fw-bold">DÚVIDAS NO FÓRUM</span>
              <span className="stat-value h2 fw-bold">5</span>
            </div>
          </section>

          <section className="disciplines-section text-center">
            <div className="section-header d-flex justify-content-between align-items-center mb-4 text-start">
              <h2 className="fw-bold mb-0">Minhas Disciplinas</h2>
              <input type="text" placeholder="Pesquisar disciplina..." className="form-control w-25" />
            </div>

            {loading ? (
              <div className="text-center py-5">
                <Spinner animation="border" variant="dark" />
                <p className="mt-2 text-muted">Carregando suas disciplinas...</p>
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
                              <span>Progresso</span>
                              <span>{disc.progress}%</span>
                            </div>
                            <ProgressBar now={disc.progress} variant="dark" style={{ height: '6px' }} className="mb-2" />
                          </div>
                          <div className="text-muted small mb-4">
                            {disc.monitorCount} monitores disponíveis
                          </div>
                          <div className="d-grid gap-2">
                            <Link to={`/discipline/${disc.id}`} className="btn btn-dark w-100 btn-sm py-2 fw-bold">Ver Detalhes</Link>
                          </div>
                        </Card.Body>
                      </Card>
                    </Col>
                  ))
                ) : (
                  <div className="text-center py-5">
                    <p className="text-muted">Você ainda não está matriculado em nenhuma disciplina.</p>
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

export default Dashboard;
