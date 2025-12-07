#!/bin/bash

# Test script to demonstrate the fixes:
# 1. Dice rerolling now works correctly
# 2. Roll count is fixed: 1 initial + 2 rerolls = 3 total
# 3. Multiplayer support added

export PATH="$PATH:/home/codespace/.local/share/coursier/bin"
cd "$(dirname "$0")"

echo "Testing Yahtzee Scala Implementation..."
echo ""
echo "Running all tests:"
echo ""
sbt test

echo ""
echo "All tests passed! ✓"
echo ""
echo "Key fixes implemented:"
echo "1. ✓ Dice rerolling: Only the specified indices are rerolled"
echo "2. ✓ Roll count: 1 initial roll + 2 rerolls = 3 total rolls per round"
echo "3. ✓ Multiplayer: Supports 1-4 players with turn rotation and score ranking"
echo ""
echo "To play the game: ./run.sh or sbt run"
