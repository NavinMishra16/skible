package com.skible.be.dto;

public class GuessRequest {
    private String roomId;
    private String playerName;
    private String guess;

    public GuessRequest() { }

    public GuessRequest(String roomId, String playerName, String guess) {
        this.roomId = roomId;
        this.playerName = playerName;
        this.guess = guess;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }
}