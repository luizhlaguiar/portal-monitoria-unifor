import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export interface ChatMessage {
    type: 'CHAT' | 'JOIN' | 'LEAVE';
    senderId: number;
    senderName?: string;
    receiverId: number;
    receiverName?: string;
    content: string;
    timestamp?: string;
    conversationId?: number;
}

class ChatService {
    private stompClient: Stomp.Client | null = null;
    private socket: SockJS.Transport | null = null;
    private reconnectTimeout: NodeJS.Timeout | null = null;
    private isConnecting = false;
    private currentUserId: number | null = null;
    private messageCallback: ((message: ChatMessage) => void) | null = null;

    ensureConnected(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        this.currentUserId = userId;
        this.messageCallback = onMessageReceived;

        if (this.stompClient && this.stompClient.connected) {
            this.subscribeToChannels(userId);
            return;
        }

        if (this.isConnecting) return;
        this.isConnecting = true;

        try {
            console.log(`[ChatService] Initializing global connection for user ${userId}...`);
            this.socket = new SockJS('http://localhost:8080/ws-chat');
            this.stompClient = Stomp.over(this.socket);
            this.stompClient.debug = null;

            this.stompClient.connect({}, () => {
                this.isConnecting = false;
                console.log('[ChatService] WebSocket Connected Successfully');
                this.subscribeToChannels(userId);
            }, (error) => {
                this.isConnecting = false;
                console.error('[ChatService] WebSocket Connection Error:', error);
                this.scheduleReconnect();
            });
        } catch (e) {
            this.isConnecting = false;
            console.error('[ChatService] Failed to initialize WebSocket:', e);
            this.scheduleReconnect();
        }
    }

    private subscribeToChannels(userId: number) {
        if (!this.stompClient || !this.stompClient.connected) return;

        // Canal 1: Fila privada (depende de autenticação do Spring)
        this.stompClient.subscribe(`/user/${userId}/queue/messages`, (sdkEvent) => {
            this.handleIncomingMessage(sdkEvent);
        });

        // Canal 2: Tópico público (Garante a entrega instantânea)
        this.stompClient.subscribe('/topic/messages', (sdkEvent) => {
            this.handleIncomingMessage(sdkEvent);
        });

        console.log(`[ChatService] Subscribed to both private and public channels for user ${userId}`);
    }

    private handleIncomingMessage(sdkEvent: any) {
        try {
            const msg = JSON.parse(sdkEvent.body);
            if (this.messageCallback) {
                this.messageCallback(msg);
            }
        } catch (e) {
            console.error('[ChatService] Error parsing message body:', e);
        }
    }

    private scheduleReconnect() {
        if (this.reconnectTimeout) return;
        this.reconnectTimeout = setTimeout(() => {
            this.reconnectTimeout = null;
            if (this.currentUserId && this.messageCallback) {
                this.ensureConnected(this.currentUserId, this.messageCallback);
            }
        }, 5000);
    }

    disconnect() {
        if (this.stompClient) {
            try {
                if (this.stompClient.connected) this.stompClient.disconnect();
            } catch (e) {}
            this.stompClient = null;
        }
        if (this.socket) {
            try { this.socket.close(); } catch (e) {}
            this.socket = null;
        }
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }
        this.currentUserId = null;
        this.messageCallback = null;
    }

    sendMessage(message: ChatMessage) {
        if (this.stompClient && this.stompClient.connected) {
            this.stompClient.send("/app/chat.send", {}, JSON.stringify(message));
        } else {
            console.error('[ChatService] WebSocket not connected.');
        }
    }
}

export default new ChatService();
