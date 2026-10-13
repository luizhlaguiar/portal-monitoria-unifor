import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Tabs, Tab, Badge } from 'react-bootstrap';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import Forum from './Forum';
import ChatInterface from './ChatInterface';
import './Discipline.css';

interface Schedule {
  id: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  room: string;
  monitoringSession: {
    monitor: {
      nome: string;
    };
  };
}

interface Activity {
  id: number;
  titulo: string;
  prazo: string;
  descricao: string;
}

interface Monitor {
  id: number;
  nome: string;
  email: string;
}

const Discipline: React.FC = () => {
  const { id } = useParams();
  const [monitors, setMonitors] = useState<Monitor[]>([]);
  const [schedules, setSchedules] = useState<Schedule[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [disciplineName, setDisciplineName] = useState('Carregando...');

  useEffect(() => {
    const fetchAllData = async () => {
      // Fetch Discipline Name
      try {
        const discRes = await api.get(`/disciplines/${id}`);
        setDisciplineName(discRes.data.name);
      } catch (error) {
        console.error('Erro ao carregar nome da disciplina:', error);
        setDisciplineName('Disciplina não encontrada');
      }

      // Fetch Monitors
      try {
        const monRes = await api.get(`/disciplines/${id}/monitors`);
        setMonitors(monRes.data);
      } catch (error) {
        console.error('Erro ao carregar monitores:', error);
      }

      // Fetch Schedules
      try {
        const schRes = await api.get(`/disciplines/${id}/schedules`);
        setSchedules(schRes.data);
      } catch (error) {
        console.error('Erro ao carregar horários:', error);
      }

      // Fetch Activities
      try {
        const actRes = await api.get(`/disciplines/${id}/activities`);
        setActivities(actRes.data);
      } catch (error) {
        console.error('Erro ao carregar atividades:', error);
      }
    };

    if (id) fetchAllData();
  }, [id]);

  return (
    <Container className="py-5">
      <nav aria-label="breadcrumb" className="mb-4">
        <ol className="breadcrumb small font-monospace">
          <li className="breadcrumb-item"><Link to="/dashboard" className="text-decoration-none">Dashboard</Link></li>
          <li className="breadcrumb-item active">{disciplineName}</li>
        </ol>
      </nav>

      <Card className="border-0 shadow-sm mb-5 overflow-hidden">
        <div className="banner-disciplina" style={{ backgroundColor: '#4e73df', height: '120px' }}>
          <div className="banner-overlay"></div>
          <div className="banner-text text-end p-3 text-white fw-bold">
            <span className="opacity-75 small font-monospace">ID: {id}</span>
            <div className="h4 mb-0">{disciplineName}</div>
          </div>
        </div>
        <Card.Body className="p-4 d-flex justify-content-between align-items-center">
          <div>
            <h3 className="fw-bold mb-1">{disciplineName}</h3>
            <p className="text-muted mb-0">Acompanhe os horários de monitoria e atividades de fixação.</p>
          </div>
          <Button variant="dark" className="fw-bold px-4">Ir para o Fórum</Button>
        </Card.Body>
      </Card>

      <Tabs defaultActiveKey="monitores" id="discipline-tabs" className="mb-4 custom-tabs">
        <Tab eventKey="monitores" title="Monitores">
          <div className="p-4 bg-white border rounded shadow-sm">
            <Row className="g-4">
              {monitors.map(mon => (
                <Col key={mon.id} md={6}>
                  <Card className="border-0 shadow-sm p-3 monitor-card-modern">
                    <div className="d-flex align-items-center gap-3">
                      <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${mon.nome}`} alt={mon.nome} className="rounded-circle" style={{width: '60px', height: '60px', border: '2px solid #eee'}} />
                      <div className="flex-grow-1">
                        <h6 className="fw-bold mb-0">{mon.nome}</h6>
                        <small className="text-muted">{mon.email}</small>
                      </div>
                      <Button variant="outline-dark" size="sm" className="fw-bold">Chat</Button>
                    </div>
                  </Card>
                </Col>
              ))}
              {monitors.length === 0 && <Col className="text-center w-100"><p className="text-muted">Nenhum monitor encontrado.</p></Col>}
            </Row>
          </div>
        </Tab>

        <Tab eventKey="horarios" title="Horários">
          <div className="p-4 bg-white border rounded shadow-sm">
            <Table responsive hover className="align-middle">
              <thead className="bg-light text-muted small">
                <tr className="text-center">
                  <th className="border-0">Dia da Semana</th>
                  <th className="border-0">Horário</th>
                  <th className="border-0">Sala</th>
                  <th className="border-0">Monitor Responsável</th>
                  <th className="border-0 text-center">Ação</th>
                </tr>
              </thead>
              <tbody className="text-center small">
                {schedules.map(item => (
                  <tr key={item.id} className="border-bottom">
                    <td>{item.dayOfWeek}</td>
                    <td>{item.startTime} - {item.endTime}</td>
                    <td>{item.room}</td>
                    <td className="fw-bold">{item.monitoringSession?.monitor?.nome || 'Não informado'}</td>
                    <td className="text-center"><Button variant="dark" size="sm">Agendar</Button></td>
                  </tr>
                ))}
                {schedules.length === 0 && <tr><td colSpan={5} className="text-center py-3 text-muted">Nenhum horário cadastrado.</td></tr>}
              </tbody>
            </Table>
          </div>
        </Tab>

        <Tab eventKey="atividades" title="Atividades">
          <div className="p-4 bg-white border rounded shadow-sm">
            <Row className="g-3">
              {activities.map(act => (
                <Col xs={12} key={act.id}>
                  <Card className="border-0 shadow-sm p-3 mb-2">
                    <div className="d-flex justify-content-between align-items-start">
                      <div>
                        <h6 className="fw-bold mb-1">{act.titulo}</h6>
                        <p className="small text-muted mb-0">
                          Prazo: {act.prazo ? new Date(act.prazo).toLocaleString('pt-BR') : 'Sem limite de entrega'}
                        </p>
                      </div>
                      <Badge bg="primary">Atividade</Badge>
                    </div>
                    <p className="small text-muted mt-2">{act.descricao}</p>
                    <Button variant="link" className="p-0 text-decoration-none small fw-bold">Ver Instruções →</Button>
                  </Card>
                </Col>
              ))}
              {activities.length === 0 && <Col className="text-center w-100"><p className="text-muted">Nenhuma atividade de fixação disponível.</p></Col>}
            </Row>
          </div>
        </Tab>

        <Tab eventKey="forum" title="Fórum">
          <div className="p-4 bg-white border rounded shadow-sm">
            <Forum disciplineId={id} />
          </div>
        </Tab>

        <Tab eventKey="mensagens" title="Mensagens">
          <div className="p-4 bg-white border rounded shadow-sm">
            {(() => {
              const user = JSON.parse(localStorage.getItem('user') || '{}');
              return <ChatInterface contacts={monitors} currentUserId={user.id} />;
            })()}
          </div>
        </Tab>
      </Tabs>
    </Container>
  );
};

export default Discipline;