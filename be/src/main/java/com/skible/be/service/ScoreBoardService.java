package com.skible.be.service;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScoreBoardService {
    Map<String, Map<String,Integer>>playerScores = new ConcurrentHashMap<>();

    public void initializeScore(String roomId, List<String> players){
        Map<String ,Integer> roomScore  = new ConcurrentHashMap<>();
        for(String player : players){
            roomScore.put(player,0);
        }
        playerScores.put(roomId,roomScore);
    }
    public void incrementPlayerScore(String roomId, String playerName){
        Map<String,Integer>roomScore = playerScores.get(roomId);
        if(roomScore!=null){
            roomScore.put(playerName,roomScore.getOrDefault(playerName,0)+1);
        }
    }
    public int getPlayerScore(String roomId, String playerName) {
        Map<String, Integer> roomScores = playerScores.get(roomId);
        return roomScores != null ? roomScores.getOrDefault(playerName, 0) : 0;
    }

    public Map<String, Integer> getAllScores(String roomId) {
        return playerScores.getOrDefault(roomId, new ConcurrentHashMap<>());
    }

    public void resetScores(String roomId) {
        Map<String, Integer> roomScores = playerScores.get(roomId);
        if (roomScores != null) {
            roomScores.replaceAll((k, v) -> 0);
        }
    }
}
