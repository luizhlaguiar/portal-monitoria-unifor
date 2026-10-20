import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Button, Badge, Modal, Form, ListGroup } from 'react-bootstrap';
import api from '../services/api';
import './Forum.css';

interface Duvida {
  id: number;
  titulo: string;
  descricao: string;
  status: 'PENDENTE' | 'EM_ANALISE' | 'RESOLVIDA';
  dataCriacao: string;
  alunoNome: string;
  alunoId: number;
  aluno?: { id: number; nome: string };
  respostas: any[];
}

interface Resposta {
  id: number;
  conteudo: string;
  dataResposta: string;
  usuarioNome: string;
  usuarioId: number;
  usuario?: { id: number; nome: string };
  respostaPaiId?: number;
  respostasFilhas?: Resposta[];
}

// COMPONENTE MOVIDO PARA FORA DO FORUM PARA EVITAR PERDA DE FOCO NO INPUT
const RenderResposta: React.FC<{
  resposta: Resposta,
  depth: number,
  editingRespostaId: number | null,
  editingRespostaText: string,
  setEditingRespostaId: (id: number | null) => void,
  setEditingRespostaText: (text: string) => void,
  handleDeleteResposta: (id: number) => void,
  handleEditResposta: () => void,
  setReplyingToResponseId: (id: number | null) => void,
  setNovaResposta: (text: string) => void
}> = ({
  resposta,
  depth,
  editingRespostaId,
  editingRespostaText,
  setEditingRespostaId,
  setEditingRespostaText,
  handleDeleteResposta,
  handleEditResposta,
  setReplyingToResponseId,
  setNovaResposta
}) => {
  return (
    <div style={{ marginLeft: depth > 0 ? '2rem' : '0' }} className="mb-3">
      <Card className="border-0 shadow-sm p-3 bg-light">
        <div className="d-flex justify-content-between mb-1">
          <small className="fw-bold">{resposta?.usuarioNome || 'Usuário'}</small>
          <div className="d-flex gap-2 align-items-center">
            {(() => {
              const userStr = localStorage.getItem('user');
              if (!userStr) return null;
              const user = JSON.parse(userStr);
              const isAuthor = Number(resposta?.usuarioId) === Number(user.id);

              return isAuthor ? (
                <>
                  <Button variant="link" size="sm" className="p-0 text-decoration-none" onClick={() => {
                    setEditingRespostaId(resposta.id);
                    setEditingRespostaText(resposta.conteudo);
                  }}>Editar</Button>
                  <Button variant="link" size="sm" className="p-0 text-decoration-none text-danger" onClick={() => handleDeleteResposta(resposta.id)}>Excluir</Button>
                </>
              ) : null;
            })()}
            <small className="text-muted" style={{ fontSize: '0.7rem' }}>{resposta?.dataResposta ? new Date(resposta.dataResposta).toLocaleString() : ''}</small>
          </div>
        </div>

        {editingRespostaId === resposta.id ? (
          <div className="d-flex gap-2 mt-2">
            <Form.Control
              size="sm"
              autoFocus
              value={editingRespostaText}
              onChange={e => setEditingRespostaText(e.target.value)}
            />
            <Button size="sm" variant="dark" onClick={handleEditResposta}>Salvar</Button>
            <Button size="sm" variant="secondary" onClick={() => setEditingRespostaId(null)}>Cancelar</Button>
          </div>
        ) : (
          <p className="small mb-2">{resposta?.conteudo}</p>
        )}

        <Button variant="link" size="sm" className="p-0 text-decoration-none small fw-bold" onClick={() => {
          setReplyingToResponseId(resposta.id);
          setNovaResposta('');
          window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
        }}>Responder</Button>

        {resposta.respostasFilhas && resposta.respostasFilhas.length > 0 && (
          <div className="mt-3 border-start ps-3">
            {resposta.respostasFilhas.map((filha) => (
              <RenderResposta
                key={filha.id}
                resposta={filha}
                depth={depth + 1}
                editingRespostaId={editingRespostaId}
                editingRespostaText={editingRespostaText}
                setEditingRespostaId={setEditingRespostaId}
                setEditingRespostaText={setEditingRespostaText}
                handleDeleteResposta={handleDeleteResposta}
                handleEditResposta={handleEditResposta}
                setReplyingToResponseId={setReplyingToResponseId}
                setNovaResposta={setNovaResposta}
              />
            ))}
          </div>
        )}
      </Card>
    </div>
  );
};

