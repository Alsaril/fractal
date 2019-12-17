package com.alsaril.bool

import java.util.*

fun main() {
    val seed = 100L
    val count = 1000

    val n = 6
    val maxDepth = 20
    val random = Random(seed)

    val expressionGenetics = ExpressionGenetics(n - 1, maxDepth, random)
    val solutionGenetics = SolutionGenetics(1, expressionGenetics)

    val solutions = mutableListOf<ExpressionSolution>()
    repeat(count) {
        solutions.add(solutionGenetics.random())
    }
    var minDiff: Int
    var i = 0

    do {
        val equations = generateEquations(n, random)
        val rankedSolutions =
            solutions.map { solution -> solution to equations.map { (x, y) -> solution.diff(x, y) }.sum() }
                .sortedBy { it.second }
        minDiff = rankedSolutions.first().second
        println("${i++}: $minDiff")
        println(rankedSolutions.first().first.toString())
        val newSolutions =
            step(rankedSolutions.map { it.first }, solutionGenetics, random)
        solutions.clear()
        solutions.addAll(newSolutions)
    } while (minDiff > 0)
}

fun randomBooleanArray(n: Int, random: Random) = BooleanArray(n).apply {
    repeat(n) {
        set(it, random.nextBoolean())
    }
}

fun generateEquations(n: Int, random: Random): List<Pair<BooleanArray, BooleanArray>> { // n to n-1
    fun actual(x: BooleanArray): BooleanArray {
        return BooleanArray(1).apply {
            set(0, (x[0] and (x[1] xor true xor x[3])) xor (x[2]))
        }
    }
    return (1..10000).map { randomBooleanArray(n, random).let { it to actual(it) } }
}

val first = 5
val second = 10
val third = 100
val quad = 100

fun step(
    solutions: List<ExpressionSolution>,
    solutionGenetics: SolutionGenetics,
    random: Random
): List<ExpressionSolution> {
    val result = mutableListOf<ExpressionSolution>()
    repeat(first) { i ->
        result.add(solutionGenetics.mutate(solutions[i])) // + first
        repeat(first) { j ->
            result.add(solutionGenetics.cross(solutions[i], solutions[j])) // + first^2
        }
        repeat(second) {
            result.add(solutionGenetics.cross(solutions[i], solutions.random(random))) // + first*second
        }
    }
    repeat(third) {
        result.add(solutionGenetics.mutate(solutions.random(random))) // + third
    }
    repeat(quad) {
        result.add(solutionGenetics.random()) // + quad
    }
    val end = Math.sqrt((solutions.size - first - first * first - first * second - third - quad).toDouble()).toInt()
    repeat(end) {
        result.add(solutionGenetics.cross(solutions.random(random), solutions.random(random)))
    }

    require(result.size <= solutions.size)
    while (result.size < solutions.size) {
        result.add(solutionGenetics.random())
    }

    require(result.size == solutions.size)
    return result.map { solutionGenetics.reduce(it) }
}

fun <T> List<T>.random(random: Random) = this[random.nextInt(this.size)]