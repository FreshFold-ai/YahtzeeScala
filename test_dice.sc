// Quick test of dice logic
val dice = List(5, 1, 4, 4, 1)
println(s"Initial dice: $dice")
println(s"Indices:      0, 1, 2, 3, 4")
println()

val keepIndices = Set(2, 3)
println(s"Keeping indices: $keepIndices")
println(s"Values to keep: ${dice(2)}, ${dice(3)}")
println()

val result = dice.zipWithIndex.map { case (v, i) => 
  if (keepIndices.contains(i)) v else scala.util.Random.nextInt(6) + 1
}

println(s"After keepAndReroll: $result")
println(s"Kept values at 2,3: ${result(2)}, ${result(3)}")
println(s"Should be same as:   ${dice(2)}, ${dice(3)}")
