// File: com/skible/be/skibleController/SkibleController.java
package com.skible.be.skibleController;

import com.skible.be.dto.*;
import com.skible.be.service.GameManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * WebSocket controller for managing rooms and chat in Skible.
 */
@RestController
@Controller
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SkibleController {

      private final GameManager gameManager;
      private final SimpMessagingTemplate messagingTemplate;

      @Autowired
      public SkibleController(GameManager gameManager,
                              SimpMessagingTemplate messagingTemplate) {
            this.gameManager = gameManager;
            this.messagingTemplate = messagingTemplate;
      }

      /**
       * Create a new room. Broadcasts the new room to all subscribers of /topic/room-created.
       */
      @MessageMapping("/create-room")
      @SendTo("/topic/room-created")
      public RoomResponse createRoom(CreateRoomRequest req) {
            return gameManager.createRoom(req.getPlayerName());
      }

      /**
       * Join an existing room. Sends the updated room state to subscribers of /topic/room/{roomId}.
       */
      @MessageMapping("/join-room")
      public void joinRoom(JoinRoomRequest request) {
            RoomResponse updatedRoom = gameManager.joinRoom(
                    request.getRoomId(), request.getPlayerName());
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId(),
                    updatedRoom
            );
      }

      /**
       * Handle chat messages. Broadcasts chat to subscribers of /topic/room/{roomId}/chat.
       */
      @MessageMapping("/chat")
      public void chat(ChatMessage message) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId() + "/chat",
                    message
            );
      }




      /*
         Added https POST Request for start game
       */

      @PostMapping("/player-ready")
      public ResponseEntity<?> playerReady(@RequestBody PlayerReadyRequest req) {
            boolean isReady =  gameManager.togglePlayerReady(req.getRoomId(),req.getPlayerName());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/player-ready",
                    new PlayerReadyResponse(req.getPlayerName(), isReady)
            );

            if (gameManager.allPlayerReady(req.getRoomId())) {
                  GameStateResponse resp = gameManager.startGame(req.getRoomId());

                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() + "/game-started",
                          resp
                  );
            }

            return ResponseEntity.ok().build();
      }


      /**  startGame, 60 sec Timer or usme muje player 1 ko yeh 3 word aayega
       * Get word options for a player to choose from.
       * Sends word options to subscribers of /topic/room/{roomId}/word-options.
       */
      @MessageMapping("/get-word-options")
      public void getWordOptions(WordOptionsRequest request) {
            // Get 3 random words from the WordService through GameManager
            List<String> options = gameManager.getWordOptions(3);

            WordOptionResponse response = new WordOptionResponse(request.getRoomId(), options);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/word-options",
                    response
            );
      }

      /**
       * Handle player's word selection.
       * Broadcasts the chosen word to subscribers of /topic/room/{roomId}/word-chosen.
       */
      @MessageMapping("/pick-word")
      public void pickWord(PickWordRequest request) {
            // Update game state to record the chosen word
            GameStateResponse updatedState = gameManager.setChosenWord(
                    request.getRoomId(), request.getChosenWord());

            // Broadcast the chosen word
            PickWordResponse wordResponse = new PickWordResponse(
                    request.getRoomId(), request.getChosenWord());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/word-chosen",
                    wordResponse
            );

            // Send game state response
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/game-state",
                    updatedState
            );
      }

      /**
       * Start the game.
       * Broadcasts game state to subscribers of /topic/room/{roomId}/game-state.
       */
      @MessageMapping("/start-game")
      public void startGame(GameStateRequest request) {
            GameStateResponse state = gameManager.startGame(request.getRoomId());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/game-state",
                    state
            );
      }

}