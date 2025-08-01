package com.skible.be.model;
import com.skible.be.enums.RoundResult;
import com.skible.be.enums.GameStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;



public class GameState {
    private String roomId;
    private List<String> players = new ArrayList<>();
    private int currentRound = 0;
    private int currentPlayerIndex = 0;
    private GameStatus status = GameStatus.WAITING_FOR_PLAYERS;
    private RoundResult lastRoundResult = RoundResult.PENDING;
    private String chosenWord;
    private Set<String>readyPlayers = new CopyOnWriteArraySet<>();


    public GameState() {}

    public GameState(String roomId){
        this.roomId = roomId;
        this.status = GameStatus.WAITING_FOR_PLAYERS;
        this.currentPlayerIndex = 0 ;
        this.currentRound = 0 ;
    }

    public String getRoomId(){
        return roomId;
    }
    public void setRoomId(String roomId){
        this.roomId = roomId ;
    }

    public List<String> getPlayers(){
        return players;
    }

    public void setPlayers(List<String> players){
        this.players = players;
    }

    public int getCurrentRound(){
        return currentRound;
    }

    public void setCurrentRound(int currentRound){
        this.currentRound = currentRound;
    }

    public int getCurrentPlayerIndex(){
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex){
        this.currentPlayerIndex = currentPlayerIndex;
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

    public Set<String> getReadyPlayers() {
        return readyPlayers;
    }

    public void addPlayer(String player){
        this.players.add(player);
    }

    public String getCurrentPlayer() {
        if (players.isEmpty() || currentPlayerIndex >= players.size()) {
            return null;
        }
        return players.get(currentPlayerIndex);
    }

      public String getCurrentPicker() {
        return getCurrentPlayer(); 
    }

    public String getCurrentGuesser() {
        return players.get(1-currentPlayerIndex);
    }

    public void toggleCurrentPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public boolean togglePlayerReady(String playerName) {
        if (readyPlayers.remove(playerName)) {
            return false; 
        }
        readyPlayers.add(playerName);
        return true; 
    }

    public boolean allPlayersReady() {
        return players.size() >= 2 && readyPlayers.containsAll(players);
    }

    public void clearChosenWord() {
        this.chosenWord = null;
    }

    public void advanceToNextRound() {
        this.chosenWord = null; 
        this.currentPlayerIndex = 1 - this.currentPlayerIndex; 
        this.currentRound++;
    }




}