package yahtzee

import scala.io.StdIn
import scala.util.Try

object UI {
  def clearScreen(): Unit = {
    print("\u001b[2J\u001b[H")
  }

  def displayGame(state: GameState, playerName: String = ""): Unit = {
    clearScreen()
    val playerInfo = if (playerName.nonEmpty) s" - $playerName" else ""
    println("╔════════════════════════════════════════╗")
    println("║         YAHTZEE - ROUND " + f"${state.round}%2d" + playerInfo.padTo(8, ' ') + "║")
    println("╚════════════════════════════════════════╝")
    println()
    
    displayDice(state.dice)
    println()
    if (state.hasRolled) {
      println(s"Rerolls left: ${state.rollsLeft}")
      println()
    }
    
    displayScoreCard(state.scoreCard)
    println()
    println(s"Current Total: ${state.scoreCard.total}")
    println()
  }

  def displayDice(dice: Dice): Unit = {
    println("┌─────────┬─────────┬─────────┬─────────┬─────────┐")
    println("│ Die 1   │ Die 2   │ Die 3   │ Die 4   │ Die 5   │")
    println("├─────────┼─────────┼─────────┼─────────┼─────────┤")
    println(
      "│   " + dice.values(0) + "     │   " + dice.values(1) + "     │   " + dice.values(2) + "     │   " + dice.values(3) + "     │   " + dice.values(4) + "     │"
    )
    println("├─────────┼─────────┼─────────┼─────────┼─────────┤")
    println("│ Index 0 │ Index 1 │ Index 2 │ Index 3 │ Index 4 │")
    println("└─────────┴─────────┴─────────┴─────────┴─────────┘")
  }

  def displayScoreCard(card: ScoreCard): Unit = {
    println("SCORE CARD:")
    println("──────────────────────────────────")
    
    val allCategories = List(
      (Ones, "Ones"),
      (Twos, "Twos"),
      (Threes, "Threes"),
      (Fours, "Fours"),
      (Fives, "Fives"),
      (Sixes, "Sixes"),
      (ThreeOfAKind, "Three of a Kind"),
      (FourOfAKind, "Four of a Kind"),
      (FullHouse, "Full House"),
      (SmallStraight, "Small Straight"),
      (LargeStraight, "Large Straight"),
      (Yahtzee, "Yahtzee"),
      (Chance, "Chance")
    )
    
    allCategories.foreach { case (cat, name) =>
      val score = card.scores.get(cat).map(_.toString).getOrElse("  -")
      println(f"$name%-20s: $score%4s")
    }
    println("──────────────────────────────────")
  }

  def promptAction(state: GameState): GameState = {
    if (state.rollsLeft == 3) {
      println("Press Enter to roll dice...")
      StdIn.readLine()
      state.rollDice
    } else {
      println()
      println("Options:")
      println("1. Reroll dice (enter indices 0-4 separated by spaces, e.g., '0 2 4')")
      println("2. Score this round (enter category number)")
      println()
      
      val input = StdIn.readLine("Choose action: ").trim
      
      if (input.isEmpty) {
        println("Invalid input. Try again.")
        promptAction(state)
      } else {
        Try {
          input.split("\\s+").map(_.toInt)
        }.toOption match {
          case Some(indices) if indices.forall(i => i >= 0 && i <= 4) =>
            state.rerollDice(indices.toSet)
          case _ =>
            // Try parsing as category
            Try(input.toInt).toOption match {
              case Some(catNum) if catNum >= 1 && catNum <= state.scoreCard.availableCategories.length =>
                val category = state.scoreCard.availableCategories(catNum - 1)
                state.scoreRound(category)
              case _ =>
                println("Invalid input. Try again.")
                promptAction(state)
            }
        }
      }
    }
  }

