package com.skible.be.dto;

public class ChatMessage {

    private String roomId;
    private String sender;
    private String content;

    public ChatMessage() {}
    public ChatMessage(String roomId, String sender, String content) {
        this.roomId = roomId;
        this.sender = sender;
        this.content = content;
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
