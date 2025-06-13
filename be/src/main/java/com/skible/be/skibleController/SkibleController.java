package com.skible.be.skibleController;
import com.skible.be.dto.*;
import com.skible.be.service.GameManager;
import com.skible.be.service.GameStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
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

      @Autowired
      public SkibleController(
              GameManager gameManager,
              GameStateService gameStateService,
              SimpMessagingTemplate messagingTemplate
      ){
            this.gameManager      = gameManager;
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
      public ResponseEntity<?> playerReady(@RequestBody PlayerReadyRequest req) {
            boolean isReady = gameManager.togglePlayerReady(req.getRoomId(), req.getPlayerName());

            // broadcast ready toggle
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/player-ready",
                    new PlayerReadyResponse(req.getPlayerName(), isReady)
            );

            if (gameManager.allPlayerReady(req.getRoomId())) {
                  // 1) start game
                  GameStateResponse state = gameManager.startGame(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() + "/game-started",
                          state
                  );

                  // 2) announce first pick-phase
                  String firstPicker = gameManager.getCurrentPicker(req.getRoomId());
                  messagingTemplate.convertAndSend(
                          "/topic/room/" + req.getRoomId() + "/turn-start",
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
      public void getWordOptions(WordOptionsRequest req) {
            List<String> opts = gameManager.getWordOptionsForRoom(
                    req.getRoomId(), req.getPlayerName()
            );
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/word-options",
                    new WordOptionResponse(req.getRoomId(), opts)
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
                    new PickWordResponse(req.getRoomId(), req.getChosenWord())
            );

            // 3) broadcast updated game state
            messagingTemplate.convertAndSend(
                    "/topic/room/" + req.getRoomId() + "/game-state",
                    gameManager.getGameState(req.getRoomId())
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
                    new WordOptionResponse(req.getRoomId(), opts)
            );
      }
}
