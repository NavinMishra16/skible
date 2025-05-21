package com.skible.be.skibleController;
import java.util.List;
import com.skible.be.dto.*;
import com.skible.be.service.GameManager;
import com.skible.be.service.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.SendTo;


import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Controller
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SkibleController {

      private final GameManager gameManager;
      private final GameStateService gameStateService;
      private final SimpMessagingTemplate messagingTemplate;

      @Autowired
      public SkibleController(GameManager gameManager, GameStateService gameStateService,
                              SimpMessagingTemplate messagingTemplate) {
            this.gameManager = gameManager;
            this.messagingTemplate = messagingTemplate;
            this.gameStateService = gameStateService;
      }

      //──────────────────────────────────────────────────────────────────
      // Room creation & joining

      @MessageMapping("/create-room")
      @SendTo("/topic/room-created")
      public RoomResponse createRoom(CreateRoomRequest req) {
            return gameManager.createRoom(req.getPlayerName());
      }

      @MessageMapping("/join-room")
      public void joinRoom(JoinRoomRequest request) {
            RoomResponse updatedRoom = gameManager.joinRoom(
                    request.getRoomId(), request.getPlayerName());
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId(),
                    updatedRoom
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Chat

      @MessageMapping("/chat")
      public void chat(ChatMessage message) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoomId() + "/chat",
                    message
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Ready & game start

      @PostMapping("/player-ready")
      public ResponseEntity<?> playerReady(@RequestBody PlayerReadyRequest req) {
            boolean isReady = gameManager.togglePlayerReady(req.getRoomId(), req.getPlayerName());

            // Broadcast this player's ready status
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/player-ready",
                    new PlayerReadyResponse(req.getPlayerName(), isReady)
            );

            // If everyone is now ready, start the game and send first turn
            if (gameManager.allPlayerReady(req.getRoomId())) {
                  GameStateResponse resp = gameManager.startGame(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() + "/game-started",
                          resp
                  );

                  // broadcast whose turn it is first
                  String firstPlayer = gameStateService.getCurrentPlayer(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() + "/turn-start",
                          Map.of("currentPlayer", firstPlayer)
                  );
            }

            return ResponseEntity.ok().build();
      }

      //──────────────────────────────────────────────────────────────────
      // Turn‐based word option & selection

      /**
       * Only the current player may request their three options.
       */
      @MessageMapping("/get-word-options")
      public void getWordOptions(WordOptionsRequest request, Principal principal) {
            // throws if not this player's turn
            String playerName;

            // Check if Principal is available, otherwise use the playerName from the request
            if (principal != null) {
                  playerName = principal.getName();
            } else {
                  playerName = request.getPlayerName();
            }

            // Throws if not this player's turn
            List<String> options = gameManager.getWordOptionsForRoom(
                    request.getRoomId(),
                    playerName
            );

            WordOptionResponse response = new WordOptionResponse(request.getRoomId(), options);
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/word-options",
                    response
            );
      }

      /**
       * Handle a player's pick, advance turn, and notify the next player.
       */
      @MessageMapping("/pick-word")
      public void pickWord(PickWordRequest request, Principal principal) {
            // record choice and get next player's name

            String playerName ;
            if(principal != null){
                  playerName = principal.getName();
            }
            else playerName = request.getPlayerName();

            String nextPlayer = gameManager.chooseWordAndAdvance(
                    request.getRoomId(),
                    playerName,
                    request.getChosenWord()
            );

            PickWordResponse wordResponse = new PickWordResponse(
                    request.getRoomId(), request.getChosenWord()
            );

            messagingTemplate.convertAndSend(
                    "/topic/room" + request.getRoomId() + "/word-chosen",wordResponse
            );

            GameStateResponse updatedState = gameManager.getGameState(request.getRoomId());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/game-state",
                    updatedState
            );

            // broadcast turn start for next player
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/turn-start",
                    Map.of("currentPlayer", nextPlayer)
            );

//            String nextPlayer = gameManager.chooseWordAndAdvance(
//                    request.getRoomId(),
//                    principal.getName(),
//                    request.getChosenWord()
//            );
//
//            // broadcast the chosen word to all
//            PickWordResponse wordResponse = new PickWordResponse(
//                    request.getRoomId(),
//                    request.getChosenWord()
//            );
//            messagingTemplate.convertAndSend(
//                    "/topic/room/" + request.getRoomId() + "/word-chosen",
//                    wordResponse
//            );
//
//            // broadcast updated game state
//            GameStateResponse updatedState = gameManager.getGameState(request.getRoomId());
//            messagingTemplate.convertAndSend(
//                    "/topic/room/" + request.getRoomId() + "/game-state",
//                    updatedState
//            );
//
//            // broadcast turn start for next player
//            messagingTemplate.convertAndSend(
//                    "/topic/room/" + request.getRoomId() + "/turn-start",
//                    Map.of("currentPlayer", nextPlayer)
//            );
      }
}
