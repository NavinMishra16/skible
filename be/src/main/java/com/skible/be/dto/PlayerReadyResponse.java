package com.skible.be.dto;

public class PlayerReadyResponse {
    private String playerName ;
    private boolean ready;

    public PlayerReadyResponse() {}

    public PlayerReadyResponse(String playerName, boolean ready) {
        this.playerName = playerName;
        this.ready = ready;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
