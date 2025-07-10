package com.skible.be.skibleController;
import com.skible.be.dto.*;
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
      private final GameStateService gameStateService;
      private final SimpMessagingTemplate messagingTemplate;

      public SkibleController(
              GameManager gameManager,GameStateService gameStateService,
              SimpMessagingTemplate messagingTemplate){    
        this.gameManager = gameManager;  
        this.gameStateService = gameStateService;
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
      public RoomResponse createRoom(CreateRoomRequest req) {
            return gameManager.createRoom(req.getPlayerName());
      }

      @MessageMapping("/join-room")
      public void joinRoom(JoinRoomRequest req) {
            RoomResponse room = gameManager.joinRoom(req.getRoomId(), req.getPlayerName());
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId(),
                    room
            );
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
      public ResponseEntity<?> playerReady(@RequestBody PlayerReadyRequest request) {

            boolean isReady = gameManager.togglePlayerReady(request.roomId(),request.playerName());

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.roomId() + "/player-ready",

                    Map.of(
                        "playerName",request.playerName(),
                        "ready",isReady
                        )
            );

            if (gameManager.allPlayerReady(request.roomId())) {
                  // 1) start game
                  GameState gameState = gameManager.startGame(request.roomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + request.roomId() + "/game-started",
                          Map.of(
                                "roomId", gameState.getRoomId(),
                                "players", gameState.getPlayers(),
                                "currentRound", gameState.getCurrentRound(),
                                "currentPlayer", gameState.getCurrentPlayer(),
                                "status", gameState.getStatus()
                          )
                  );

                  // 2) announce first pick-phase

                  String firstPicker = gameManager.getCurrentPicker(request.roomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + request.roomId() + "/turn-start",

                          Map.of(
                                  "currentPlayer", firstPicker,
                                  "phase", "PICK"
                          )
                  );
            }
            return ResponseEntity.ok().build();
      }

      //──────────────────────────────────────────────────────────────────
      // Pick-phase: word options & selection

      @MessageMapping("/get-word-options")
      public void getWordOptions(@Payload PlayerReadyRequest request) {

            List<String> opts = gameManager.getWordOptionsForRoom(
                    request.roomId(), request.playerName()
            );

            messagingTemplate.convertAndSend(
                    "/topic/room/" + request.roomId() + "/word-options",
                    Map.of(
                        "roomId", request.roomId(),
                        "options", opts
                    )
            );
      }

      @MessageMapping("/pick-word")
      public void pickWord(PickWordRequest req) {
            // 1) record pick & flip to guesser
            String guesser = gameManager.chooseWordAndAdvance(
                    req.getRoomId(), req.getPlayerName(), req.getChosenWord()
            );


            // 2) broadcast the chosen word
            messagingTemplate.convertAndSend(
                    
            "/topic/room/" + req.getRoomId() + "/word-chosen",
            Map.of(       
            "roomId",req.getRoomId(),    
            "chosenWord",req.getChosenWord()       
            )
                    );

            // 3) broadcast updated game state
             GameState gameState = gameManager.getGameState(req.getRoomId());
             messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/game-state",
                    gameState
                    );

            // 4) announce guess-phase
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/turn-start",
                    Map.of(
                            "currentPlayer", guesser,
                            "phase", "GUESS"
                    )
            );

            // 5) optional: send guess prompt
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/guess-request",
                    Map.of("playerToGuess", guesser, "prompt", "Type your guess in chat")
            );
      }

      //──────────────────────────────────────────────────────────────────
      // Guess-phase: submission & result

      @MessageMapping("/make-guess")
      public void makeGuess(GuessRequest req) {
            // 1) process guess
            boolean correct = gameManager.processGuess(
                    req.getRoomId(), req.getPlayerName(), req.getGuess()
            );

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
            // 3) if Guess is right,broadCast updateScore
            if(correct){
                  broadCastScore(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" +
                                  "/score-update",
                          Map.of(
                                  "guesser", req.getPlayerName(),
                                  "word" , req.getGuess(),
                                  "message" , req.getPlayerName() + "earned a point"
                          )
                  );
            }

            // 3) Advance to next round after guess
            String nextPicker = gameManager.advanceAfterGuess(req.getRoomId());

            // 4) announce next pick-phase
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/turn-start",
                    Map.of(
                            "currentPlayer", nextPicker,
                            "phase", "PICK"
                    )
            );


            // 5) immediately send word-options for next pick
            List<String> opts = gameManager.getWordOptionsForRoom(req.getRoomId(), nextPicker);
            messagingTemplate.convertAndSend(
            "/topic/room/" + req.getRoomId() + "/word-options",
            Map.of("roomId", req.getRoomId(), "options", opts)
            );
      }
}
