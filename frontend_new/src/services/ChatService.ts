import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export interface ChatMessage {
    type: 'CHAT' | 'JOIN' | 'LEAVE';
    senderId: number;
    receiverId: number;
    content: string;
    conversationId?: number;
}

class ChatService {
    private stompClient: Stomp.Client | null = null;
    private socket: any = null;
    private reconnectTimer: any = null;

    connect(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        if (this.stompClient && this.stompClient.connected) {
            return;
        }

        try {
            this.socket = new SockJS('http://localhost:8090/ws-chat');
            this.stompClient = Stomp.over(this.socket);
            
            // Opcional: desativa logs excessivos do STOMP no console se desejar
            // this.stompClient.debug = () => {};

            this.stompClient.connect({}, () => {
                console.log('Connected to WebSocket');
                if (this.reconnectTimer) {
                    clearTimeout(this.reconnectTimer);
                    this.reconnectTimer = null;
                }

                this.stompClient?.subscribe(`/user/${userId}/queue/messages`, (sdkEvent) => {
                    try {
                        onMessageReceived(JSON.parse(sdkEvent.body));
                    } catch (e) {
                        console.error('Error parsing message body:', e);
                    }
                });
            }, (error) => {
                console.error('WebSocket Connection Error:', error);
                this.scheduleReconnect(userId, onMessageReceived);
            });
        } catch (e) {
            console.error('Failed to initialize WebSocket:', e);
            this.scheduleReconnect(userId, onMessageReceived);
        }
    }

    private scheduleReconnect(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        if (!this.reconnectTimer) {
            this.reconnectTimer = setTimeout(() => {
                console.log('Tentando reconectar ao WebSocket...');
                this.connect(userId, onMessageReceived);
            }, 5000); // Tenta reconectar a cada 5 segundos
        }
    }

    disconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
        if (this.stompClient) {
            this.stompClient.disconnect();
            this.stompClient = null;
        }
        if (this.socket) {
            this.socket.close();
            this.socket = null;
        }
    }

    sendMessage(message: ChatMessage) {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.send", {}, JSON.stringify(message));
        } else {
            console.error('WebSocket not connected. Tentando enviar mesmo assim ou aguardando reconexão...');
            alert('A conexão com o chat caiu momentaneamente. Aguarde alguns segundos e tente enviar novamente.');
        }
    }
}

export default new ChatService(); 