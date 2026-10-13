import React, { useState } from 'react';
import './Messages.css';

interface Message {
  id: string;
  sender: string;
  text: string;
  time: string;
  isMe: boolean;
}

const mockMessages: Message[] = [
  { id: '1', sender: 'Monitor Igor', text: 'Olá Luiz, você conseguiu resolver a dúvida do React?', time: '10:30', isMe: false },
  { id: '2', sender: 'Eu', text: 'Ainda não, estou com dificuldade no useEffect.', time: '10:35', isMe: true },
  { id: '3', sender: 'Monitor Igor', text: 'Vou te enviar um link de documentação que ajuda muito!', time: '10:36', isMe: false },
];

const Messages: React.FC = () => {
  const [text, setText] = useState('');

  return (
    <div className="chat-layout">
      <aside className="chat-sidebar">
        <div className="sidebar-header">
          <h2>Conversas</h2>
        </div>
        <div className="contacts-list">
          <div className="contact active">
            <div className="avatar">I</div>
            <div className="contact-info">
              <span className="name">Monitor Igor</span>
              <span className="last-msg">Vou te enviar um link...</span>
            </div>
          </div>
          <div className="contact">
            <div className="avatar">V</div>
            <div className="contact-info">
              <span className="name">Monitor Vinicius</span>
              <span className="last-msg">A aula será às 14h.</span>
            </div>
          </div>
        </div>
      </aside>

      <main className="chat-main">
        <header className="chat-header">
          <div className="chat-user">
            <div className="avatar">I</div>
            <h3>Monitor Igor</h3>
          </div>
          <div className="chat-actions">
            <button className="btn-info">Info</button>
          </div>
        </header>

        <div className="message-area">
          {mockMessages.map(msg => (
            <div key={msg.id} className={`message-wrapper ${msg.isMe ? 'me' : 'them'}`}>
              <div className="message-bubble">
                <p>{msg.text}</p>
                <span className="msg-time">{msg.time}</span>
              </div>
            </div>
          ))}
        </div>

        <footer className="chat-input">
          <input
            type="text"
            placeholder="Digite sua mensagem..."
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <button className="btn-send">Enviar</button>
        </footer>
      </main>
    </div>
  );
};

export default Messages;
