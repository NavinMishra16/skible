package com.skible.be.dto;

public class WordOptionsRequest {

    private String roomId;

    public WordOptionsRequest() {}

    public WordOptionsRequest(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

}
