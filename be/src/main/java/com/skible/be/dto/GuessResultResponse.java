package com.skible.be.dto;

public class GuessResultResponse {
    private String roomId;
    private String playerName;
    private String guess;
    private boolean correct;

    public GuessResultResponse() { }

    public GuessResultResponse(String roomId, String playerName, String guess, boolean correct) {
        this.roomId = roomId;
        this.playerName = playerName;
        this.guess = guess;
        this.correct = correct;
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

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }
}