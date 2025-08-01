package com.skible.be.service;
import com.skible.be.enums.GameStatus;
import com.skible.be.model.GameState;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameStateService {
    private final Map<String, GameState>   gameStates   = new ConcurrentHashMap<>();
    
    public GameState initializeGame(String roomId) {
        GameState state = new GameState(roomId);
        gameStates.put(roomId, state);
        return state;
    }

    public GameState getGameState(String roomId){
        GameState gameState = gameStates.get(roomId);
        if(gameState == null){
            throw new IllegalArgumentException("No Game Found for room : " + roomId);
        }
        return gameState;
    }

    public GameState addPlayerToGame(String roomId, String playerName) {
        GameState state = getGameState(roomId);
        state.addPlayer(playerName);
        return state;
    }


    public boolean togglePlayerReady(String roomId, String playerName) {

        return gameStates.get(roomId).togglePlayerReady(playerName);
    }

    public boolean allPlayersReady(String roomId) {
        GameState gameState = gameStates.get(roomId);
        return gameState.allPlayersReady();
    }

    public GameState startGame(String roomId) {
        GameState gameState =  getGameState(roomId);
        if(gameState.getPlayers().size() < 2){
            throw new IllegalArgumentException("Need 2 Players to play the game" + roomId);
        }
        gameState.setStatus(GameStatus.IN_PROGRESS);
        gameState.setCurrentRound(1);
        gameState.setCurrentPlayerIndex(0);

        return gameState;
    }

    public GameState updateChosenWord(String roomId, String word) {
        GameState gameState = gameStates.get(roomId);
        gameState.setChosenWord(word);
        return gameState;

    }

    public String getChosenWord(String roomId) {
        return  gameStates.get(roomId).getChosenWord();
    }

    /** Flip currentPlayerIndex (0↔1), clear the chosen word, and return new current. */



    public String advanceToNextRound(String roomId){
        GameState gameState = getGameState(roomId);
        gameState.advanceToNextRound();
        return gameState.getCurrentPlayer();
    }

    public String getCurrentPicker(String roomId){
        GameState state = getGameState(roomId);
        return state.getCurrentPicker();
    }

    public String getCurrentGuesser(String roomId){
        GameState state = getGameState(roomId);
        return state.getCurrentGuesser();
    }



    public String getCurrentPlayer(String roomId) {
       GameState gameState = getGameState(roomId);
       return gameState.getCurrentPlayer();
    }
    

    // Can be Removed 
    public String getOtherPlayer(String roomId) {
        GameState gameState = getGameState(roomId);
        return gameState.getPlayers().get(1 - gameState.getCurrentPlayerIndex());
    }

     public boolean canStartGame(String roomId) {
        GameState state = getGameState(roomId);
         System.out.println("[DEBUG] players="   + state.getPlayers() +
                   ", readySet="        + state.getReadyPlayers() +
                   ", allReady="        + state.allPlayersReady() +
                   ", status="          + state.getStatus());

        return state.getPlayers().size() >= 2 && 
               state.allPlayersReady() && 
               state.getStatus() == GameStatus.WAITING_FOR_PLAYERS;
    }

    public boolean isGameInProgress(String roomId) {
        GameState state = getGameState(roomId);
        return state.getStatus() == GameStatus.IN_PROGRESS;
    }

    public boolean isWaitingForPlayers(String roomId) {
        GameState state = getGameState(roomId);
        return state.getStatus() == GameStatus.WAITING_FOR_PLAYERS;
    }

     public boolean isGameFinished(String roomId) {
        GameState state = getGameState(roomId);
        return state.getStatus() == GameStatus.ENDED;
    }

    public void removeGame(String roomId) {
        gameStates.remove(roomId);
    }

    public boolean gameExists(String roomId) {
        return gameStates.containsKey(roomId);
    }


}
