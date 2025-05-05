package com.skible.be.service;

import com.skible.be.dto.GameState;
import com.skible.be.enums.RoundResult;
import com.skible.be.enums.GameStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class GameStateService {
    private final Map<String, GameState> gameStates = new ConcurrentHashMap<>();
    private final Map<String, String> chosenWords = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();

    public GameState initializeGame(String roomId) {
        GameState state = new GameState();
        state.setRoomId(roomId);
        state.setStatus(GameStatus.WAITING_FOR_PLAYERS);
        state.setLastRoundResult(RoundResult.PENDING);
        state.setCurrentPlayerIndex(0); // Start with first player
        gameStates.put(roomId, state);
        readyPlayers.put(roomId,new CopyOnWriteArraySet<>());
        return state;
    }

    public GameState addPlayerToGame(String roomId, String playerName) {
        GameState state = getState(roomId);
        state.addPlayer(playerName);
        readyPlayers.computeIfAbsent(roomId,k->new CopyOnWriteArraySet<>());
        return state;
    }

    public  boolean togglePlayerReady(String roomId, String playerName){
        Set<String> readySet = readyPlayers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
        if (readySet.contains(playerName)) {
            readySet.remove(playerName);
            return false;
        }
        else {
            readySet.add(playerName);
            return true;
        }
    }

    public boolean allPlayersReady(String roomId) {
        GameState state = getState(roomId);
        Set<String> readySet = readyPlayers.get(roomId);
        return readySet != null && state.getPlayers().size() > 0 && readySet.containsAll(state.getPlayers());
    }

    public GameState startGame(String roomId) {
        GameState state = getState(roomId);

        if (state.getPlayers().size() < 2) {

            throw new IllegalStateException("Need at least 2 players to start the game");
        }
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setCurrentRound(1);
        state.setCurrentPlayerIndex(0); // First player starts
        return state;
    }

    public GameState updateChosenWord(String roomId, String chosenWord) {
        GameState state = getState(roomId);
        chosenWords.put(roomId, chosenWord);
        return state;
    }

    public String getChosenWord(String roomId) {
        return chosenWords.getOrDefault(roomId, "");
    }

    public void clearChosenWord(String roomId) {
        chosenWords.remove(roomId);
    }

    public GameState nextRound(String roomId, RoundResult result) {
        GameState state = getState(roomId);
        if (state.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game is not in progress");
        }

        state.setLastRoundResult(result);
        state.setCurrentRound(state.getCurrentRound() + 1);

        // Toggle player turn
        state.toggleCurrentPlayer();

        // Clear the chosen word for the new round
        clearChosenWord(roomId);

        if (state.getCurrentRound() > 5) {
            state.setStatus(GameStatus.ENDED);
        }
        return state;
    }

    // Remove this method as it's now handled by toggleCurrentPlayer in GameState
    // private void switchTurn(GameState state){
    //    int current = state.getCurrentRound();
    //    state.setCurrentRound(1-current);
    // }

    public GameState getState(String roomId) {
        GameState state = gameStates.get(roomId);
        if (state == null) {
            throw new IllegalArgumentException("Game state not found for room: " + roomId);
        }
        return state;
    }

    public boolean isGameEnded(String roomId) {
        return getState(roomId).getStatus() == GameStatus.ENDED;
    }

    public String getWinner(String roomId) {
        GameState state = getState(roomId);
        if (state.getStatus() != GameStatus.ENDED) {
            throw new IllegalStateException("Game is not finished yet");
        }

        switch (state.getLastRoundResult()) {
            case PLAYER_ONE_WINS:
                return state.getPlayers().get(0);
            case PLAYER_TWO_WINS:
                return state.getPlayers().get(1);
            case DRAW:
                return "Draw";
            default:
                return "No result";
        }
    }
}