const Forum: React.FC<{ disciplineId: string }> = ({ disciplineId }) => {
  const [duvidas, setDuvidas] = useState<Duvida[]>([]);
  const [selectedDuvida, setSelectedDuvida] = useState<Duvida | null>(null);
  const [respostas, setRespostas] = useState<Resposta[]>([]);
  const [novaResposta, setNovaResposta] = useState('');
  const [loading, setLoading] = useState(true);
  const [showNewDuvidaModal, setShowNewDuvidaModal] = useState(false);
  const [novaDuvida, setNovaDuvida] = useState({ titulo: '', descricao: '' });

  const [showEditDuvidaModal, setShowEditDuvidaModal] = useState(false);
  const [editingDuvida, setEditingDuvida] = useState({ titulo: '', descricao: '' });
  const [editingRespostaId, setEditingRespostaId] = useState<number | null>(null);
  const [editingRespostaText, setEditingRespostaText] = useState('');
  const [replyingToResponseId, setReplyingToResponseId] = useState<number | null>(null);
  const [isPosting, setIsPosting] = useState(false);

  const fetchDuvidas = async () => {
    try {
      const res = await api.get(`/forum/${disciplineId}`);
      if (res.data && Array.isArray(res.data)) {
        setDuvidas(res.data);
      } else {
        setDuvidas(res.data || []);
      }
    } catch (error) {
      console.error('[Forum] Erro na requisição:', error);
      setDuvidas([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (disciplineId) fetchDuvidas();
  }, [disciplineId]);

  const handleOpenTopic = (duvida: Duvida) => {
    setSelectedDuvida(duvida);
    setRespostas(duvida.respostas || []);
  };

  const handlePostReply = async () => {
    if (!selectedDuvida || !novaResposta.trim() || isPosting) return;
    try {
      setIsPosting(true);
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      await api.post(`/forum/duvida/${selectedDuvida.id}/resposta`, {
        usuarioId: user.id,
        conteudo: novaResposta,
        respostaPaiId: replyingToResponseId
      });

      setNovaResposta('');
      setReplyingToResponseId(null);

      const res = await api.get(`/forum/${disciplineId}`);
      setDuvidas(res.data);
      const updatedDuvida = res.data.find((d: any) => d.id === selectedDuvida.id);
      if (updatedDuvida) {
        setSelectedDuvida(updatedDuvida);
        setRespostas(updatedDuvida.respostas || []);
      }

    } catch (error) {
      alert('Erro ao enviar resposta');
    } finally {
      setIsPosting(false);
    }
  };

  const handleCreateDuvida = async () => {
    if (!novaDuvida.titulo.trim() || !novaDuvida.descricao.trim()) {
      alert('Por favor, preencha título e descrição.');
      return;
    }
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      await api.post('/forum/duvida', {
        alunoId: user.id,
        disciplinaId: Number(disciplineId),
        titulo: novaDuvida.titulo,
        descricao: novaDuvida.descricao
      });

      setNovaDuvida({ titulo: '', descricao: '' });
      setShowNewDuvidaModal(false);
      await fetchDuvidas();
    } catch (error) {
      alert('Erro ao criar dúvida.');
    }
  };

  const handleEditDuvida = async () => {
    if (!selectedDuvida || !editingDuvida.titulo.trim() || !editingDuvida.descricao.trim()) {
      alert('Preencha título e descrição.');
      return;
    }
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      await api.put(`/forum/duvida/${selectedDuvida.id}`, {
        usuarioId: user.id,
        titulo: editingDuvida.titulo,
        descricao: editingDuvida.descricao
      });
      setShowEditDuvidaModal(false);

      setDuvidas(prev => prev.map(d => d.id === selectedDuvida.id ? { ...d, titulo: editingDuvida.titulo, descricao: editingDuvida.descricao } : d));
      setSelectedDuvida(prev => prev ? { ...prev, titulo: editingDuvida.titulo, descricao: editingDuvida.descricao } : null);
    } catch (error) {
      alert('Erro ao editar dúvida.');
    }
  };

  const handleDeleteDuvida = async (id: number) => {
    if (!window.confirm('Tem certeza que deseja excluir esta pergunta?')) return;
    try {
      const userStr = localStorage.getItem('user');
      if (!userStr) {
        alert('Erro: Usuário não autenticado!');
        return;
      }
      const user = JSON.parse(userStr);

      await api.delete(`/forum/duvida/${id}?usuarioId=${user.id}`);

      alert('Dúvida excluída com sucesso!');
      setDuvidas(prev => prev.filter(d => d.id !== id));
      setSelectedDuvida(null);
    } catch (error: any) {
      console.error('[Forum] Erro detalhado na exclusão:', error);
      const errMsg = error.response?.data?.message || error.message || 'Erro desconhecido';
      alert(`Erro no servidor: ${errMsg}`);
    }
  };

  const handleEditResposta = async () => {
    if (!editingRespostaId || !editingRespostaText.trim()) return;
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      await api.put(`/forum/resposta/${editingRespostaId}`, {
        usuarioId: user.id,
        conteudo: editingRespostaText
      });
      setEditingRespostaId(null);
      setEditingRespostaText('');
      setRespostas(prev => prev.map(r => r.id === editingRespostaId ? { ...r, conteudo: editingRespostaText } : r));
    } catch (error) {
      alert('Erro ao editar resposta.');
    }
  };

  const handleDeleteResposta = async (id: number) => {
    if (!window.confirm('Tem certeza que deseja excluir esta resposta?')) return;
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}');
      await api.delete(`/forum/resposta/${id}`, { data: { usuarioId: user.id } });

      await fetchDuvidas();

      if (selectedDuvida) {
        const res = await api.get(`/forum/${disciplineId}`);
        const updatedDuvida = res.data.find((d: any) => d.id === selectedDuvida.id);
        if (updatedDuvida) {
          setSelectedDuvida(updatedDuvida);
          setRespostas(updatedDuvida.respostas || []);
        }
      }
      alert('Resposta excluída com sucesso!');
    } catch (error) {
      console.error("Erro ao excluir resposta:", error);
      alert('Erro ao excluir resposta.');
    }
  };

  const handleChangeStatus = async (status: string) => {
    if (!selectedDuvida) return;
    try {
      await api.patch(`/forum/duvida/${selectedDuvida.id}/status`, { status });
      setSelectedDuvida({ ...selectedDuvida, status: status as any });
      setDuvidas(prev => prev.map(d => d.id === selectedDuvida.id ? { ...d, status: status as any } : d));
    } catch (error) {
      alert('Erro ao atualizar status');
    }
  };

  if (loading) return <div className="text-center p-5">Carregando fórum...</div>;

  return (
    <div className="p-4 bg-white border rounded shadow-sm">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h5 className="fw-bold mb-0">Discussões da Disciplina</h5>
        <Button variant="dark" size="sm" onClick={() => setShowNewDuvidaModal(true)}>+ Nova Dúvida</Button>
      </div>

      {Array.isArray(duvidas) && duvidas.length > 0 ? (
        <ListGroup className="gap-3">
          {duvidas.map((d) => (
            <Card key={d.id} className="border-0 shadow-sm p-3">
              <div className="d-flex justify-content-between align-items-start">
                <div className="flex-grow-1">
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <h6 className="fw-bold mb-0">{d.titulo || 'Sem título'}</h6>
                  </div>
                  <small className="text-muted">
                    Postado por {d.aluno?.nome || 'Usuário'} em {d.dataCriacao ? new Date(d.dataCriacao).toLocaleDateString() : 'Data desconhecida'}
                  </small>
                </div>
                <Button variant="outline-dark" size="sm" onClick={() => handleOpenTopic(d)}>Ver Conversa</Button>
              </div>
              <p className="small text-muted mt-2 text-truncate" style={{ maxWidth: '80%' }}>{d.descricao || 'Sem descrição'}</p>
            </Card>
          ))}
        </ListGroup>
      ) : (
        <p className="text-muted text-center">Nenhuma dúvida postada ainda.</p>
      )}

      <Modal show={showNewDuvidaModal} onHide={() => setShowNewDuvidaModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Nova Dúvida no Fórum</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Título</Form.Label>
              <Form.Control
                type="text"
                placeholder="Resuma sua dúvida..."
                value={novaDuvida.titulo}
                onChange={e => setNovaDuvida({...novaDuvida, titulo: e.target.value})}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Descrição</Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                placeholder="Explique detalhadamente sua dúvida..."
                value={novaDuvida.descricao}
                onChange={e => setNovaDuvida({...novaDuvida, descricao: e.target.value})}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowNewDuvidaModal(false)}>Cancelar</Button>
          <Button variant="dark" onClick={handleCreateDuvida}>Postar Dúvida</Button>
        </Modal.Footer>
      </Modal>

      <Modal show={showEditDuvidaModal} onHide={() => setShowEditDuvidaModal(false)} centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">Editar Dúvida</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Título</Form.Label>
              <Form.Control
                type="text"
                value={editingDuvida.titulo}
                onChange={e => setEditingDuvida({...editingDuvida, titulo: e.target.value})}
              />
            </Form.Group>
            <Form.Group className="mb-3">
              <Form.Label className="small fw-bold">Descrição</Form.Label>
              <Form.Control
                as="textarea"
                rows={4}
                value={editingDuvida.descricao}
                onChange={e => setEditingDuvida({...editingDuvida, descricao: e.target.value})}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={() => setShowEditDuvidaModal(false)}>Cancelar</Button>
          <Button variant="dark" onClick={handleEditDuvida}>Salvar Alterações</Button>
        </Modal.Footer>
      </Modal>

      <Modal show={!!selectedDuvida} onHide={() => setSelectedDuvida(null)} size="lg" centered>
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold">{selectedDuvida?.titulo || 'Detalhes da Dúvida'}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <div className="mb-4 p-3 bg-light rounded">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <small className="fw-bold text-primary">Pergunta de {selectedDuvida?.aluno?.nome || 'Usuário'}</small>
              <div className="d-flex gap-2">
                {(() => {
                  const userStr = localStorage.getItem('user');
                  if (!userStr) return null;
                  const user = JSON.parse(userStr);
                  const isAuthor = Number(selectedDuvida?.aluno?.id) === Number(user.id);
                  const isAdmin = user.role === 'ADMIN';

                  return (
                    <>
                      {isAuthor || isAdmin ? (
                        <>
                          <Button variant="outline-primary" size="sm" onClick={() => {
                            setEditingDuvida({ titulo: selectedDuvida.titulo, descricao: selectedDuvida.descricao });
                            setShowEditDuvidaModal(true);
                          }}>Editar</Button>
                          <Button variant="outline-danger" size="sm" onClick={() => handleDeleteDuvida(selectedDuvida.id)}>Excluir</Button>
                        </>
                      ) : null}
                    </>
                  );
                })()}
              </div>
            </div>
            <p className="mb-3">{selectedDuvida?.descricao || 'Sem descrição'}</p>
            <Button
              variant="link"
              size="sm"
              className="p-0 text-decoration-none small fw-bold text-primary"
              onClick={() => {
                setReplyingToResponseId(null);
                setNovaResposta('');
                window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
              }}
            >
              Responder a esta pergunta
            </Button>
          </div>

          <h6 className="fw-bold mb-3">Respostas</h6>
          <div className="d-flex flex-column gap-3 mb-4">
            {(() => {
              const allChildrenIds = new Set<number>();
              const collectChildren = (resps: any) => {
                if (!Array.isArray(resps)) return;
                resps.forEach(r => {
                  if (r && r.respostasFilhas) {
                    r.respostasFilhas.forEach((filha: any) => {
                      allChildrenIds.add(filha.id);
                      collectChildren(filha.respostasFilhas);
                    });
                  }
                });
              };
              collectChildren(respostas || []);
              return respostas?.filter(r => !allChildrenIds.has(r.id)).map((r) => (
                <RenderResposta
                  key={r.id}
                  resposta={r}
                  depth={0}
                  editingRespostaId={editingRespostaId}
                  editingRespostaText={editingRespostaText}
                  setEditingRespostaId={setEditingRespostaId}
                  setEditingRespostaText={setEditingRespostaText}
                  handleDeleteResposta={handleDeleteResposta}
                  handleEditResposta={handleEditResposta}
                  setReplyingToResponseId={setReplyingToResponseId}
                  setNovaResposta={setNovaResposta}
                />
              ));
            })()}
          </div>

          <Form className="d-flex gap-2" onSubmit={(e) => { e.preventDefault(); handlePostReply(); }}>
            <Form.Control
              placeholder={replyingToResponseId ? `Respondendo à resposta...` : "Escreva sua resposta..."}
              value={novaResposta}
              onChange={e => setNovaResposta(e.target.value)}
            />
            <Button variant="dark" onClick={handlePostReply} disabled={isPosting}>
              {isPosting ? 'Enviando...' : 'Enviar'}
            </Button>
            {replyingToResponseId && (
              <Button variant="secondary" size="sm" onClick={() => setReplyingToResponseId(null)}>Cancelar</Button>
            )}
          </Form>
        </Modal.Body>
      </Modal>
    </div>
  );
};

export default Forum;
