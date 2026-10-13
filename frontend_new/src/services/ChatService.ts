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
    private socket: SockJS.Transport | null = null;

    connect(userId: number, onMessageReceived: (message: ChatMessage) => void) {
        if (this.stompClient && this.stompClient.connected) {
            return;
        }

        try {
            this.socket = new SockJS('http://localhost:8090/ws-chat');
            this.stompClient = Stomp.over(this.socket);

            this.stompClient.connect({}, () => {
                console.log('Connected to WebSocket');
                this.stompClient?.subscribe(`/user/${userId}/queue/messages`, (sdkEvent) => {
                    try {
                        onMessageReceived(JSON.parse(sdkEvent.body));
                    } catch (e) {
                        console.error('Error parsing message body:', e);
                    }
                });
            }, (error) => {
                console.error('WebSocket Connection Error:', error);
            });
        } catch (e) {
            console.error('Failed to initialize WebSocket:', e);
        }
    }

    disconnect() {
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
            console.error('WebSocket not connected. Cannot send message.');
        }
    }
}

export default new ChatService();
