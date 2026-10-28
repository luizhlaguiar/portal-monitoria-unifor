import React, { useState, useEffect } from 'react';
import { Button, Modal, Form } from 'react-bootstrap';
import api from '../services/api';
import ChatInterface from './ChatInterface';
import './Messages.css';

interface Contact {
  id: number;
  nome: string;
  email: string;
  role?: string;
}

const Messages: React.FC = () => {
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [showNewChatModal, setShowNewChatModal] = useState(false);
  const [availableUsers, setAvailableUsers] = useState<Contact[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  useEffect(() => {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      setCurrentUserId(user.id);
      fetchConversations(user.id, user.role);
    }
  }, []);

  const fetchConversations = async (userId: number, role: string) => {
    try {
      const endpoint = role === 'MONITOR' ? `/chat/conversas/monitor/${userId}` : `/chat/conversas/aluno/${userId}`;
      const response = await api.get(endpoint);
      if (response.data && Array.isArray(response.data)) {
        setContacts(response.data);
      }
    } catch (error) {
      console.error('Erro ao buscar conversas:', error);
      // Fallback para listar usuários caso a rota específica falhe
      try {
        const fallbackRes = await api.get(`/usuarios`);
        if (fallbackRes.data) {
          setContacts(fallbackRes.data.filter((u: any) => u.id !== userId));
        }
      } catch (e) {
        console.error('Erro no fallback', e);
      }
    }
  };

  // Abre modal para iniciar conversa com QUALQUER usuário (disponível para Aluno e Monitor)
  const handleOpenNewChatModal = async () => {
    try {
      const response = await api.get('/usuarios');
      if (response.data) {
        // Filtra para remover o próprio usuário logado
        const users = response.data.filter((u: any) => u.id !== currentUserId);
        setAvailableUsers(users);
        setShowNewChatModal(true);
      }
    } catch (error) {
      console.error('Erro ao carregar usuários:', error);
      alert('Erro ao carregar lista de usuários.');
    }
  };

  const handleStartConversation = () => {
    if (!selectedUserId) {
      alert('Selecione um usuário.');
      return;
    }
    const userToAdd = availableUsers.find(u => u.id === selectedUserId);
    if (userToAdd) {
      if (!contacts.some(c => c.id === userToAdd.id)) {
        setContacts(prev => [userToAdd, ...prev]);
      }
      setShowNewChatModal(false);
      setSelectedUserId(null);
    }
  };

  if (!currentUserId) {
    return <div className="text-center p-5">Carregando dados do usuário...</div>;
  }

  return (
    <div className="p-4" style={{ height: 'calc(100vh - 80px)' }}>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h3 className="fw-bold mb-0">Mensagens & Chat</h3>
        <Button variant="primary" onClick={handleOpenNewChatModal}>
          + Iniciar Nova Conversa
        </Button>
      </div>

      <div className="bg-white border rounded shadow-sm overflow-hidden" style={{ height: '80vh' }}>
        <ChatInterface contacts={contacts} currentUserId={currentUserId} />
      </div>

      {/* Modal para Aluno ou Monitor escolherem com quem conversar */}
      <Modal show={showNewChatModal} onHide={() => setShowNewChatModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Iniciar Nova Conversa</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group className="mb-3">
            <Form.Label className="fw-bold small">Selecione o Usuário</Form.Label>
            <Form.Select 
              value={selectedUserId || ''} 
              onChange={e => setSelectedUserId(Number(e.target.value))}
            >
              <option value="">Escolha um usuário...</option>
              {availableUsers.map(u => (
                <option key={u.id} value={u.id}>
                  {u.nome} ({u.role || 'Usuário'}) - {u.email}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowNewChatModal(false)}>Cancelar</Button>
          <Button variant="primary" onClick={handleStartConversation}>Começar a Conversar</Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
};
 
export default Messages; 