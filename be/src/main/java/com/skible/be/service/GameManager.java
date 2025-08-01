package com.skible.be.service;
import com.skible.be.service.GameManager;
import com.skible.be.dto.RoomResponse;
import com.skible.be.model.GameState;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameManager {
    private final Map<String, RoomResponse> rooms = new ConcurrentHashMap<>();
    private final GameStateService gameStateService;
    private final WordService wordService;
    private final ScoreBoardService scoreBoardService;

    public GameManager(GameStateService gameStateService, WordService wordService,ScoreBoardService scoreBoardService) {
        this.gameStateService = gameStateService;
        this.wordService      = wordService;
        this.scoreBoardService = scoreBoardService;
    }

    public RoomResponse createRoom(String hostPlayer) {
        RoomResponse room = new RoomResponse();
        room.addPlayer(hostPlayer);
        rooms.put(room.getRoomId(), room);

        gameStateService.initializeGame(room.getRoomId());
        gameStateService.addPlayerToGame(room.getRoomId(), hostPlayer);
        return room;
    }

    public RoomResponse joinRoom(String roomId, String playerName) {
        RoomResponse room = rooms.get(roomId);
        if (room != null) {
            room.addPlayer(playerName);
            gameStateService.addPlayerToGame(roomId, playerName);
        }
        return room;
    }

    public boolean togglePlayerReady(String roomId, String playerName) {
        if(gameStateService.canStartGame(roomId)){
            startGame(roomId);
        }
        return gameStateService.togglePlayerReady(roomId, playerName);
    }

    public boolean allPlayerReady(String roomId) {
        return gameStateService.allPlayersReady(roomId);
    }

    /* to Start the Game */

    public GameState startGame(String roomId) {
        RoomResponse room = rooms.get(roomId);
        if(room !=null){
            scoreBoardService.initializeScore(roomId,room.getPlayers());
        }

        return gameStateService.startGame(roomId);
    }

    public List<String> getWordOptionsForRoom(String roomId, String requester) {
        if (!gameStateService.getCurrentPlayer(roomId).equals(requester)) {
            throw new IllegalStateException("Not your turn to pick !");
        }
        return wordService.pickN(3);
    }

    /** Record the word */
    public String chooseWordAndRecord(String roomId, String chooser, String word) {

        if (!gameStateService.getCurrentPicker(roomId).equals(chooser)) {
            throw new IllegalStateException("Not your turn to pick !");
        }
        gameStateService.updateChosenWord(roomId, word);
        return gameStateService.getOtherPlayer(roomId);
    }

    /** Validate the guess (must match currentPlayer), but do not flip here */
    public boolean processGuess(String roomId, String guesser, String guess) {
        if (!gameStateService.getCurrentGuesser(roomId).equals(guesser)) {
            throw new IllegalStateException("Not your turn to guess");
        }
        String secret = gameStateService.getChosenWord(roomId);
        boolean correct =  secret != null && secret.equalsIgnoreCase(guess);
        if(correct){
            scoreBoardService.incrementPlayerScore(roomId,guesser);
        }
        return correct;
    }

    /** After the guess, switch to the next picker */
    public String advanceAfterGuess(String roomId) {
        return gameStateService.advanceToNextRound(roomId);
    }

    public String getCurrentPicker(String roomId){
         return gameStateService.getCurrentPicker(roomId);
    }
    public String getCurrentGuesser(String roomId){
        return gameStateService.getCurrentGuesser(roomId);
    }

    public Map<String, Integer> getScores(String roomId) {
        return scoreBoardService.getAllScores(roomId);
    }

    /** Get score for a specific player */
    public int getPlayerScore(String roomId, String playerName) {
        return scoreBoardService.getPlayerScore(roomId, playerName);
    }

    /** Reset scores for a room */
    public void resetScores(String roomId) {
        scoreBoardService.resetScores(roomId);
    }

    /** Manually increment a player's score (for special cases) */
    public void incrementPlayerScore(String roomId, String playerName) {
        scoreBoardService.incrementPlayerScore(roomId, playerName);
    }

    /** So your controller can broadcast the full state when needed */
    public GameState getGameState(String roomId) {
        return gameStateService.getGameState(roomId);
    }
}

