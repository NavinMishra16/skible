package com.skible.be.dto;

import com.skible.be.enums.GameStatus;
import com.skible.be.enums.RoundResult;

import java.util.List;

public class GameStateResponse {
    private String roomId;
    private List<String> players;
    private int currentRound;
    private String currentPlayer;
    private GameStatus status;
    private RoundResult lastRoundResult;
    private String chosenWord; // Include the chosen word when appropriate

    public GameStateResponse() {}

    // Constructor to create from a GameState entity
    public GameStateResponse(GameState state, String chosenWord) {
        this.roomId = state.getRoomId();
        this.players = state.getPlayers();
        this.currentRound = state.getCurrentRound();
        this.currentPlayer = state.getCurrentPlayer();
        this.status = state.getStatus();
        this.lastRoundResult = state.getLastRoundResult();
        this.chosenWord = chosenWord;
    }

    // Getters and setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public RoundResult getLastRoundResult() {
        return lastRoundResult;
    }

    public void setLastRoundResult(RoundResult lastRoundResult) {
        this.lastRoundResult = lastRoundResult;
    }

    public String getChosenWord() {
        return chosenWord;
    }

    public void setChosenWord(String chosenWord) {
        this.chosenWord = chosenWord;
    }
}