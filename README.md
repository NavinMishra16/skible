# Skible Multiplayer Game – Fixes & Updates

This project enables real-time multiplayer gameplay using WebSockets (STOMP over SockJS), with player chat, a ready-up system, and automatic game start when both players are ready.

---

##  Key Fixes

###  1. Room ID Overwriting Fix
- **Issue:** Whenever any player created a room, a new `RoomResponse` with a fresh UUID was returned, potentially overwriting the existing room.
- **Fix:** Separated room initialization logic to avoid regenerating the `roomId` if it already exists. Now the host's room ID is preserved and reused properly.

### 2. Host Player Not Added to Game State
- **Issue:** The host (creator) was not automatically added to the `GameState`, causing `startGame` to fail due to insufficient players.
- **Fix:** Updated the `createRoom` logic to add the host player to both the room and the corresponding game state immediately during initialization.

### 3.GameState Switching twice while playing 
- **Issue:** The host (creator) was always picking up the word and guest (player 2) was always gueeessing
- **Fix** : Added the decriptive answer in [Feature]#1

---

##  Features Summary

- Room creation and joining
- Real-time chat and ready state
- Auto game start when all players are ready
- Game state management with backend sync
- Options Added to choose Word / turn based option to choose Word

- Guess logic from ChatOption and validatiton (Completed) 

---
## Next Feature 

- Quality of life : ScoreBoard 
- Quality of life : GAME END and declaring winner


## 🧪 How to Test

1. Open `index.html` in two browser tabs.
2. Create a room in one tab (host), join in the second (guest).
3. Mark both players as ready — the game will auto-start and log will update.
