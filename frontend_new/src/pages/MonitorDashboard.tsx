import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Navbar, Nav, Spinner } from 'react-bootstrap';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import './Dashboard.css';

interface MonitorDiscipline {
  id: number;
  name: string;
  code: string;
  studentCount?: number;
  doubtCount?: number;
  heldMinutes?: number;
  attendeeIds?: number[];
  nextSession?: string | null;
  color?: string;
}

const formatHours = (minutes: number | null) => {
  if (minutes === null) return '–';
  const h = Math.floor(minutes / 60);
  const m = Math.round(minutes % 60);
  return m ? `${h}h${String(m).padStart(2, '0')}` : `${h}h`;
};
const MonitorDashboard: React.FC = () => {
  const [disciplines, setDisciplines] = useState<MonitorDiscipline[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    const fetchDisciplines = async () => {
      try {
        setLoading(true);
        const response = await api.get(`/disciplines/monitor/${user.id}`);
        const list: any[] = Array.isArray(response.data) ? response.data : [];

        const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'];
        const now = Date.now();
        const dayOf = (s: any) => String(s.date).slice(0, 10);
        const startMs = (s: any) => new Date(`${dayOf(s)}T${s.startTime}`).getTime() || 0;
        const minutesOf = (s: any) => {
          const diff = (new Date(`${dayOf(s)}T${s.endTime}`).getTime() - startMs(s)) / 60000;
          return diff > 0 ? diff : 0;
        };

        const dataWithColors: MonitorDiscipline[] = await Promise.all(
          list.map(async (d: any, index: number) => {
            const item: MonitorDiscipline = { ...d, color: colors[index % colors.length] };

            try {
              const r = await api.get(`/chat/students/${d.id}`);
              item.studentCount = Array.isArray(r.data) ? r.data.length : 0;
            } catch (error) {
              console.error(`Erro ao carregar alunos da disciplina ${d.id}:`, error);
            }

            try {
              const r = await api.get(`/forum/${d.id}`);
              item.doubtCount = Array.isArray(r.data) ? r.data.length : 0;
            } catch (error) {
              console.error(`Erro ao carregar dúvidas da disciplina ${d.id}:`, error);
            }

            try {
              const r = await api.get(`/disciplines/${d.id}/schedules`);
              const mine = (Array.isArray(r.data) ? r.data : []).filter(
                (s: any) => s.monitoringSession?.monitor?.id === user.id
              );
              const held = mine.filter((s: any) => startMs(s) <= now);
              const upcoming = mine
                .filter((s: any) => startMs(s) > now)
                .sort((a: any, b: any) => startMs(a) - startMs(b));

              item.heldMinutes = held.reduce((acc: number, s: any) => acc + minutesOf(s), 0);
              item.nextSession = upcoming.length
                ? new Date(startMs(upcoming[0])).toLocaleString('pt-BR', {
                    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit'
                  })
                : null;

              const presencas = await Promise.all(held.map((s: any) => api.get(`/presencas/horario/${s.id}`)));
              const ids = new Set<number>();
              presencas.forEach(res =>
                (Array.isArray(res.data) ? res.data : []).forEach((p: any) => {
                  if (p.user?.id != null) ids.add(p.user.id);
                })
              );
              item.attendeeIds = Array.from(ids);
            } catch (error) {
              console.error(`Erro ao carregar monitorias da disciplina ${d.id}:`, error);
            }

            return item;
          })
        );
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

  const known = <T,>(pick: (d: MonitorDiscipline) => T | undefined): T[] | null => {
    const values = disciplines.map(pick);
    return values.some(v => v === undefined) ? null : (values as T[]);
  };
  const minutesList = known(d => d.heldMinutes);
  const minutesTotal = minutesList ? minutesList.reduce((a, b) => a + b, 0) : null;
  const doubtList = known(d => d.doubtCount);
  const doubtsTotal = doubtList ? doubtList.reduce((a, b) => a + b, 0) : null;
  const attendeeLists = known(d => d.attendeeIds);
  const attendeesTotal = attendeeLists ? new Set(attendeeLists.flat()).size : null;

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
              <span className="stat-value h2 fw-bold">{loading ? '…' : attendeesTotal ?? '–'}</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-primary border-4 text-center">
              <span className="stat-label text-muted small fw-bold">HORAS DE MONITORIA</span>
              <span className="stat-value h2 fw-bold">{loading ? '…' : formatHours(minutesTotal)}</span>
            </div>
            <div className="stat-card shadow-sm p-4 bg-white rounded border-start border-danger border-4 text-center">
              <span className="stat-label text-muted small fw-bold">DÚVIDAS NO FÓRUM</span>
              <span className="stat-value h2 fw-bold">{loading ? '…' : doubtsTotal ?? '–'}</span>
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
                        </div>
                        <Card.Body className="p-4">
                          <div className="mb-3">
                            <h5 className="fw-bold mb-1">{disc.name}</h5>
                          </div>
                          <div className="small text-muted mb-4">
                            <div className="d-flex justify-content-between mb-1">
                              <span>Alunos matriculados</span>
                              <span className="fw-bold text-dark">{disc.studentCount ?? '–'}</span>
                            </div>
                            <div className="d-flex justify-content-between mb-1">
                              <span>Dúvidas no fórum</span>
                              <span className="fw-bold text-dark">{disc.doubtCount ?? '–'}</span>
                            </div>
                            <div className="d-flex justify-content-between">
                              <span>Sua próxima monitoria</span>
                              <span className="fw-bold text-dark">
                                {disc.nextSession === undefined ? '–' : disc.nextSession || 'Nenhuma agendada'}
                              </span>
                            </div>
                          </div>                          <div className="d-grid gap-2">
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
