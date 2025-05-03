
package com.skible.be.dto;

public class PickWordResponse {
    private String roomId;
    private String chosenWord;

    public PickWordResponse() {}

    public PickWordResponse(String roomId, String chosenWord) {
        this.roomId     = roomId;
        this.chosenWord = chosenWord;
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
}
