
package com.skible.be.dto;

public class PickWordRequest {
    private String roomId;
    private String chosenWord;
    private String playerName;

    public PickWordRequest() {}

    public PickWordRequest(String roomId, String chosenWord, String playerName) {
        this.roomId     = roomId;
        this.chosenWord = chosenWord;
        this.playerName = playerName;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getChosenWord() {
        return chosenWord;
    }

    public void setChosenWord(String chosenWord) {
        this.chosenWord = chosenWord;
    }

    public String getPlayerName(){
        return playerName;
    }
    public void setPlayerName(String playerName){
        this.playerName = playerName;
    }
}
