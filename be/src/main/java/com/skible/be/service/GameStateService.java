package com.skible.be.service;

import com.skible.be.dto.GameState;
import com.skible.be.enums.GameStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class GameStateService {
    private final Map<String, GameState>   gameStates   = new ConcurrentHashMap<>();
    private final Map<String, String>      chosenWords  = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> readyPlayers = new ConcurrentHashMap<>();
    private final Map<String,Integer> currentPickerIndex = new ConcurrentHashMap<>();

    
    public GameState initializeGame(String roomId) {
        GameState state = new GameState();
        state.setRoomId(roomId);
        state.setStatus(GameStatus.WAITING_FOR_PLAYERS);
        state.setCurrentPlayerIndex(0);            // start with player-0
        gameStates.put(roomId, state);
        readyPlayers.put(roomId, new CopyOnWriteArraySet<>());
        return state;
    }

    public GameState addPlayerToGame(String roomId, String playerName) {
        GameState state = getState(roomId);
        state.addPlayer(playerName);
        readyPlayers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
        return state;
    }

    public boolean togglePlayerReady(String roomId, String playerName) {
        Set<String> set = readyPlayers.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>());
        if (set.remove(playerName)) return false;
        set.add(playerName);
        return true;
    }

    public boolean allPlayersReady(String roomId) {
        GameState state = getState(roomId);
        Set<String> set = readyPlayers.get(roomId);
        return set != null
                && state.getPlayers().size() >= 2
                && set.containsAll(state.getPlayers());
    }

    public GameState startGame(String roomId) {
        GameState state = getState(roomId);
        if (state.getPlayers().size() < 2) {
            throw new IllegalStateException("Need 2 players");
        }
        state.setStatus(GameStatus.IN_PROGRESS);
        state.setCurrentRound(1);
        state.setCurrentPlayerIndex(0);            // reset to first player
        currentPickerIndex.put(roomId,0);
        return state;
    }

    public GameState updateChosenWord(String roomId, String word) {
        chosenWords.put(roomId, word);
        return getState(roomId);
    }

    public String getChosenWord(String roomId) {
        return chosenWords.get(roomId);
    }

    /** Flip currentPlayerIndex (0↔1), clear the chosen word, and return new current. */
    public String advanceTurn(String roomId) {
        GameState st = getState(roomId);
        st.toggleCurrentPlayer();
        return getCurrentPlayer(roomId);
    }
    public String advanceToNextRound(String roomId){
        GameState st = getState(roomId);
        chosenWords.remove(roomId);
        Integer currentPicker = currentPickerIndex.get(roomId);
        int newPicker = 1 - currentPicker;
        currentPickerIndex.put(roomId,newPicker);
        st.setCurrentPlayerIndex(newPicker);
        st.setCurrentRound(st.getCurrentRound()+1);
        return getCurrentPlayer(roomId);
    }

    public String getCurrentPicker(String roomId){
        Integer pickerIdx = currentPickerIndex.get(roomId);
        if(pickerIdx == null)pickerIdx = 0 ;
        GameState st = getState(roomId);
        return st.getPlayers().get(pickerIdx);
    }

    public String getCurrentGuesser(String roomId){
        Integer guessIdx = currentPickerIndex.get(roomId);
        if(guessIdx == null)guessIdx = 0 ;
        GameState st = getState(roomId);
        return st.getPlayers().get(1- guessIdx);
    }



    public String getCurrentPlayer(String roomId) {
        GameState st = getState(roomId);
        return st.getPlayers().get(st.getCurrentPlayerIndex());
    }

    public String getOtherPlayer(String roomId) {
        GameState st = getState(roomId);
        return st.getPlayers().get(1 - st.getCurrentPlayerIndex());
    }



    GameState getState(String roomId) {
        GameState st = gameStates.get(roomId);
        if (st == null) throw new IllegalArgumentException("No game for room " + roomId);
        return st;
    }
}
