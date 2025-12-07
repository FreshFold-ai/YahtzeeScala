# Yahtzee in Scala

A fully functional implementation of Yahtzee written in pure Scala with a terminal-based UI and multiplayer support.

## Building & Running

### Build the project:
```bash
sbt compile
```

### Run the game:
```bash
./run.sh
# or
sbt run
```

### Run tests:
```bash
./test.sh
# or
sbt test
```

## How to Play

1. Start a new game by running `./run.sh`
2. Enter the number of players (1-4)
3. Enter each player's name
4. **Round flow (3 total rolls per round):**
   - **Initial roll**: Press Enter to roll all 5 dice (automatically happens)
   - After the initial roll, you have **2 more rolls** available
   - The display shows "Rerolls left: 2" then "Rerolls left: 1" then you must score
   - On each turn you can:
     - Enter indices to **KEEP** (e.g., `2 3` keeps dice at indices 2 and 3, rerolls the rest)
     - Press `s` to score the round and end your turn
     - Press Enter (empty) to reroll all dice
   - When you run out of rolls, you must score

## How Dice Selection Works

**IMPORTANT**: You select which dice to KEEP, not which to reroll!

When prompted "Indices to keep:":
- Example: `2 3` means **KEEP** dice at indices 2 and 3
- All other dice (0, 1, 4) will be rerolled with new random values
- Your kept dice will show the exact same values in the next display

## Architecture

- `Model.scala`: Core game logic (Dice, Scoring, ScoreCard, GameState, Player, MultiPlayerGame)
- `UI.scala`: Terminal interface and user prompts
- `Main.scala`: Game loop and entry point
- `YahtzeeTest.scala`: 22 comprehensive tests (all passing)

All code is written in a purely functional style with no mutable state.
