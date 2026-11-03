import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { API_URL } from './api';

const WS_URL = API_URL.replace(/\/api\/?$/, '') + '/ws-chat';

export interface ChatMessage {
    type: 'CHAT' | 'JOIN' | 'LEAVE';
    senderId: number;
    receiverId: number;
    content: string;
    conversationId?: number;
    disciplineId?: number | null;
    senderName?: string;
    receiverName?: string;
}

class ChatService {
    private stompClient: Stomp.Client | null = null;
    private socket: any = null;
    private reconnectTimer: any = null;
    private onMessage: ((message: ChatMessage) => void) | null = null;
    private connecting = false;
    private currentUserId: number | null = null;

    connect(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        if (this.currentUserId !== null && this.currentUserId !== userId) {
            this.disconnect();
        }
        this.currentUserId = userId;
        this.onMessage = onMessageReceived;
        if (this.connecting || (this.stompClient && this.stompClient.connected)) {
            return;
        }
        this.connecting = true;

        try {
            this.socket = new SockJS(WS_URL);
            this.stompClient = Stomp.over(this.socket);
            this.stompClient.debug = () => {};
            

            const token = localStorage.getItem('token') || '';
            this.stompClient.connect({ Authorization: `Bearer ${token}` }, () => {
                this.connecting = false;
                if (this.reconnectTimer) {
                    clearTimeout(this.reconnectTimer);
                    this.reconnectTimer = null;
                }

                this.stompClient?.subscribe(`/topic/chat.${userId}`, (sdkEvent) => {
                    try {
                        this.onMessage?.(JSON.parse(sdkEvent.body));
                    } catch (e) {
                        console.error('Error parsing message body:', e);
                    }
                });
            }, (error) => {
                this.connecting = false;
                console.error('WebSocket Connection Error:', error);
                this.scheduleReconnect(userId, onMessageReceived);
            });
        } catch (e) {
            this.connecting = false;
            console.error('Failed to initialize WebSocket:', e);
            this.scheduleReconnect(userId, onMessageReceived);
        }
    }

    private scheduleReconnect(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        if (!this.reconnectTimer) {
            this.reconnectTimer = setTimeout(() => {
                this.connect(userId, this.onMessage || onMessageReceived);
            }, 5000);
        }
    }

    disconnect() {
        this.connecting = false;
        this.currentUserId = null;
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