  def promptScore(state: GameState): Category = {
    println()
    println("Available categories:")
    state.scoreCard.availableCategories.zipWithIndex.foreach { case (cat, idx) =>
      val potentialScore = Scoring.score(state.dice.values, cat)
      val catName = categoryName(cat)
      println(f"${idx + 1}%2d. $catName%-20s (${potentialScore} points)")
    }
    println()
    
    var validInput = false
    var category: Category = Ones
    
    while (!validInput) {
      Try(StdIn.readLine("Select category (1-" + state.scoreCard.availableCategories.length + "): ").trim.toInt)
        .toOption match {
          case Some(num) if num >= 1 && num <= state.scoreCard.availableCategories.length =>
            category = state.scoreCard.availableCategories(num - 1)
            validInput = true
          case _ =>
            println("Invalid input. Try again.")
        }
    }
    
    category
  }

  def promptReroll(state: GameState): GameState = {
    println()
    println(s"You have ${state.rollsLeft} roll(s) left.")
    println()
    println("Choose which dice to KEEP (the others will be rerolled):")
    println("  Enter dice indices to KEEP (0-4 separated by spaces)")
    println("    Example: '0 2 4' keeps dice at indices 0, 2, and 4 (rerolls indices 1 and 3)")
    println("  Press 's' to SCORE this round (end turn)")
    println("  Press Enter to REROLL ALL dice (keep none)")
    
    val input = StdIn.readLine("Indices to keep: ").trim.toLowerCase
    
    if (input == "s") {
      // Score now
      val category = promptScore(state)
      state.scoreRound(category)
    } else if (input.isEmpty) {
      // Reroll all dice - keep none (empty set)
      state.rerollDice(Set.empty[Int])
    } else {
      Try {
        input.split("\\s+").map(_.toInt).toSet
      }.toOption match {
        case Some(keptIndices) if keptIndices.forall(i => i >= 0 && i <= 4) && keptIndices.nonEmpty =>
          // Pass the kept indices directly
          state.rerollDice(keptIndices)
        case _ =>
          println("Invalid input. Try again.")
          promptReroll(state)
      }
    }
  }

  def promptNumPlayers(): Int = {
    println()
    var validInput = false
    var numPlayers = 1
    
    while (!validInput) {
      Try(StdIn.readLine("Number of players (1-4): ").trim.toInt).toOption match {
        case Some(n) if n >= 1 && n <= 4 =>
          numPlayers = n
          validInput = true
        case _ =>
          println("Invalid input. Please enter a number between 1 and 4.")
      }
    }
    
    numPlayers
  }

  def promptPlayerNames(count: Int): List[String] = {
    (1 to count).map { i =>
      StdIn.readLine(s"Player $i name: ").trim match {
        case name if name.nonEmpty => name
        case _ => s"Player $i"
      }
    }.toList
  }

  def displayMultiPlayerGameOver(game: MultiPlayerGame): Unit = {
    clearScreen()
    println("╔════════════════════════════════════════╗")
    println("║           GAME OVER!                   ║")
    println("╚════════════════════════════════════════╝")
    println()
    println("FINAL SCORES (ranked):")
    println("──────────────────────────────────")
    
    game.scores.zipWithIndex.foreach { case ((name, score), idx) =>
      println(f"${idx + 1}. $name%-20s: $score%4d")
    }
    
    println("──────────────────────────────────")
    if (game.scores.nonEmpty) {
      println(s"🏆 WINNER: ${game.scores.head._1} with ${game.scores.head._2} points!")
    }
    println()
  }

  def categoryName(cat: Category): String = cat match {
    case Ones => "Ones"
    case Twos => "Twos"
    case Threes => "Threes"
    case Fours => "Fours"
    case Fives => "Fives"
    case Sixes => "Sixes"
    case ThreeOfAKind => "Three of a Kind"
    case FourOfAKind => "Four of a Kind"
    case FullHouse => "Full House"
    case SmallStraight => "Small Straight"
    case LargeStraight => "Large Straight"
    case Yahtzee => "Yahtzee"
    case Chance => "Chance"
  }

  def displayGameOver(state: GameState): Unit = {
    clearScreen()
    println("╔════════════════════════════════════════╗")
    println("║           GAME OVER!                   ║")
    println("╚════════════════════════════════════════╝")
    println()
    displayScoreCard(state.scoreCard)
    println()
    println(f"FINAL SCORE: ${state.scoreCard.total}")
    println()
  }
}
