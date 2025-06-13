package com.skible.be.dto;

import com.skible.be.enums.RoundResult;

public class GameStateRequest {
    private String roomId;
    private RoundResult roundResult;
    private String playerName;
    private String guess;

    public GameStateRequest() {}

    public GameStateRequest(String roomId, String playerName) {
        this.roomId = roomId;
        this.playerName = playerName;
    }

    public GameStateRequest(String roomId, String playerName, RoundResult roundResult) {
        this.roomId = roomId;
        this.playerName = playerName;
        this.roundResult = roundResult;
    }

    // ← ADD THESE:
    public String getGuess() {
        return guess;
    }
    public void setGuess(String guess) {
        this.guess = guess;
    }

    // (existing getters/setters below)
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public RoundResult getRoundResult() { return roundResult; }
    public void setRoundResult(RoundResult roundResult) { this.roundResult = roundResult; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
}
