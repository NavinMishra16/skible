package com.skible.be.dto;

public class CreateRoomRequest {
    private String playerName;
    public CreateRoomRequest(){};

    public CreateRoomRequest(String playerName){
        this.playerName = playerName;
    }
    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}
