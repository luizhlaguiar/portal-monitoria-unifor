import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Table, Tabs, Tab, Badge, Modal, Form } from 'react-bootstrap';
import { useParams, useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import Forum from "./Forum";
import ChatInterface from "./ChatInterface";
import './Dashboard.css';

interface Schedule {
  id: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  room: string;
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

interface Contact {
  id: number;
  nome: string;
  email: string;
}

const MonitorDisciplineDetail: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [monitors, setMonitors] = useState<Monitor[]>([]);
  const [schedule, setSchedule] = useState<Schedule[]>([]);
  const [activities, setActivities] = useState<Activity[]>([]);
  const [conversations, setConversations] = useState<Contact[]>([]);

  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [newSchedule, setNewSchedule] = useState({
    dayOfWeek: 'Segunda',
    startTime: '',
    endTime: '',
    room: ''
  });

  const [showActivityModal, setShowActivityModal] = useState(false);
  const [newActivity, setNewActivity] = useState({
    titulo: '',
    prazo: '',
    descricao: '',
    noDeadline: false
  });

  const fetchAllData = async () => {
    try {
      const [monRes, schRes, actRes] = await Promise.all([
        api.get(`/disciplines/${id}/monitors`),
        api.get(`/disciplines/${id}/schedules`),
        api.get(`/disciplines/${id}/activities`),
      ]);
      setMonitors(monRes.data);
      setSchedule(schRes.data);
      setActivities(actRes.data);
    } catch (error) {
      console.error('Erro ao carregar dados da disciplina:', error);
    }
  };

  const fetchConversations = async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const res = await api.get(`/chat/conversations/${user.id}`);
      setConversations(res.data);
    } catch (error) {
      console.error('Erro ao carregar conversas do monitor:', error);
    }
  };

  useEffect(() => {
    fetchAllData();
    fetchConversations();
  }, [id]);

  const handleAddSchedule = async () => {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      const sessionRes = await api.get(`/disciplines/${id}/session/${user.id}`);
      const sessionId = sessionRes.data;

      await api.post('/schedules', {
        ...newSchedule,
        monitoringSession: { id: sessionId }
      });
      await fetchAllData();
      setShowScheduleModal(false);
      setNewSchedule({ dayOfWeek: 'Segunda', startTime: '', endTime: '', room: '' });
    } catch (error) {
      console.error(error);
      alert('Erro ao adicionar horário. Verifique se você está vinculado a esta disciplina como monitor.');
    }
  };

  const handleRemoveSchedule = async (scheduleId: number) => {
    try {
      await api.delete(`/schedules/${scheduleId}`);
      await fetchAllData();
    } catch (error) {
      alert('Erro ao remover horário');
    }
  };

  const handleAddActivity = async () => {
    try {
      const activityData = {
        ...newActivity,
        prazo: newActivity.noDeadline ? null : newActivity.prazo,
        discipline: { id: Number(id) }
      };
      await api.post('/activities', activityData);
      await fetchAllData();
      setShowActivityModal(false);
      setNewActivity({ titulo: '', prazo: '', descricao: '', noDeadline: false });
    } catch (error) {
      alert('Erro ao adicionar atividade');
    }
  };

  const handleRemoveActivity = async (activityId: number) => {
    try {
      await api.delete(`/activities/${activityId}`);
      await fetchAllData();
    } catch (error) {
      alert('Erro ao remover atividade');
    }
  };

  const user = JSON.parse(localStorage.getItem('user') || '{}');

  return (
    <Container className="py-5">
      <nav aria-label="breadcrumb" className="mb-4">
        <ol className="breadcrumb small font-monospace">
          <li className="breadcrumb-item"><Link to="/monitor-dashboard" className="text-decoration-none">Painel do Monitor</Link></li>
          <li className="breadcrumb-item active">Gestão de Disciplina</li>
        </ol>
      </nav>

      <Card className="border-0 shadow-sm mb-5 overflow-hidden">
        <div className="banner-disciplina" style={{ backgroundColor: '#4e73df', height: '120px' }}>
          <div className="banner-overlay"></div>
          <div className="banner-text text-end p-3 text-white fw-bold">
            <span className="opacity-75 small font-monospace">ID: {id}</span>
            <div className="h4 mb-0">Gestão da Disciplina</div>
          </div>
        </div>
        <Card.Body className="p-4 d-flex justify-content-between align-items-center">
          <div>
            <h3 className="fw-bold mb-1">Painel Administrativo</h3>
            <p className="text-muted mb-0">Modo Editor: Você pode alterar horários e atividades.</p>
          </div>
          <Button variant="dark" className="fw-bold px-4" onClick={() => navigate('/monitor-dashboard')}>Voltar</Button>
        </Card.Body>
      </Card>

      <Tabs defaultActiveKey="monitores" id="monitor-discipline-tabs" className="mb-4 custom-tabs">

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
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold mb-0">Gerenciar Horários de Atendimento</h5>
              <Button variant="dark" size="sm" onClick={() => setShowScheduleModal(true)}>+ Adicionar Horário</Button>
            </div>
            <Table responsive hover className="align-middle">
              <thead className="bg-light text-muted small">
                <tr className="text-center">
                  <th className="border-0">Dia</th>
                  <th className="border-0">Início</th>
                  <th className="border-0">Fim</th>
                  <th className="border-0 text-center">Ação</th>
                </tr>
              </thead>
              <tbody className="text-center small">
                {schedule.map(item => (
                  <tr key={item.id} className="border-bottom">
                    <td className="fw-bold">{item.dayOfWeek}</td>
                    <td>{item.startTime}</td>
                    <td>{item.endTime}</td>
                    <td className="text-center">
                      <Button variant="outline-danger" size="sm" onClick={() => handleRemoveSchedule(item.id)}>Remover</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </div>
        </Tab>

        <Tab eventKey="atividades" title="Atividades">
          <div className="p-4 bg-white border rounded shadow-sm">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold mb-0">Gerenciar Atividades de Fixação</h5>
              <Button variant="dark" size="sm" onClick={() => setShowActivityModal(true)}>+ Nova Atividade</Button>
            </div>
            <Row className="g-3">
              {activities.map(act => (
                <Col xs={12} key={act.id}>
                  <Card className="border-0 shadow-sm p-3 mb-2">
                    <div className="d-flex justify-content-between align-items-start">
                      <div className="flex-grow-1">
                        <h6 className="fw-bold mb-1">{act.titulo}</h6>
                        <p className="small text-muted mb-0">Prazo: {act.prazo}</p>
                      </div>
                      <Button variant="outline-danger" size="sm" onClick={() => handleRemoveActivity(act.id)}>Excluir</Button>
                    </div>
                    <p className="small text-muted mt-2">{act.descricao}</p>
                  </Card>
                </Col>
              ))}
            </Row>
          </div>
        </Tab>

        <Tab eventKey="forum" title="Fórum">
          <Forum disciplineId={id} />
        </Tab>

        <Tab eventKey="mensagens" title="Mensagens">
          <div className="p-4 bg-white border rounded shadow-sm">
            {conversations.length > 0 ? (
              <ChatInterface
                contacts={conversations}
                currentUserId={user.id}
              />
            ) : (
              <div className="text-center py-5">
                <img src="https://cdn-icons-png.flaticon.com/512/5968/5968771.png" alt="Chat" style={{width: '100px', opacity: 0.5}} className="mb-3" />
                <h5 className="fw-bold">Nenhuma conversa ativa</h5>
                <p className="text-muted">Quando um aluno enviar uma mensagem, ela aparecerá aqui.</p>
              </div>
            )}
          </div>
        </Tab>
      </Tabs>

      {/* Modal Horário */}
      <Modal show={showScheduleModal} onHide={() => setShowScheduleModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Novo Horário de Monitoria</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Dia da Semana</Form.Label>
              <Form.Select
                value={newSchedule.dayOfWeek}
                onChange={e => setNewSchedule({...newSchedule, dayOfWeek: e.target.value})}
              >
                <option>Segunda</option>
                <option>Terça</option>
                <option>Quarta</option>
                <option>Quinta</option>
                <option>Sexta</option>
                <option>Sábado</option>
                <option>Domingo</option>
              </Form.Select>
            </Form.Group>
            <Row>
              <Col>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-bold">Início</Form.Label>
                  <Form.Control
                    type="time"
                    value={newSchedule.startTime}
                    onChange={e => setNewSchedule({...newSchedule, startTime: e.target.value})}
                  />
                </Form.Group>
              </Col>
              <Col>
                <Form.Group className="mb-3">
                  <Form.Label className="small fw-bold">Fim</Form.Label>
                  <Form.Control
                    type="time"
                    value={newSchedule.endTime}
                    onChange={e => setNewSchedule({...newSchedule, endTime: e.target.value})}
                  />
                </Form.Group>
              </Col>
            </Row>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Sala/Local</Form.Label>
              <Form.Control
                type="text"
                placeholder="Ex: Lab 01"
                value={newSchedule.room}
                onChange={e => setNewSchedule({...newSchedule, room: e.target.value})}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowScheduleModal(false)}>Cancelar</Button>
          <Button variant="dark" onClick={handleAddSchedule}>Salvar Horário</Button>
        </Modal.Footer>
      </Modal>

      {/* Modal Atividade */}
      <Modal show={showActivityModal} onHide={() => setShowActivityModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Nova Atividade de Fixação</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Título da Atividade</Form.Label>
              <Form.Control
                type="text"
                placeholder="Ex: Atividade 1 - Flexbox"
                value={newActivity.titulo}
                onChange={e => setNewActivity({...newActivity, titulo: e.target.value})}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Prazo (Deadline)</Form.Label>
              <div className="d-flex gap-2 align-items-center mb-2">
                <Form.Check
                  type="switch"
                  id="no-deadline"
                  label="Sem limite de entrega"
                  checked={newActivity.noDeadline}
                  onChange={e => setNewActivity({...newActivity, noDeadline: e.target.checked})}
                />
              </div>
              <Form.Control
                type="datetime-local"
                disabled={newActivity.noDeadline}
                value={newActivity.prazo}
                onChange={e => setNewActivity({...newActivity, prazo: e.target.value})}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Descrição</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                placeholder="Instruções da atividade..."
                value={newActivity.descricao}
                onChange={e => setNewActivity({...newActivity, descricao: e.target.value})}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowActivityModal(false)}>Cancelar</Button>
          <Button variant="dark" onClick={handleAddActivity}>Salvar Atividade</Button>
        </Modal.Footer>
      </Modal>
    </Container>
  );
};

export default MonitorDisciplineDetail;
