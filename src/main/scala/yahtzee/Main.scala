package yahtzee

import scala.annotation.tailrec
import scala.io.StdIn

object Main extends App {
  @tailrec
  def playerTurn(state: GameState): GameState = {
    if (state.isGameOver) {
      state
    } else {
      if (!state.hasRolled) {
        // Start of round - must roll
        UI.displayGame(state)
        println("Press Enter to roll dice...")
        StdIn.readLine()
        playerTurn(state.rollDice)
      } else {
        // Mid-round - have rolled, now choose to reroll or score
        UI.displayGame(state)
        
        if (state.rollsLeft == 0) {
          // No more rolls left - must score now
          val category = UI.promptScore(state)
          playerTurn(state.scoreRound(category))
        } else {
          // Still have rolls left - can reroll or score
          val newState = UI.promptReroll(state)
          playerTurn(newState)
        }
      }
    }
  }

  @tailrec
  def multiPlayerGameLoop(game: MultiPlayerGame): MultiPlayerGame = {
    if (game.isGameOver) {
      game
    } else {
      val player = game.currentPlayer
      val updatedState = playerTurn(player.gameState)
      val updatedGame = game.updateCurrentPlayer(updatedState).nextPlayer
      multiPlayerGameLoop(updatedGame)
    }
  }

  UI.clearScreen()
  println("╔════════════════════════════════════════╗")
  println("║    WELCOME TO YAHTZEE IN SCALA!        ║")
  println("╚════════════════════════════════════════╝")
  println()

  val numPlayers = UI.promptNumPlayers()
  val playerNames = UI.promptPlayerNames(numPlayers)
  
  val initialGame = YahtzeeGame.newMultiPlayerGame(playerNames)
  val finalGame = multiPlayerGameLoop(initialGame)
  
  UI.displayMultiPlayerGameOver(finalGame)
}
