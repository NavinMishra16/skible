package com.skible.be.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.skible.be.dto.GameState;
import com.skible.be.dto.GameStateResponse;
import com.skible.be.dto.RoomResponse;
import com.skible.be.enums.RoundResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameManager {
    private final Map<String, RoomResponse> rooms = new ConcurrentHashMap<>();
    private final GameStateService gameStateService;
    private final WordService wordService;

    @Autowired
    public GameManager(GameStateService gameStateService, WordService wordService) {
        this.gameStateService = gameStateService;
        this.wordService = wordService;
    }

    /**
     * Creates a new game room with the given host player
     */
    public RoomResponse createRoom(String hostPlayer) {
        RoomResponse room = new RoomResponse();
        room.addPlayer(hostPlayer);
        rooms.put(room.getRoomId(), room);
        gameStateService.initializeGame(room.getRoomId());
        gameStateService.addPlayerToGame(room.getRoomId(),hostPlayer);
        return room;
    }

    /**
     * Adds a player to an existing room
     */
    public RoomResponse joinRoom(String roomId, String playerName) {
        RoomResponse room = rooms.get(roomId);
        if (room != null) {
            room.addPlayer(playerName);
            gameStateService.addPlayerToGame(roomId, playerName);
        }
        return room;
    }

    // toggle Players to Start the Game
     public  boolean togglePlayerReady(String roomId, String playerName){
        return gameStateService.togglePlayerReady(roomId,playerName);
     }
     // Check all players are ready or not
    public  boolean allPlayerReady(String roomId){
        return gameStateService.allPlayersReady(roomId);
    }
    /**
     * Starts a game in the specified room
     */
    public GameStateResponse startGame(String roomId) {
        GameState state = gameStateService.startGame(roomId);
        return new GameStateResponse(state, null);
    }

    /*
     * Only let the current player choose the word
     */
    public List<String> getWordOptionsForRoom(String roomId, String requester){
        String current = gameStateService.getCurrentPlayer(roomId);
        if(!current.equals(requester)){
            throw new IllegalStateException("Not your Turn");
        }
        return wordService.pickN(3);
    }

    /*
     * Record the word advance the turn and choose next player
     */
    public String chooseWordAndAdvance(String roomId,String chooser,String word){
        String current = gameStateService.getCurrentPlayer(roomId);
        if(!current.equals(chooser)){
            throw new IllegalStateException("Not your Turn");
        }
        gameStateService.updateChosenWord(roomId,word);
        return gameStateService.advanceTurn(roomId);
    }


    /**
     * Moves the game to the next round with the given result
     */
    public GameStateResponse nextRound(String roomId, RoundResult result) {
        GameState state = gameStateService.nextRound(roomId, result);
        return new GameStateResponse(state, null);
    }

    /**
     * Gets a list of word options for players to choose from
     */
    public List<String> getWordOptions(int count) {
        return wordService.pickN(count);
    }

    /**
     * Sets the chosen word for a specific room
     */
    public GameStateResponse setChosenWord(String roomId, String chosenWord) {
        GameState state = gameStateService.updateChosenWord(roomId, chosenWord);
        return new GameStateResponse(state, chosenWord);
    }

    /**
     * Gets the current game state
     */
    public GameStateResponse getGameState(String roomId) {
        GameState state = gameStateService.getState(roomId);
        String chosenWord = gameStateService.getChosenWord(roomId);
        return new GameStateResponse(state, chosenWord);
    }

    /**
     * Checks if the game has ended
     */
    public boolean isGameEnded(String roomId) {
        return gameStateService.isGameEnded(roomId);
    }

    /**
     * Gets the winner of the game if it has ended
     */
    public String getWinner(String roomId) {
        return gameStateService.getWinner(roomId);
    }


}