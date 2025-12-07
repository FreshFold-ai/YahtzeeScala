package yahtzee

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class YahtzeeTest extends AnyFlatSpec with Matchers {
  "Scoring" should "score ones correctly" in {
    Scoring.score(List(1, 1, 2, 3, 4), Ones) shouldBe 2
  }

  it should "score full house" in {
    Scoring.score(List(2, 2, 3, 3, 3), FullHouse) shouldBe 25
  }

  it should "not score full house for non-full-house" in {
    Scoring.score(List(1, 2, 3, 4, 5), FullHouse) shouldBe 0
  }

  it should "score yahtzee" in {
    Scoring.score(List(5, 5, 5, 5, 5), Yahtzee) shouldBe 50
  }

  it should "score three of a kind" in {
    Scoring.score(List(3, 3, 3, 1, 2), ThreeOfAKind) shouldBe 12
  }

  it should "score four of a kind" in {
    Scoring.score(List(2, 2, 2, 2, 5), FourOfAKind) shouldBe 13
  }

  it should "score small straight" in {
    Scoring.score(List(1, 2, 3, 4, 6), SmallStraight) shouldBe 30
  }

  it should "score large straight" in {
    Scoring.score(List(1, 2, 3, 4, 5), LargeStraight) shouldBe 40
  }

  it should "score chance" in {
    Scoring.score(List(1, 2, 3, 4, 5), Chance) shouldBe 15
  }

  "ScoreCard" should "track scores and prevent duplicates" in {
    val card = ScoreCard().addScore(Ones, 3).addScore(Ones, 5)
    card.scores(Ones) shouldBe 3
  }

  it should "calculate total" in {
    val card = ScoreCard().addScore(Ones, 3).addScore(Twos, 6)
    card.total shouldBe 9
  }

  it should "track available categories" in {
    val card = ScoreCard().addScore(Ones, 3)
    card.availableCategories should not contain Ones
    card.availableCategories should contain(Twos)
  }

  "GameState" should "be game over when all categories are filled" in {
    val card = List(
      Ones, Twos, Threes, Fours, Fives, Sixes,
      ThreeOfAKind, FourOfAKind, FullHouse, SmallStraight, LargeStraight, Yahtzee, Chance
    ).foldLeft(ScoreCard())((c, cat) => c.addScore(cat, 10))
    
    GameState(scoreCard = card).isGameOver shouldBe true
  }

  it should "not be game over with available categories" in {
    GameState().isGameOver shouldBe false
  }

  it should "properly track rolls (1 initial, 2 rerolls)" in {
    val state = GameState(rollsLeft = 0, hasRolled = false)
    val afterRoll = state.rollDice
    afterRoll.rollsLeft shouldBe 2
    afterRoll.hasRolled shouldBe true
    
    val afterReroll = afterRoll.rerollDice(Set(0, 1))  // Keep 0 and 1
    afterReroll.rollsLeft shouldBe 1
    afterReroll.hasRolled shouldBe true
  }

  "Dice" should "have 5 values" in {
    val dice = Dice.initial
    dice.values.length shouldBe 5
  }

  it should "have values between 1 and 6" in {
    val dice = Dice.initial
    dice.values.forall(v => v >= 1 && v <= 6) shouldBe true
  }

  it should "keep specified indices and reroll others" in {
    val dice = Dice(List(1, 2, 3, 4, 5))
    val rerolled = dice.keepAndReroll(Set(1, 3))  // Keep indices 1 and 3, reroll 0, 2, 4
    
    // Indices 1 and 3 should be unchanged (2 and 4)
    rerolled.values(1) shouldBe 2
    rerolled.values(3) shouldBe 4
    
    // Indices 0, 2, 4 should be between 1 and 6 (they were rerolled)
    rerolled.values(0) should (be >= 1 and be <= 6)
    rerolled.values(2) should (be >= 1 and be <= 6)
    rerolled.values(4) should (be >= 1 and be <= 6)
  }

  "MultiPlayerGame" should "track players and current player" in {
    val game = YahtzeeGame.newMultiPlayerGame(List("Alice", "Bob"))
    game.players.length shouldBe 2
    game.currentPlayer.name shouldBe "Alice"
  }

  it should "rotate players correctly" in {
    val game = YahtzeeGame.newMultiPlayerGame(List("Alice", "Bob", "Charlie"))
    val next1 = game.nextPlayer
    next1.currentPlayerIndex shouldBe 1
    next1.currentPlayer.name shouldBe "Bob"
    
    val next2 = next1.nextPlayer
    next2.currentPlayerIndex shouldBe 2
    next2.currentPlayer.name shouldBe "Charlie"
    
    val next3 = next2.nextPlayer
    next3.currentPlayerIndex shouldBe 0
    next3.round shouldBe 2
  }

  it should "be game over when all players are done" in {
    val card = List(
      Ones, Twos, Threes, Fours, Fives, Sixes,
      ThreeOfAKind, FourOfAKind, FullHouse, SmallStraight, LargeStraight, Yahtzee, Chance
    ).foldLeft(ScoreCard())((c, cat) => c.addScore(cat, 10))
    
    val doneState = GameState(scoreCard = card)
    val game = YahtzeeGame.newMultiPlayerGame(List("Alice", "Bob"))
      .copy(players = List(
        Player("Alice", doneState),
        Player("Bob", doneState)
      ))
    
    game.isGameOver shouldBe true
  }

  it should "rank scores correctly" in {
    val state1 = GameState(scoreCard = ScoreCard().addScore(Ones, 50))
    val state2 = GameState(scoreCard = ScoreCard().addScore(Ones, 30))
    
    val game = YahtzeeGame.newMultiPlayerGame(List("Alice", "Bob"))
      .copy(players = List(
        Player("Alice", state2),
        Player("Bob", state1)
      ))
    
    game.scores.head._1 shouldBe "Bob"
    game.scores.head._2 shouldBe 50
  }
}
