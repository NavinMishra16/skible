package com.skible.be.service;
import com.skible.be.service.GameManager;
import com.skible.be.dto.GameStateResponse;
import com.skible.be.dto.RoomResponse;
import com.skible.be.service.GameStateService;
import com.skible.be.service.WordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameManager {
    private final Map<String, RoomResponse> rooms = new ConcurrentHashMap<>();
    private final GameStateService gameStateService;
    private final WordService wordService;

    @Autowired
    public GameManager(GameStateService gameStateService, WordService wordService) {
        this.gameStateService = gameStateService;
        this.wordService      = wordService;
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
        return gameStateService.togglePlayerReady(roomId, playerName);
    }

    public boolean allPlayerReady(String roomId) {
        return gameStateService.allPlayersReady(roomId);
    }

    public GameStateResponse startGame(String roomId) {
        return new GameStateResponse(
                gameStateService.startGame(roomId),
                null
        );
    }

    public List<String> getWordOptionsForRoom(String roomId, String requester) {
        if (!gameStateService.getCurrentPlayer(roomId).equals(requester)) {
            throw new IllegalStateException("Not your turn to pick");
        }
        return wordService.pickN(3);
    }

    /** Record the word and switch to the guesser */
    public String chooseWordAndAdvance(String roomId, String chooser, String word) {

        if (!gameStateService.getCurrentPicker(roomId).equals(chooser)) {
            throw new IllegalStateException("Not your turn to pick");
        }
        gameStateService.updateChosenWord(roomId, word);
        return gameStateService.advanceTurn(roomId);
    }

    /** Validate the guess (must match currentPlayer), but do not flip here */
    public boolean processGuess(String roomId, String guesser, String guess) {
        if (!gameStateService.getCurrentGuesser(roomId).equals(guesser)) {
            throw new IllegalStateException("Not your turn to guess");
        }
        String secret = gameStateService.getChosenWord(roomId);
        return secret != null && secret.equalsIgnoreCase(guess);
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

    /** So your controller can broadcast the full state when needed */
    public GameStateResponse getGameState(String roomId) {
        return new GameStateResponse(
                gameStateService.getState(roomId),
                gameStateService.getChosenWord(roomId)
        );
    }
}

