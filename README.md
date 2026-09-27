# 🏴‍☠️ Treasure Board

**Treasure Board** is a modern, native Android 2-player turn-based board game built with **Jetpack Compose**, **Material 3**, and pure Kotlin game logic.

Players take turns rolling the die, moving around an interactive game board, collecting treasures, avoiding traps, and competing for the highest score across two full laps!

---

## 🚀 Features

- **Local 2-Player Gameplay**: Turn-based multiplayer mode for two players on the same device.
- **Custom Canvas Board Drawing**: Smooth, custom-rendered board tiles, player tokens, and dice roll movement animations.
- **Dynamic Board Spaces**:
  - 🏁 **START**: Complete a lap every time you pass or land here.
  - 🪙 **COIN (+2 pts)**: Collect coins along the board.
  - 💎 **TREASURE (+5 pts)**: Uncover valuable treasures.
  - 👑 **GOLD CHEST (+8 pts)**: The ultimate jackpot!
  - 🪤 **TRAP (−3 pts)**: Watch out for traps! (Points cannot fall below 0).
  - ⚡ **BONUS STEP**: Automatically advance 2 extra spaces forward.
- **Audio & Haptics**: Rich sound effects and tactile haptic feedback for rolling dice, landing on spaces, collecting treasure, and game over states.
- **Customizable Themes & Settings**:
  - Dark & Light mode toggle with full edge-to-edge support.
  - Sound Effects & Haptics toggle.
  - Persisted user settings using **Preferences DataStore**.
- **Interactive Rules & Guides**: Built-in "How to Play" screen with interactive visual examples and space descriptions.

---

## 🛠️ Architecture & Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 design elements.
- **Architecture**: Unidirectional Data Flow (UDF) with `ViewModel`, `StateFlow`, and Repository pattern.
- **Data Persistence**: [Jetpack DataStore (Preferences)](https://developer.android.com/topic/libraries/architecture/datastore) for user settings.
- **Navigation**: [Jetpack Navigation Compose](https://developer.android.com/guide/navigation/navigation-compose).
- **Testing**: JUnit 4 unit tests for pure game rule calculations and state transitions (`TreasureRulesTest`).

---

## 🕹️ Game Rules

1. **Roll the Die**: Players take turns rolling a standard 6-sided die. Player 1 always starts.
2. **Move Clockwise**: Tokens move clockwise tile-by-tile according to the dice roll.
3. **Landing Effects**:
   - Scores increase/decrease based on the tile landed on.
   - Bonus tiles move the player 2 additional spaces forward without chaining bonuses.
4. **Game Finish**:
   - The game completes when a player finishes **2 laps**.
   - Player 2 always receives a final turn if Player 1 triggers the finish condition to ensure equal turns.
5. **Winning**: The player with the highest total score at the end wins!

---

## 📂 Project Structure

```text
com.example.treasureboard/
├── audio/            # Sound effects player and haptics manager
├── data/             # DataStore settings repository and preference models
├── game/             # Pure, UI-free board game engine, models, and state rules
├── ui/
│   ├── board/        # Custom Canvas board rendering & drawing logic
│   ├── components/   # Reusable Compose UI widgets & cards
│   ├── screens/      # Game, Home, HowToPlay, Result, Settings & Splash screens
│   └── theme/        # Material 3 color palettes and typography
└── MainActivity.kt   # Entry activity with Edge-to-Edge setup
```

---

## 🛠️ Requirements & Setup

- **Android Studio**: Ladybug / Meerkat or newer
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 37 (Android 15)
- **JDK Version**: 11 or higher

### Building the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/Treasure-Board.git
   cd Treasure-Board
   ```
2. Open the project in Android Studio.
3. Build and run on an emulator or physical Android device:
   ```bash
   ./gradlew assembleDebug
   ```

### Running Unit Tests

Run the pure Kotlin game rules unit test suite with Gradle:
```bash
./gradlew test
```

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
