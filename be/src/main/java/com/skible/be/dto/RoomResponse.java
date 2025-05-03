package com.skible.be.dto;



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomResponse {
    private String roomId;
    private List<String> players = new ArrayList<>();


    public RoomResponse() {
        this.roomId = UUID.randomUUID().toString();
    }

    public RoomResponse(List<String> initialPlayers) {
        this();
        this.players.addAll(initialPlayers);
    }

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

    public void addPlayer(String player) {
        this.players.add(player);
    }


}
