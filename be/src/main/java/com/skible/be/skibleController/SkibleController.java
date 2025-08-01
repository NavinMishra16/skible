package com.skible.be.skibleController;
import com.skible.be.dto.*;
import com.skible.be.model.GameState;
import com.skible.be.service.GameManager;
import com.skible.be.service.GameStateService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SkibleController {

      private final GameManager gameManager;
      private final SimpMessagingTemplate messagingTemplate;

      public SkibleController(
              GameManager gameManager,GameStateService gameStateService,
              SimpMessagingTemplate messagingTemplate){    
        this.gameManager = gameManager;  
        this.messagingTemplate = messagingTemplate;
        }

      // Helper Method to BroadCast the score of current room
      private void broadCastScore(String roomId){
            Map<String,Integer>scores = gameManager.getScores(roomId);
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/scores",
                    scores
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Room creation & joining

      @MessageMapping("/create-room")
      @SendTo("/topic/room-created")
      public RoomResponse createRoom(@Payload Map<String,String> payload) {

        String playerName  = payload.get("playerName").trim();
        System.out.println("[DEBUG] Creating room for player: " + playerName);
        return gameManager.createRoom(playerName);
        
      }

      @MessageMapping("/join-room")
      public void joinRoom(JoinRoomRequest req) {
            System.out.println("[DEBUG] Player " + req.getPlayerName() + " joining room: " + req.getRoomId());
            RoomResponse room = gameManager.joinRoom(req.getRoomId(), req.getPlayerName());
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId(),
                    room
            );
            System.out.println("[DEBUG] Join room message sent to: /topic/room/" + req.getRoomId());
      }

      //──────────────────────────────────────────────────────────────────
      // Chat

      @MessageMapping("/chat")
      public void chat(ChatMessage msg) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + msg.getRoomId() + "/chat",
                    msg
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Ready & game start

      @PostMapping("/player-ready")
      public ResponseEntity<?> playerReady(@RequestBody Map<String,String>request) {
            String roomId = request.get("roomId");
            String playerName = request.get("playerName");
            
            System.out.println("[DEBUG] Player ready request - Room: " + roomId + ", Player: " + playerName);

            if(roomId == null || playerName == null){
                System.out.println("[DEBUG] Invalid ready request - missing roomId or playerName");
                return ResponseEntity.badRequest().build();
            }

            boolean isReady = gameManager.togglePlayerReady(roomId, playerName);
            System.out.println("[DEBUG] Player " + playerName + " ready status: " + isReady);

            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/player-ready",
                    Map.of(
                        "playerName", playerName,
                        "ready", isReady
                    )
            );

            boolean allReady = gameManager.allPlayerReady(roomId);
            System.out.println("[DEBUG] All players ready: " + allReady);

            if (allReady) {
                  System.out.println("[DEBUG] Starting game for room: " + roomId);
                  
                  // 1) start game
                  GameState gameState = gameManager.startGame(roomId);
                  System.out.println("[DEBUG] Game started. Current player: " + gameState.getCurrentPlayer());
                  
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + roomId + "/game-started",
                          Map.of(
                                "roomId", gameState.getRoomId(),
                                "players", gameState.getPlayers(),
                                "currentRound", gameState.getCurrentRound(),
                                "currentPlayer", gameState.getCurrentPlayer(),
                                "status", gameState.getStatus()
                          )
                  );
                  System.out.println("[DEBUG] Game started message sent to: /topic/room/" + roomId + "/game-started");

                  // 2) announce first pick-phase
                  String firstPicker = gameManager.getCurrentPicker(roomId);
                  System.out.println("[DEBUG] First picker: " + firstPicker);
                  
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + roomId + "/turn-start",
                          Map.of(
                                  "currentPlayer", firstPicker,
                                  "phase", "PICK"
                          )
                  );
                  System.out.println("[DEBUG] Turn start message sent to: /topic/room/" + roomId + "/turn-start");

                  // 3) Get and send word options
                  try {
                        System.out.println("[DEBUG] Getting word options for: " + firstPicker);
                        List<String> opts = gameManager.getWordOptionsForRoom(roomId, firstPicker);
                        System.out.println("[DEBUG] Got word options: " + opts);
                        
                        String topic = "/topic/room/" + roomId + "/word-options";
                        System.out.println("[DEBUG] Sending word options to topic: " + topic);
                        
                        Map<String, Object> payload = Map.of(
                                "roomId", roomId,
                                "options", opts
                        );
                        System.out.println("[DEBUG] Word options payload: " + payload);
                        
                        messagingTemplate.convertAndSend(topic, payload);
                        System.out.println("[DEBUG] Word options sent successfully!");
                        
                  } catch(Exception e) {
                        System.out.println("[ERROR] Exception getting/sending word options:");
                        e.printStackTrace();
                        
                        // Send error message to help debug
                        messagingTemplate.convertAndSend(
                            "/topic/room/" + roomId + "/error",
                            Map.of("message", "Error getting word options: " + e.getMessage())
                        );
                  }
            }
            return ResponseEntity.ok(Map.of("ready", isReady));
      }

      //──────────────────────────────────────────────────────────────────
      // Pick-phase: word options & selection

      @MessageMapping("/get-word-options")
      public void getWordOptions(@Payload Map<String,String> request) {
            String roomId = request.get("roomId");
            String playerName = request.get("playerName");
            
            System.out.println("[DEBUG] Manual word options request - Room: " + roomId + ", Player: " + playerName);

            try {
                List<String> opts = gameManager.getWordOptionsForRoom(roomId, playerName);
                System.out.println("[DEBUG] Manual word options: " + opts);

                messagingTemplate.convertAndSend(
                        "/topic/room/" + roomId + "/word-options",
                        Map.of(
                            "roomId", roomId,
                            "options", opts
                        )
                );
                System.out.println("[DEBUG] Manual word options sent to: /topic/room/" + roomId + "/word-options");
            } catch (Exception e) {
                System.out.println("[ERROR] Error in manual word options:");
                e.printStackTrace();
            }
      }

      @MessageMapping("/pick-word")
      public void pickWord(PickWordRequest request) {
            System.out.println("[DEBUG] Word picked: " + request.getChosenWord() + " by " + request.getPlayerName());

            // 1) record pick 
            String guesser = gameManager.chooseWordAndRecord(
                    request.getRoomId(), request.getPlayerName(), request.getChosenWord()
            );
            System.out.println("[DEBUG] Guesser for this round: " + guesser);

            // 2) broadcast the chosen word
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/word-chosen",
                    Map.of(       
                        "roomId", request.getRoomId(),    
                        "chosenWord", request.getChosenWord()       
                    )
            );

            // 3) broadcast updated game state
            GameState gameState = gameManager.getGameState(request.getRoomId());
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/game-state",
                    gameState
            );

            // 4) announce guess-phase
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/turn-start",
                    Map.of(
                            "currentPlayer", guesser,
                            "phase", "GUESS"
                    )
            );

            // 5) optional: send guess prompt
            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.getRoomId() + "/guess-request",
                    Map.of("playerToGuess", guesser, "prompt", "Type your guess in chat")
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Guess-phase: submission & result

      @MessageMapping("/make-guess")
      public void makeGuess(GuessRequest req) {
            System.out.println("[DEBUG] Guess made: " + req.getGuess() + " by " + req.getPlayerName());
            
            // 1) process guess
            boolean correct = gameManager.processGuess(
                    req.getRoomId(), req.getPlayerName(), req.getGuess()
            );
            System.out.println("[DEBUG] Guess was " + (correct ? "CORRECT" : "WRONG"));

            // 2) broadcast result
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/guess-result",
                    new GuessResultResponse(
                            req.getRoomId(),
                            req.getPlayerName(),
                            req.getGuess(),
                            correct
                    )
            );

            // 3) if Guess is right, broadcast updateScore
            if(correct){
                  broadCastScore(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() +  "/score-update",
                          Map.of(
                                  "guesser", req.getPlayerName(),
                                  "word", req.getGuess(),
                                  "message", req.getPlayerName() + " earned a point"
                          )
                  );
            }

            // 4) Advance to next round after guess
            String nextPicker = gameManager.advanceAfterGuess(req.getRoomId());
            System.out.println("[DEBUG] Next picker: " + nextPicker);

            // 5) announce next pick-phase
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/turn-start",
                    Map.of(
                            "currentPlayer", nextPicker,
                            "phase", "PICK"
                    )
            );

            // 6) immediately send word-options for next pick
            try {
                List<String> opts = gameManager.getWordOptionsForRoom(req.getRoomId(), nextPicker);
                System.out.println("[DEBUG] Next round word options: " + opts);
                
                messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/word-options",
                    Map.of("roomId", req.getRoomId(), "options", opts)
                );
                System.out.println("[DEBUG] Next round word options sent successfully");
            } catch (Exception e) {
                System.out.println("[ERROR] Error getting next round word options:");
                e.printStackTrace();
            }
      }
}