import React, { useState, useEffect, useRef } from 'react';
<<<<<<< HEAD
import { Container, Row, Col, Form, Button, Modal, ListGroup } from 'react-bootstrap';
=======
import { Container, Row, Col, Card, Form, Button, Modal } from 'react-bootstrap';
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
import chatService, { ChatMessage } from '../services/ChatService';
import api from '../services/api';
import { storageService } from '../services/storageService';
import './Forum.css';

interface Contact {
  id: number;
  nome: string;
  email: string;
  role?: string;
}

interface MessageUI {
  id: number;
  senderId: number;
  senderName?: string;
  content: string;
  arquivoUrl?: string;
  timestamp: string;
  isImage?: boolean;
  isPdf?: boolean;
}

<<<<<<< HEAD
const ChatInterface: React.FC<{ contacts: Contact[], currentUserId: number | null }> = ({ contacts: initialContacts, currentUserId }) => {
  // Estado de contatos agora é dinâmico e sincronizado com o localStorage
  const [contacts, setContacts] = useState<Contact[]>(() => {
    const savedContacts = localStorage.getItem('chat_contacts');
    if (savedContacts) {
      try { return JSON.parse(savedContacts); } catch (e) { /* fallback */ }
    }
    return initialContacts;
  });

  const [selectedContact, setSelectedContact] = useState<Contact | null>(() => {
    const saved = localStorage.getItem('selected_contact');
    return saved ? JSON.parse(saved) : null;
  });

  const [messages, setMessages] = useState<MessageUI[]>([]);
  const [inputText, setInputText] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);
  
  const [showNewChatModal, setShowNewChatModal] = useState(false);
  const [allSystemUsers, setAllSystemUsers] = useState<Contact[]>([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  // Paginação para o modal (15 por página)
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 15;

  const fileInputRef = useRef<HTMLInputElement>(null);
=======
const ChatInterface: React.FC<{ currentUserId: number | null }> = ({ currentUserId }) => {
  const [contacts, setContacts] = useState<Contact[]>([]);
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
  const [messages, setMessages] = useState<MessageUI[]>([]);
  const [inputText, setInputText] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [showContactModal, setShowContactModal] = useState(false);
  const [availableContacts, setAvailableContacts] = useState<Contact[]>([]);
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
  const scrollRef = useRef<HTMLDivElement>(null);
  const selectedContactRef = useRef<Contact | null>(null);

  useEffect(() => {
    selectedContactRef.current = selectedContact;
  }, [selectedContact]);

  // Persistir contatos no localStorage sempre que houver nova adição
  useEffect(() => {
    localStorage.setItem('chat_contacts', JSON.stringify(contacts));
  }, [contacts]);

  useEffect(() => {
    if (selectedContact) {
      localStorage.setItem('selected_contact', JSON.stringify(selectedContact));
    } else {
      localStorage.removeItem('selected_contact');
    }
  }, [selectedContact]);

  const isImg = (content: string) => {
    if (!content) return false;
    return content.includes('data:image') || content.startsWith('data:image');
  };

  const isPdf = (content: string) => {
    if (!content) return false;
    return content.includes('data:application/pdf') || content.includes('[PDF:');
  };

  useEffect(() => {
    if (!currentUserId) return;

<<<<<<< HEAD
    chatService.connect(currentUserId, (msg: ChatMessage) => {
      const contentStr = msg.content || '';
      setMessages((prev) => [...prev, {
        id: Date.now(),
        senderId: msg.senderId,
        senderName: msg.senderName,
        content: contentStr,
        timestamp: new Date().toISOString(),
        isImage: isImg(contentStr),
        isPdf: isPdf(contentStr)
      }]);
=======
    const loadActiveConversations = async () => {
      try {
        const userStr = localStorage.getItem('user');
        if (!userStr) return;
        const user = JSON.parse(userStr);

        if (user.role === 'MONITOR') {
          const res = await api.get(`/chat/conversations/${currentUserId}`);
          setContacts(res.data);
        } else {
          // Alunos devem ver as conversas ativas que já possuem com monitores,
          // além de poderem iniciar novas.
          const res = await api.get(`/chat/conversations/${currentUserId}`);
          setContacts(res.data);
        }
      } catch (error) {
        console.error('Erro ao carregar conversas ativas:', error);
      }
    };

    loadActiveConversations();

    chatService.ensureConnected(currentUserId, (msg: ChatMessage) => {
      setMessages((prev) => {
        const currentContact = selectedContactRef.current;
        const isFromMe = msg.senderId === currentUserId;
        const isFromSelectedContact = currentContact && msg.senderId === currentContact.id;

        if (isFromMe || isFromSelectedContact) {
          const isDuplicate = prev.some(m => m.content === msg.content && m.senderId === msg.senderId && m.arquivoUrl === msg.arquivoUrl && (Date.now() - new Date(m.timestamp).getTime() < 2000));
          if (isDuplicate) return prev;

          return [...prev, {
            id: Date.now(),
            senderId: msg.senderId,
            senderName: msg.senderName,
            content: msg.content,
            arquivoUrl: msg.arquivoUrl,
            timestamp: new Date().toISOString()
          }];
        }
        return prev;
      });
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
    });
  }, [currentUserId]);

  const handleOpenContactModal = async () => {
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr) return;
      const user = JSON.parse(userStr);

      if (user.role === 'MONITOR') {
        const res = await api.get('/chat/alunos');
        setAvailableContacts(res.data);
      } else {
        const res = await api.get('/chat/monitors');
        setAvailableContacts(res.data);
      }
      setShowContactModal(true);
    } catch (error) {
      console.error('Erro ao carregar contatos disponíveis:', error);
      alert('Erro ao carregar lista de contatos');
    }
  };

  useEffect(() => {
    if (selectedContact && currentUserId) {
      loadChatHistory(selectedContact.id);
    }
  }, [selectedContact, currentUserId]);

  const loadChatHistory = async (contactId: number) => {
    if (!currentUserId || !contactId) return;
    try {
<<<<<<< HEAD
      const response = await api.get(`/api/chat/history/${currentUserId}/${contactId}`).catch(() => 
        api.get(`/chat/history/${currentUserId}/${contactId}`)
      );
=======
      const response = await api.get(`/chat/history/${currentUserId}/${contactId}`);
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602

      const historyData = Array.isArray(response.data) ? response.data : [];

<<<<<<< HEAD
      const formattedHistory = historyData.map((msg: any) => {
        const contentStr = msg.content || '';
        const sId = msg.sender?.id !== undefined ? msg.sender.id : (msg.usuario_id !== undefined ? msg.usuario_id : msg.senderId);
        const sName = msg.sender?.nome || msg.usuario_nome || (sId !== currentUserId ? selectedContact?.nome : 'Eu');

        return {
          id: msg.id || Date.now() + Math.random(),
          senderId: sId,
          senderName: sName,
          content: contentStr,
          timestamp: msg.timestamp || new Date().toISOString(),
          isImage: isImg(contentStr),
          isPdf: isPdf(contentStr)
        };
      });
=======
      const formattedHistory = historyData.map((msg: any) => ({
        id: msg.id,
        senderId: msg.sender?.id || msg.usuario_id || msg.senderId,
        senderName: msg.sender?.nome || msg.usuario_nome || (msg.senderId !== currentUserId ? selectedContact?.nome : 'Eu'),
        content: msg.content,
        arquivoUrl: msg.arquivoUrl,
        timestamp: msg.timestamp
      })).reverse();
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602

      setMessages(formattedHistory);
    } catch (error) {
      console.error('Erro ao carregar histórico:', error);
    }
  };

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

<<<<<<< HEAD
  const handleOpenNewChatModal = async () => {
    try {
      const response = await api.get('/api/usuarios').catch(() => api.get('/usuarios'));
      if (response && response.data) {
        const users = response.data.filter((u: any) => u.id !== currentUserId);
        setAllSystemUsers(users);
        setSearchTerm('');
        setSelectedUserId(null);
        setCurrentPage(1);
        setShowNewChatModal(true);
      }
    } catch (error) {
      console.error('Erro ao buscar usuários:', error);
      alert('Não foi possível carregar a lista de usuários do sistema.');
    }
  };

  const handleStartConversation = () => {
    if (!selectedUserId) {
      alert('Selecione um usuário.');
      return;
    }
    const userToAdd = allSystemUsers.find(u => u.id === selectedUserId);
    if (userToAdd) {
      setContacts(prev => {
        if (!prev.some(c => c.id === userToAdd.id)) {
          return [userToAdd, ...prev];
        }
        return prev;
      });
      setSelectedContact(userToAdd);
      setShowNewChatModal(false);
      setSelectedUserId(null);
    }
  };

  // Filtragem e Paginação de Usuários
  const filteredUsers = allSystemUsers.filter(u => 
    u.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalPages = Math.ceil(filteredUsers.length / itemsPerPage) || 1;
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentUsersPage = filteredUsers.slice(startIndex, startIndex + itemsPerPage);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setSelectedFile(file);
      
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault();
    if ((!inputText.trim() && !selectedFile) || !selectedContact || !currentUserId) return;

    let contentToSend = inputText;

    if (selectedFile && previewUrl) {
      if (selectedFile.type.startsWith('image/')) {
        contentToSend = previewUrl;
      } else {
        const fileName = selectedFile.name || 'documento.pdf';
        contentToSend = `[PDF:${fileName}]${previewUrl}`;
      }
    }

    const msg: ChatMessage = {
      type: 'CHAT',
      senderId: currentUserId,
      receiverId: selectedContact.id,
      content: contentToSend,
    };
=======
  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    if ((!inputText.trim() && !selectedFile) || !selectedContact) return;

    try {
      let arquivoUrl = '';
      if (selectedFile) {
        setIsUploading(true);
        arquivoUrl = await storageService.uploadFile(selectedFile, 'chat');
      }
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602

      const msg: ChatMessage = {
        type: 'CHAT',
        senderId: currentUserId!,
        receiverId: selectedContact.id,
        content: inputText,
        arquivoUrl: arquivoUrl,
      };

<<<<<<< HEAD
    setMessages((prev) => [...prev, {
      id: Date.now(),
      senderId: currentUserId,
      content: contentToSend,
      timestamp: new Date().toISOString(),
      isImage: isImg(contentToSend),
      isPdf: isPdf(contentToSend)
    }]);

    setInputText('');
    setSelectedFile(null);
    setPreviewUrl(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const openMediaSecurely = (content: string, mimeTypeDefault: string) => {
    try {
      let base64Data = content;
      let mime = mimeTypeDefault;

      if (content.startsWith('[PDF:')) {
        const match = content.match(/\[PDF:(.*?)\](.*)/);
        if (match) base64Data = match[2];
      }

      const arr = base64Data.split(',');
      if (arr.length > 1 && arr[0].includes(':')) {
        mime = arr[0].match(/:(.*?);/)?.[1] || mimeTypeDefault;
      }

      const bstr = atob(arr.length > 1 ? arr[1] : arr[0]);
      let n = bstr.length;
      const u8arr = new Uint8Array(n);
      while (n--) {
        u8arr[n] = bstr.charCodeAt(n);
      }
      const blob = new Blob([u8arr], { type: mime });
      const blobUrl = URL.createObjectURL(blob);
      window.open(blobUrl, '_blank');
    } catch (e) {
      console.error('Erro ao abrir mídia:', e);
      alert('Não foi possível abrir o arquivo.');
    }
  };

  const parsePdfName = (content: string) => {
    if (content.startsWith('[PDF:')) {
      const match = content.match(/\[PDF:(.*?)\](.*)/);
      if (match) return match[1];
    }
    return 'Documento.pdf';
=======
      chatService.sendMessage(msg);

      setMessages((prev) => [...prev, {
        id: Date.now(),
        senderId: currentUserId!,
        content: inputText,
        arquivoUrl: arquivoUrl,
        timestamp: new Date().toISOString()
      }]);

      setInputText('');
      setSelectedFile(null);
    } catch (error) {
      console.error('DETALHES DO ERRO NO CHAT:', error);
      alert(`Erro ao enviar mensagem: ${error instanceof Error ? error.message : 'Erro desconhecido'}`);
    } finally {
      setIsUploading(false);
    }
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
  };

  return (
    <Container fluid className="p-0">
<<<<<<< HEAD
      <Row className="g-0" style={{ height: '600px' }}>
        <Col md={4} className="border-end bg-light d-flex flex-column" style={{ height: '600px' }}>
          <div className="p-3 border-bottom bg-white d-flex justify-content-between align-items-center">
=======
      <Row className="g-0" style={{ height: '600px', maxHeight: '80vh' }}>
        <Col md={4} className="border-end bg-light d-flex flex-column" style={{ height: '100%' }}>
          <div className="p-3 border-bottom bg-white sticky-top">
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
            <h5 className="fw-bold mb-0">Conversas</h5>
            <Button variant="outline-primary" size="sm" onClick={handleOpenNewChatModal}>
              + Nova Conversa
            </Button>
          </div>
<<<<<<< HEAD
          
          <div className="list-group list-group-flush flex-grow-1" style={{ overflowY: 'auto' }}>
            {contacts.map(contact => (
              <button
                key={contact.id}
                onClick={() => setSelectedContact(contact)}
                className={`list-group-item list-group-item-action d-flex align-items-center gap-3 p-3 border-0 ${selectedContact?.id === contact.id ? 'bg-primary text-white' : ''}`}
                style={{ cursor: 'pointer' }}
=======
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
            <div className="p-3">
              <Button
                variant="outline-primary"
                className="w-100"
                onClick={handleOpenContactModal}
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
              >
                + Nova Conversa
              </Button>
            </div>
          </div>
        </Col>

<<<<<<< HEAD
        <Col md={8} className="d-flex flex-column bg-white" style={{ height: '600px' }}>
=======
        <Col md={8} className="d-flex flex-column bg-white" style={{ height: '100%' }}>
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
          {selectedContact ? (
            <>
              <div className="p-3 border-bottom d-flex align-items-center gap-3 bg-white" style={{ flexShrink: 0 }}>
                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${selectedContact.nome}`} alt={selectedContact.nome} className="rounded-circle" style={{width: '40px', height: '40px'}} />
                <h6 className="fw-bold mb-0">{selectedContact.nome}</h6>
              </div>

              <div
                ref={scrollRef}
                className="flex-grow-1 p-4 overflow-auto"
                style={{
                  backgroundColor: '#e5ddd5',
                  backgroundImage: 'url("https://user-images.githubusercontent.com/15075759/28719144-86dc-f70-73b1-11e8-101a-72dc5ef8220d.jpg")',
                  backgroundBlendMode: 'overlay',
                  position: 'relative'
                }}
              >
                <div className="d-flex flex-column gap-3">
                  {messages.map((msg, index) => {
                    const imageCheck = msg.isImage || isImg(msg.content);
                    const pdfCheck = msg.isPdf || isPdf(msg.content);
                    
                    return (
                      <div key={index} className={`d-flex ${msg.senderId === currentUserId ? 'justify-content-end' : 'justify-content-start'}`}>
                        <div className={`p-2 px-3 rounded-3 shadow-sm ${msg.senderId === currentUserId ? 'bg-primary text-white rounded-end-0' : 'bg-white text-dark rounded-start-0'}`} style={{ maxWidth: '70%' }}>
                          {msg.senderId !== currentUserId && (
                            <div className="fw-bold small mb-1" style={{ fontSize: '0.75rem' }}>
                              {msg.senderName || selectedContact?.nome}
                            </div>
                          )}
                          
                          {imageCheck ? (
                            <img 
                              src={msg.content} 
                              alt="Anexo" 
                              className="img-fluid rounded mb-1" 
                              style={{ maxHeight: '250px', maxWidth: '100%', objectFit: 'contain', display: 'block', cursor: 'pointer' }} 
                              onClick={() => openMediaSecurely(msg.content, 'image/png')}
                              title="Clique para abrir em tamanho real"
                            />
                          ) : pdfCheck ? (
                            <div className="p-2 bg-light rounded border text-dark d-flex align-items-center gap-2">
                              <span style={{ fontSize: '1.5rem' }}>📄</span>
                              <div>
                                <div className="fw-bold small text-truncate" style={{ maxWidth: '180px' }}>{parsePdfName(msg.content)}</div>
                                <button 
                                  onClick={() => openMediaSecurely(msg.content, 'application/pdf')}
                                  className="btn btn-link p-0 small text-primary text-decoration-underline border-0 bg-transparent"
                                >
                                  Abrir PDF
                                </button>
                              </div>
                            </div>
                          ) : (
                            <div className="small" style={{ wordBreak: 'break-word' }}>{msg.content}</div>
                          )}

                          <div className="text-end mt-1" style={{ fontSize: '0.7rem', opacity: 0.8 }}>
                            {new Date(msg.timestamp || Date.now()).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                          </div>
<<<<<<< HEAD
=======
                        )}
                        {msg.arquivoUrl && (
                          <div className="mb-2">
                            <a href={msg.arquivoUrl} target="_blank" rel="noopener noreferrer">
                              <img src={msg.arquivoUrl} alt="Anexo" style={{ maxWidth: '100%', maxHeight: '200px', borderRadius: '8px', border: '1px solid #ddd' }} />
                            </a>
                          </div>
                        )}
                        <div className="small">{msg.content}</div>
                        <div className="text-end mt-1" style={{ fontSize: '0.7rem', opacity: 0.8 }}>
                          {new Date(msg.timestamp).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>

<<<<<<< HEAD
              {selectedFile && (
                <div className="px-3 py-2 bg-light border-top d-flex justify-content-between align-items-center">
                  <div className="d-flex align-items-center gap-2">
                    {previewUrl && selectedFile.type.startsWith('image/') && (
                      <img src={previewUrl} alt="Preview" style={{ width: '35px', height: '35px', objectFit: 'cover' }} className="rounded" />
                    )}
                    <small className="text-muted">📎 Anexo pronto: <strong>{selectedFile.name}</strong></small>
                  </div>
                  <Button variant="link" size="sm" className="text-danger p-0 text-decoration-none" onClick={() => { setSelectedFile(null); setPreviewUrl(null); }}>Remover</Button>
                </div>
              )}

              <Form onSubmit={handleSendMessage} className="p-3 border-top bg-light d-flex gap-2 align-items-center">
                <input
                  type="file"
                  ref={fileInputRef}
                  style={{ display: 'none' }}
                  accept="image/*,.pdf"
                  onChange={handleFileChange}
                />
                <Button 
                  variant="outline-secondary" 
                  title="Enviar Imagem ou PDF"
                  onClick={() => fileInputRef.current?.click()}
                >
                  📎
                </Button>

                <Form.Control
                  type="text"
                  placeholder="Digite sua mensagem ou envie um anexo..."
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}
                  className="shadow-sm"
                />
                <Button type="submit" variant="primary" className="px-4">Enviar</Button>
=======
              <Form onSubmit={handleSendMessage} className="p-3 border-top bg-light d-flex gap-2 align-items-end" style={{ flexShrink: 0 }}>
                <div className="d-flex gap-2 align-items-end flex-grow-1">
                  <div className="position-relative">
                    <Form.Control
                      type="file"
                      accept="image/*"
                      style={{ display: 'none' }}
                      onChange={e => setSelectedFile(e.target.files ? e.target.files[0] : null)}
                    />
                    <Button
                      variant="outline-secondary"
                      className="rounded-circle p-2"
                      style={{ width: '40px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                      onClick={() => document.querySelector('input[type="file"]')?.click()}
                    >
                      📎
                    </Button>
                  </div>
                  <Form.Control
                    type="text"
                    placeholder={isUploading ? "Enviando arquivo..." : "Digite sua mensagem..."}
                    value={inputText}
                    onChange={(e) => setInputText(e.target.value)}
                    className="shadow-sm"
                    disabled={isUploading}
                  />
                </div>
                <div className="d-flex flex-column align-items-center gap-1">
                  {selectedFile && (
                    <div className="badge bg-info text-dark" style={{ fontSize: '0.6rem', whiteSpace: 'nowrap' }}>
                      📷 {selectedFile.name.substring(0, 10)}...
                      <button
                        type="button"
                        className="btn-close ms-1"
                        style={{ fontSize: '0.5rem', padding: '0' }}
                        onClick={() => setSelectedFile(null)}
                      ></button>
                    </div>
                  )}
                  <Button type="submit" variant="primary" className="px-4" disabled={isUploading}>
                    {isUploading ? '...' : 'Enviar'}
                  </Button>
                </div>
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
              </Form>
            </>
          ) : (
            <div className="flex-grow-1 d-flex flex-column align-items-center justify-content-center text-center p-5 text-muted">
              <h4 className="mb-3">Selecione um contato para começar a conversar</h4>
              <p>As mensagens são privadas e ficam salvas no sistema.</p>
            </div>
          )}
        </Col>
      </Row>

<<<<<<< HEAD
      <Modal show={showNewChatModal} onHide={() => setShowNewChatModal(false)} centered size="lg">
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Iniciar Nova Conversa</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group className="mb-3">
            <Form.Label className="fw-bold small text-muted">Pesquisar usuário por nome ou e-mail</Form.Label>
            <Form.Control
              type="text"
              placeholder="Digite o nome do usuário..."
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setCurrentPage(1); // Reseta para a primeira página ao pesquisar
              }}
              className="mb-3"
              autoFocus
            />
          </Form.Group>

          <div style={{ minHeight: '320px', maxHeight: '320px', overflowY: 'auto' }} className="border rounded mb-3">
            <ListGroup variant="flush">
              {currentUsersPage.length > 0 ? (
                currentUsersPage.map(u => (
                  <ListGroup.Item
                    key={u.id}
                    action
                    active={selectedUserId === u.id}
                    onClick={() => setSelectedUserId(u.id)}
                    className="d-flex align-items-center gap-3 py-2"
                  >
                    <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${u.nome}`} alt={u.nome} className="rounded-circle" style={{width: '35px', height: '35px'}} />
                    <div>
                      <div className="fw-bold">{u.nome}</div>
                      <small className={selectedUserId === u.id ? 'text-white-50' : 'text-muted'}>
                        {u.role || 'Usuário'} • {u.email}
                      </small>
                    </div>
                  </ListGroup.Item>
                ))
              ) : (
                <div className="text-center p-4 text-muted">Nenhum usuário encontrado</div>
              )}
            </ListGroup>
          </div>

          {/* Paginação do Modal */}
          {totalPages > 1 && (
            <div className="d-flex justify-content-between align-items-center">
              <Button 
                variant="outline-secondary" 
                size="sm" 
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              >
                Anterior
              </Button>
              <small className="text-muted">Página {currentPage} de {totalPages} ({filteredUsers.length} usuários)</small>
              <Button 
                variant="outline-secondary" 
                size="sm" 
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              >
                Próxima
              </Button>
            </div>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowNewChatModal(false)}>Cancelar</Button>
          <Button variant="primary" onClick={handleStartConversation} disabled={!selectedUserId}>Começar a Conversar</Button>
        </Modal.Footer>
=======
      <Modal show={showContactModal} onHide={() => setShowContactModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Nova Conversa</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="list-group">
            {availableContacts.map(contact => (
              <button
                key={contact.id}
                onClick={() => {
                  setSelectedContact(contact);
                  setShowContactModal(false);
                }}
                className="list-group-item list-group-item-action d-flex align-items-center gap-3 p-3"
                style={{ cursor: 'pointer' }}
              >
                <img src={`https://api.dicebear.com/7.x/avataaars/svg?seed=${contact.nome}`} alt={contact.nome} className="rounded-circle" style={{width: '35px', height: '35px'}} />
                <div className="text-start">
                  <div className="fw-bold">{contact.nome}</div>
                  <small className="text-muted">{contact.email}</small>
                </div>
              </button>
            ))}
          </div>
        </Modal.Body>
>>>>>>> c5c2ad548415447bc4d01c4fe32550bcde3a5602
      </Modal>
    </Container>
  );
};

export default ChatInterface;