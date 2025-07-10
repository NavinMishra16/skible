package com.skible.be.dto;
import com.skible.be.enums.RoundResult;
import com.skible.be.enums.GameStatus;
import java.util.ArrayList;
import java.util.List;


public class GameState {
    private String roomId;
    private List<String> players = new ArrayList<>();
    private int currentRound = 0;
    private int currentPlayerIndex = 0; // Track which player's turn it is
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;
    private RoundResult lastRoundResult = RoundResult.PENDING;
    public GameState() {}

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
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }
    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public String getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
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

    public void addPlayer(String player) {
        this.players.add(player);
    }

    public void toggleCurrentPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}