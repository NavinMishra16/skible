package com.skible.be.dto;

import java.util.List;

public class WordOptionResponse {
    private String roomId;
    private List<String> options;

    public WordOptionResponse() {}

    public WordOptionResponse(String roomId, List<String> options) {
        this.roomId = roomId;
        this.options = options;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}