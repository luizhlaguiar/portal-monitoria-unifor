import React, { useState } from 'react';
import { Container, Row, Col, Card, Button, Table, Form, Tabs, Tab, Badge } from 'react-bootstrap';
import { useNavigate, useParams } from 'react-router-dom';
import './Dashboard.css';

interface Schedule {
  id: string;
  day: string;
  time: string;
  location: string;
}

interface Activity {
  id: string;
  title: string;
  description: string;
  deadline: string;
}

const ManageDiscipline: React.FC = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [schedules, setSchedules] = useState<Schedule[]>([
    { id: '1', day: 'Segunda', time: '14:00 - 16:00', location: 'Sala 201' },
    { id: '2', day: 'Quarta', time: '10:00 - 12:00', location: 'Online' },
  ]);

  const [activities, setActivities] = useState<Activity[]>([
    { id: '1', title: 'Exercícios de Flexbox', description: 'Implementar layout responsivo', deadline: '2026-10-20' },
  ]);

  const [newActivity, setNewActivity] = useState({ title: '', description: '', deadline: '' });

  const handleAddActivity = () => {
    if (newActivity.title) {
      setActivities([...activities, { ...newActivity, id: Date.now().toString() }]);
      setNewActivity({ title: '', description: '', deadline: '' });
    }
  };

  return (
    <Container className="py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <Button variant="link" className="p-0 text-decoration-none mb-2" onClick={() => navigate('/monitor-dashboard')}>
            ← Voltar ao Painel do Monitor
          </Button>
          <h2 className="fw-bold">Gerenciar Disciplina: {id}</h2>
        </div>
        <Button variant="danger" onClick={() => alert('Atenção: Ação crítica!')}>Sair da Monitoria</Button>
      </div>

      <Tabs defaultActiveKey="horarios" id="manage-tabs" className="mb-4 custom-tabs">
        <Tab eventKey="horarios" title="📅 Gestão de Horários">
          <Card className="border-0 shadow-sm p-4 bg-white">
            <div className="d-flex justify-content-between align-items-center mb-3">
              <h5 className="fw-bold">Meus Horários de Atendimento</h5>
              <Button variant="dark" size="sm" className="fw-bold">+ Adicionar Horário</Button>
            </div>
            <Table responsive hover>
              <thead className="bg-light text-muted small">
                <tr>
                  <th>Dia</th>
                  <th>Horário</th>
                  <th>Local/Link</th>
                  <th className="text-center">Ações</th>
                </tr>
              </thead>
              <tbody>
                {schedules.map(s => (
                  <tr key={s.id}>
                    <td>{s.day}</td>
                    <td>{s.time}</td>
                    <td>{s.location}</td>
                    <td className="text-center">
                      <Button variant="outline-dark" size="sm" className="me-2">Editar</Button>
                      <Button variant="outline-danger" size="sm">Excluir</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Card>
        </Tab>

        <Tab eventKey="atividades" title="📚 Atividades e Materiais">
          <div className="row g-4">
            <Col lg={4}>
              <Card className="border-0 shadow-sm p-4 bg-white">
                <h5 className="fw-bold mb-3">Nova Atividade</h5>
                <Form onSubmit={(e) => { e.preventDefault(); handleAddActivity(); }}>
                  <Form.Group className="mb-3">
                    <Form.Label className="small fw-bold">Título</Form.Label>
                    <Form.Control
                      value={newActivity.title}
                      onChange={e => setNewActivity({...newActivity, title: e.target.value})}
                      placeholder="Ex: Lista de Exercícios 1"
                    />
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label className="small fw-bold">Descrição</Form.Label>
                    <Form.Control
                      as="textarea"
                      rows={3}
                      value={
                      newActivity.description}
                      onChange={e => setNewActivity({...newActivity, description: e.target.value})}
                    />
                  </Form.Group>
                  <Form.Group className="mb-3">
                    <Form.Label className="small fw-bold">Prazo</Form.Label>
                    <Form.Control
                      type="date"
                      value={newActivity.deadline}
                      onChange={e => setNewActivity({...newActivity, deadline: e.target.value})}
                    />
                  </Form.Group>
                  <Button variant="dark" className="w-100 fw-bold" onClick={handleAddActivity}>Publicar Atividade</Button>
                </Form>
              </Card>
            </Col>

            <Col lg={8}>
              <Card className="border-0 shadow-sm p-4 bg-white">
                <h5 className="fw-bold mb-3">Atividades Publicadas</h5>
                {activities.map(act => (
                  <Card key={act.id} className="mb-3 border-0 bg-light">
                    <Card.Body className="p-3 d-flex justify-content-between align-items-center">
                      <div>
                        <h6 className="fw-bold mb-1">{act.title}</h6>
                        <p className="small text-muted mb-0">{act.description}</p>
                        <span className="badge bg-secondary mt-2">Prazo: {act.deadline}</span>
                      </div>
                      <div className="d-flex gap-2">
                        <Button variant="outline-dark" size="sm">Editar</Button>
                        <Button variant="outline-danger" size="sm">Remover</Button>
                      </div>
                    </Card.Body>
                  </Card>
                ))}
              </Card>
            </Col>
          </div>
        </Tab>

        <Tab eventKey="alunos" title="👥 Meus Alunos">
          <Card className="border-0 shadow-sm p-4 bg-white">
            <h5 className="fw-bold mb-3">Alunos Matriculados na Monitoria</h5>
            <Table responsive hover>
              <thead className="bg-light text-muted small">
                <tr>
                  <th>Nome</th>
                  <th>Matrícula</th>
                  <th>Email</th>
                  <th className="text-center">Ações</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Lucas Oliveira</td>
                  <td>2024001234</td>
                  <td>lucas.oliveira@edu.unifor.br</td>
                  <td className="text-center">
                    <Button variant="outline-dark" size="sm">Enviar Aviso</Button>
                  </td>
                </tr>
                <tr>
                  <td>Maria Souza</td>
                  <td>2024005678</td>
                  <td>maria.souza@edu.unifor.br</td>
                  <td className="text-center">
                    <Button variant="outline-dark" size="sm">Enviar Aviso</Button>
                  </td>
                </tr>
              </tbody>
            </Table>
          </Card>
        </Tab>
      </Tabs>
    </Container>
  );
};

export default ManageDiscipline;