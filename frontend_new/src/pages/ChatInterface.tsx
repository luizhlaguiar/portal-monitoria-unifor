import React, { useState, useEffect, useRef } from 'react';
import { Container, Row, Col, Card, Form, Button } from 'react-bootstrap';
import chatService, { ChatMessage } from '../services/ChatService';
import api from '../services/api';

interface Contact {
  id: number;
  nome: string;
  email: string;
}

interface MessageUI {
  id: number;
  senderId: number;
  senderName?: string;
  content: string;
  timestamp: string;
}

const ChatInterface: React.FC<{ contacts: Contact[], currentUserId: number | null }> = ({ contacts, currentUserId }) => {
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
  const [messages, setMessages] = useState<MessageUI[]>([]);
  const [inputText, setInputText] = useState('');
  const scrollRef = useRef<HTMLDivElement>(null);
  const selectedContactRef = useRef<Contact | null>(null);

  useEffect(() => {
    selectedContactRef.current = selectedContact;
  }, [selectedContact]);

  useEffect(() => {
    if (!currentUserId) return;

    chatService.ensureConnected(currentUserId, (msg: ChatMessage) => {
      setMessages((prev) => {
        const currentContact = selectedContactRef.current;
        const isFromMe = msg.senderId === currentUserId;
        const isFromSelectedContact = currentContact && msg.senderId === currentContact.id;

        if (isFromMe || isFromSelectedContact) {
          const isDuplicate = prev.some(m => m.content === msg.content && m.senderId === msg.senderId && (Date.now() - new Date(m.timestamp).getTime() < 2000));
          if (isDuplicate) return prev;

          return [...prev, {
            id: Date.now(),
            senderId: msg.senderId,
            senderName: msg.senderName,
            content: msg.content,
            timestamp: new Date().toISOString()
          }];
        }
        return prev;
      });
    });
  }, [currentUserId]);

  useEffect(() => {
    if (selectedContact) {
      loadChatHistory(selectedContact.id);
    }
  }, [selectedContact]);

  const loadChatHistory = async (contactId: number) => {
    try {
      const response = await api.get(`/chat/history/${currentUserId}/${contactId}`);

      let historyData = [];
      if (response.data && Array.isArray(response.data)) {
        historyData = response.data;
      } else if (response.data && response.data.content && Array.isArray(response.data.content)) {
        historyData = response.data.content;
      }

      const formattedHistory = historyData.map((msg: any) => ({
        id: msg.id,
        senderId: msg.sender?.id || msg.usuario_id || msg.senderId,
        senderName: msg.sender?.nome || msg.usuario_nome || (msg.senderId !== currentUserId ? selectedContact?.nome : 'Eu'),
        content: msg.content,
        timestamp: msg.timestamp
      })).reverse();

      setMessages(formattedHistory);
    } catch (error) {
      console.error('Erro ao carregar histórico do chat:', error);
    }
  };

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !selectedContact) return;

    const msg: ChatMessage = {
      type: 'CHAT',
      senderId: currentUserId!,
      receiverId: selectedContact.id,
      content: inputText,
    };

    chatService.sendMessage(msg);

    setMessages((prev) => [...prev, {
      id: Date.now(),
      senderId: currentUserId!,
      content: inputText,
      timestamp: new Date().toISOString()
    }]);

    setInputText('');
  };

  return (
    <Container fluid className="p-0">
      {/* Altura fixa para o container principal para evitar que a página inteira role */}
      <Row className="g-0" style={{ height: '600px', maxHeight: '80vh' }}>
        <Col md={4} className="border-end bg-light d-flex flex-column" style={{ height: '100%' }}>
          <div className="p-3 border-bottom bg-white sticky-top">
            <h5 className="fw-bold mb-0">Conversas</h5>
          </div>
          <div className="overflow-auto flex-grow-1">
            <div className="list-group list-group-flush">
              {contacts.map(contact => (
                <button
                  key={contact.id}
                  onClick={() => setSelectedContact(contact)}
                  className={`list-group-item list-group-item-action d-flex align-items-center gap-3 p-3 border-0 ${selectedContact?.id === contact.id ? 'bg-primary text-white' : ''}`}
                  style={{ cursor: 'pointer' }}
                >
                  <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${contact.nome}`} alt={contact.nome} className="rounded-circle" style={{width: '45px', height: '45px', border: '2px solid #eee'}} />
                  <div className="text-start">
                    <div className="fw-bold">{contact.nome}</div>
                    <small className={selectedContact?.id === contact.id ? 'text-white-50' : 'text-muted'}>Clique para conversar</small>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </Col>

        <Col md={8} className="d-flex flex-column bg-white" style={{ height: '100%' }}>
          {selectedContact ? (
            <>
              {/* Cabeçalho fixo */}
              <div className="p-3 border-bottom d-flex align-items-center gap-3 bg-white" style={{ flexShrink: 0 }}>
                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedContact.nome}`} alt={selectedContact.nome} className="rounded-circle" style={{width: '40px', height: '40px'}} />
                <h6 className="fw-bold mb-0">{selectedContact.nome}</h6>
              </div>

              {/* ÁREA DE MENSAGENS COM ROLAGEM INDEPENDENTE */}
              <div
                ref={scrollRef}
                className="flex-grow-1 p-4 overflow-auto"
                style={{
                  backgroundColor: '#e5ddd5',
                  backgroundImage: 'url("https://user-images.githubusercontent.com/15075759/28719144-86dc도f70-73b1-11e8-101a-72dc5ef8220d.jpg")',
                  backgroundBlendMode: 'overlay',
                  position: 'relative'
                }}
              >
                <div className="d-flex flex-column gap-3">
                  {messages.map((msg, index) => (
                    <div key={index} className={`d-flex ${msg.senderId === currentUserId ? 'justify-content-end' : 'justify-content-start'}`}>
                      <div className={`p-2 px-3 rounded-3 shadow-sm ${msg.senderId === currentUserId ? 'bg-primary text-white rounded-end-0' : 'bg-white text-dark rounded-start-0'}`} style={{ maxWidth: '70%' }}>
                        {msg.senderId !== currentUserId && (
                          <div className="fw-bold small mb-1" style={{ fontSize: '0.75rem' }}>
                            {msg.senderName || (selectedContact ? selectedContact.nome : 'Usuário')}
                          </div>
                        )}
                        <div className="small">{msg.content}</div>
                        <div className="text-end mt-1" style={{ fontSize: '0.7rem', opacity: 0.8 }}>
                          {new Date(msg.timestamp).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Rodapé fixo */}
              <Form onSubmit={handleSendMessage} className="p-3 border-top bg-light d-flex gap-2" style={{ flexShrink: 0 }}>
                <Form.Control
                  type="text"
                  placeholder="Digite sua mensagem..."
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}
                  className="shadow-sm"
                />
                <Button type="submit" variant="primary" className="px-4">Enviar</Button>
              </Form>
            </>
          ) : (
            <div className="flex-grow-1 d-flex flex-column align-items-center justify-content-center text-center p-5 text-muted">
              <img src="https://cdn-icons-png.flaticon.com/512/5968/5968771.png" alt="Chat" style={{width: '120px', opacity: 0.5}} className="mb-4" />
              <h4 className="mb-3">Selecione um contato para começar a conversar</h4>
              <p>As mensagens são privadas e ficam salvas no sistema.</p>
            </div>
          )}
        </Col>
      </Row>
    </Container>
  );
};

export default ChatInterface;
