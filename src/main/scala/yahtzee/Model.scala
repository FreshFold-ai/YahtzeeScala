package yahtzee

import scala.util.Random

case class Dice(values: List[Int]) {
  def roll: Dice = Dice(values.map(_ => Random.nextInt(6) + 1))
  def keepAndReroll(keepIndices: Set[Int]): Dice = 
    Dice(values.zipWithIndex.map { case (v, i) => 
      if (keepIndices.contains(i)) v else Random.nextInt(6) + 1
    })
}

object Dice {
  def initial: Dice = Dice(List.fill(5)(Random.nextInt(6) + 1))
}

sealed trait Category
case object Ones extends Category
case object Twos extends Category
case object Threes extends Category
case object Fours extends Category
case object Fives extends Category
case object Sixes extends Category
case object ThreeOfAKind extends Category
case object FourOfAKind extends Category
case object FullHouse extends Category
case object SmallStraight extends Category
case object LargeStraight extends Category
case object Yahtzee extends Category
case object Chance extends Category

object Scoring {
  def score(dice: List[Int], category: Category): Int = category match {
    case Ones => dice.filter(_ == 1).sum
    case Twos => dice.filter(_ == 2).sum
    case Threes => dice.filter(_ == 3).sum
    case Fours => dice.filter(_ == 4).sum
    case Fives => dice.filter(_ == 5).sum
    case Sixes => dice.filter(_ == 6).sum
    case ThreeOfAKind => if (hasNOfAKind(dice, 3)) dice.sum else 0
    case FourOfAKind => if (hasNOfAKind(dice, 4)) dice.sum else 0
    case FullHouse => if (isFullHouse(dice)) 25 else 0
    case SmallStraight => if (isSmallStraight(dice)) 30 else 0
    case LargeStraight => if (isLargeStraight(dice)) 40 else 0
    case Yahtzee => if (isYahtzee(dice)) 50 else 0
    case Chance => dice.sum
  }

  private def hasNOfAKind(dice: List[Int], n: Int): Boolean =
    dice.groupBy(identity).values.exists(_.length >= n)

  private def isFullHouse(dice: List[Int]): Boolean = {
    val counts = dice.groupBy(identity).values.map(_.length).toList.sorted
    counts == List(2, 3)
  }

  private def isSmallStraight(dice: List[Int]): Boolean = {
    val sorted = dice.sorted.distinct
    List(1,2,3,4).forall(sorted.contains) || 
    List(2,3,4,5).forall(sorted.contains) || 
    List(3,4,5,6).forall(sorted.contains)
  }

  private def isLargeStraight(dice: List[Int]): Boolean = {
    val sorted = dice.sorted.distinct
    sorted == List(1,2,3,4,5) || sorted == List(2,3,4,5,6)
  }

  private def isYahtzee(dice: List[Int]): Boolean =
    dice.distinct.length == 1
}

case class ScoreCard(scores: Map[Category, Int] = Map()) {
  def addScore(category: Category, points: Int): ScoreCard =
    if (scores.contains(category)) this 
    else ScoreCard(scores + (category -> points))

  def total: Int = scores.values.sum
  
  def availableCategories: List[Category] = List(
    Ones, Twos, Threes, Fours, Fives, Sixes,
    ThreeOfAKind, FourOfAKind, FullHouse, SmallStraight, LargeStraight, Yahtzee, Chance
  ).filter(!scores.contains(_))
}

case class GameState(
  scoreCard: ScoreCard = ScoreCard(),
  dice: Dice = Dice.initial,
  rollsLeft: Int = 0,
  round: Int = 1,
  hasRolled: Boolean = false
) {
  def rollDice: GameState = copy(dice = dice.roll, rollsLeft = 2, hasRolled = true)
  def rerollDice(keepIndices: Set[Int]): GameState = {
    val newDice = if (keepIndices.isEmpty) dice.roll else dice.keepAndReroll(keepIndices)
    copy(dice = newDice, rollsLeft = rollsLeft - 1)
  }
  def scoreRound(category: Category): GameState = {
    val points = Scoring.score(dice.values, category)
    copy(scoreCard = scoreCard.addScore(category, points), rollsLeft = 0, round = round + 1, hasRolled = false)
  }
  def isGameOver: Boolean = scoreCard.availableCategories.isEmpty
}

case class Player(name: String, gameState: GameState)

case class MultiPlayerGame(
  players: List[Player] = List(),
  currentPlayerIndex: Int = 0,
  round: Int = 1
) {
  def currentPlayer: Player = players(currentPlayerIndex)
  
  def updateCurrentPlayer(newState: GameState): MultiPlayerGame =
    copy(players = players.updated(currentPlayerIndex, currentPlayer.copy(gameState = newState)))
  
  def nextPlayer: MultiPlayerGame = {
    val nextIndex = (currentPlayerIndex + 1) % players.length
    if (nextIndex == 0) copy(currentPlayerIndex = nextIndex, round = round + 1)
    else copy(currentPlayerIndex = nextIndex)
  }
  
  def isGameOver: Boolean = players.forall(_.gameState.isGameOver)
  
  def scores: List[(String, Int)] = players.map(p => (p.name, p.gameState.scoreCard.total)).sortBy(-_._2)
}

object YahtzeeGame {
  def newGame: GameState = GameState()
  
  def newMultiPlayerGame(playerNames: List[String]): MultiPlayerGame =
    MultiPlayerGame(playerNames.map(name => Player(name, GameState())))
}
