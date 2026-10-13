package com.unifor.monitoria.model;

import java.time.LocalDateTime;

public class ChatMessage {
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage() {}

    public ChatMessage(Integer senderId, String senderName, Integer receiverId, String receiverName, String content, LocalDateTime timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
