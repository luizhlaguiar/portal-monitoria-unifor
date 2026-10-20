import React from 'react';
import { Modal, Form, Row, Col, Button } from 'react-bootstrap';

interface ActivityModalProps {
  show: boolean;
  onHide: () => void;
  activity: {
    id?: number;
    titulo: string;
    prazo: string;
    descricao: string;
    noDeadline: boolean;
    arquivoUrl: string;
  };
  setActivity: React.Dispatch<React.SetStateAction<any>>;
  handleSaveActivity: (e: React.FormEvent) => Promise<void>;
  handleActivityFileUpload: (e: React.ChangeEvent<HTMLInputElement>) => Promise<void>;
  uploadingActivityFile: boolean;
  isEditing: boolean;
}

const ActivityModal: React.FC<ActivityModalProps> = ({
  show,
  onHide,
  activity,
  setActivity,
  handleSaveActivity,
  handleActivityFileUpload,
  uploadingActivityFile,
  isEditing
}) => {
  return (
    <Modal show={show} onHide={onHide} centered>
      <Modal.Header closeButton>
        <Modal.Title className="fw-bold">{isEditing ? 'Editar Atividade de Fixação' : 'Nova Atividade de Fixação'}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form id="activity-form" onSubmit={handleSaveActivity}>
          <Form.Group className="mb-3">
            <Form.Label className="small fw-bold">Título</Form.Label>
            <Form.Control
              type="text"
              value={activity.titulo}
              onChange={e => setActivity({...activity, titulo: e.target.value})}
              required
            />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label className="small fw-bold">Prazo</Form.Label>
            <Row>
              <Col>
                <Form.Control
                  type="date"
                  value={activity.prazo}
                  onChange={e => setActivity({...activity, prazo: e.target.value, noDeadline: false})}
                />
              </Col>
              <Col xs="auto" className="d-flex align-items-center">
                <Form.Check
                  type="checkbox"
                  label="Sem prazo"
                  checked={activity.noDeadline}
                  onChange={e => setActivity({...activity, noDeadline: e.target.checked})}
                />
              </Col>
            </Row>
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label className="small fw-bold">Descrição</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={activity.descricao}
              onChange={e => setActivity({...activity, descricao: e.target.value})}
              required
            />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label className="small fw-bold">Anexo (Opcional)</Form.Label>
            <Form.Control
              type="file"
              onChange={handleActivityFileUpload}
              disabled={uploadingActivityFile}
            />
            {uploadingActivityFile && <small className="text-primary">Enviando arquivo...</small>}
            {activity.arquivoUrl && <small className="text-success">Arquivo anexado com sucesso!</small>}
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onHide}>Cancelar</Button>
        <Button type="submit" form="activity-form" variant="dark" disabled={uploadingActivityFile}>
          {isEditing ? 'Salvar Alterações' : 'Salvar Atividade'}
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default ActivityModal;
