import React, { useState, useEffect, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Container, Row, Col, Card, Navbar, Nav, Button, Spinner } from 'react-bootstrap';
import api from '../services/api';
import './Dashboard.css';

interface Discipline {
  id: number;
  code: string;
  name: string;
  department: string;
  description: string;
  monitorCount?: number;
  upcomingSchedules?: number;
  doubtCount?: number;
  status?: string;
  color?: string;
}

const normalize = (text: string) =>
  (text || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();

const Dashboard: React.FC = () => {
  const navigate = useNavigate();
  const [disciplines, setDisciplines] = useState<Discipline[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');

  const user = JSON.parse(localStorage.getItem('user') || '{}');
  const role = localStorage.getItem('role') || 'aluno';
  const isStaff = role.toUpperCase() === 'ADMIN' || role.toUpperCase() === 'MONITOR';

  useEffect(() => {
    const fetchDisciplines = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/disciplines/student/${user.id}`);
        const list: any[] = Array.isArray(response.data) ? response.data : [];

        const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'];
        const dataWithColors = await Promise.all(
          list.map(async (d: any, index: number) => {
            let monitorCount: number | undefined;
            try {
              const monitors = await api.get(`/disciplines/${d.id}/monitors`);
              monitorCount = Array.isArray(monitors.data) ? monitors.data.length : 0;
            } catch (error) {
              console.error(`Erro ao carregar monitores da disciplina ${d.id}:`, error);
            }
            let upcomingSchedules: number | undefined;
            try {
              const schedules = await api.get(`/disciplines/${d.id}/schedules`);
              const today = new Date().toLocaleDateString('sv-SE');
              upcomingSchedules = Array.isArray(schedules.data)
                ? schedules.data.filter((s: any) => {
                    const day = String(s.date).slice(0, 10);
                    const start = new Date(`${day}T${s.startTime}`);
                    return isNaN(start.getTime()) ? day >= today : start.getTime() > Date.now();
                  }).length
                : 0;
            } catch (error) {
              console.error(`Erro ao carregar horários da disciplina ${d.id}:`, error);
            }
            let doubtCount: number | undefined;
            try {
              const duvidas = await api.get(`/forum/${d.id}`);
              doubtCount = Array.isArray(duvidas.data) ? duvidas.data.length : 0;
            } catch (error) {
              console.error(`Erro ao carregar dúvidas da disciplina ${d.id}:`, error);
            }
            return { ...d, color: colors[index % colors.length], monitorCount, upcomingSchedules, doubtCount };
          })
        );

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

  const filteredDisciplines = useMemo(() => {
    const term = normalize(search);
    if (!term) return disciplines;
    return disciplines.filter(d => normalize(d.name).includes(term));
  }, [disciplines, search]);

  const sumOf = (pick: (d: Discipline) => number | undefined): number | null =>
    disciplines.some(d => pick(d) === undefined)
      ? null
      : disciplines.reduce((acc, d) => acc + (pick(d) || 0), 0);
  const totalSchedules = sumOf(d => d.upcomingSchedules);
  const totalDoubts = sumOf(d => d.doubtCount);

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
              <span className="stat-value h2 fw-bold">{loading ? '…' : totalSchedules ?? '–'}</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-info border-4 text-center">
              <span className="stat-label text-muted small fw-bold">DÚVIDAS NO FÓRUM</span>
              <span className="stat-value h2 fw-bold">{loading ? '…' : totalDoubts ?? '–'}</span>
            </div>
          </section>

          <section className="disciplines-section text-center">
            <div className="section-header d-flex justify-content-between align-items-center mb-4 text-start">
              <h2 className="fw-bold mb-0">Minhas Disciplinas</h2>
              <input
                type="search"
                placeholder="Pesquisar disciplina..."
                aria-label="Pesquisar disciplina"
                className="form-control"
                style={{ maxWidth: '320px', minWidth: '200px' }}
                value={search}
                onChange={e => setSearch(e.target.value)}
              />
            </div>

            {loading ? (
              <div className="text-center py-5">
                <Spinner animation="border" variant="dark" />
                <p className="mt-2 text-muted">Carregando suas disciplinas...</p>
              </div>
            ) : (
              <Row className="g-4 justify-content-center">
                {filteredDisciplines.length > 0 ? (
                  filteredDisciplines.map(disc => (
                    <Col key={disc.id} xs={12} sm={6} lg={4} xl={3}>
                      <Card className="h-100 border-0 shadow-sm overflow-hidden discipline-card-modern">
                        <div className="banner-disciplina" style={{ backgroundColor: disc.color, height: '80px' }}>
                           <div className="banner-overlay"></div>
                        </div>
                        <Card.Body className="p-4">
                          <div className="mb-3">
                            <h5 className="fw-bold mb-1">{disc.name}</h5>
                          </div>
                          {disc.monitorCount !== undefined && (
                            <div className="text-muted small mb-4">
                              {disc.monitorCount === 1
                                ? '1 monitor disponível'
                                : `${disc.monitorCount} monitores disponíveis`}
                            </div>
                          )}
                          <div className="d-grid gap-2">
                            <Link to={`/discipline/${disc.id}`} className="btn btn-dark w-100 btn-sm py-2 fw-bold">Ver Detalhes</Link>
                          </div>
                        </Card.Body>
                      </Card>
                    </Col>
                  ))
                ) : (
                  <div className="text-center py-5">
                    <p className="text-muted">
                      {disciplines.length === 0
                        ? 'Você ainda não está matriculado em nenhuma disciplina.'
                        : `Nenhuma disciplina encontrada para "${search.trim()}".`}
                    </p>
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
