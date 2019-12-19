package com.alsaril.bool

import java.util.*

fun main() {
    val seed = 108900L
    val count = 500

    val n = 4
    val maxDepth = 10
    val random = Random(seed)

    val expressionGenetics = ExpressionGenetics(n, maxDepth, random)

    val expressions = mutableListOf<Expression>()
    repeat(count) {
        expressions.add(expressionGenetics.random())
    }
    var minDiff: Int
    var i = 0

    do {
        val equations = generateEquations(n, random)
        val rankedSolutions =
            expressions.map { expression -> expression to equations.map { (x, y) -> expression.diff(x, y) }.sum() }
                .sortedBy { it.second }
        minDiff = rankedSolutions.first().second
        println("${i++}: $minDiff")
        println(rankedSolutions.first().first.toString())
        val newSolutions =
            step(rankedSolutions.map { it.first }, expressionGenetics, random)
        expressions.clear()
        expressions.addAll(newSolutions)
    } while (minDiff > 0)
}

fun randomBooleanArray(n: Int, random: Random) = BooleanArray(n).apply {
    repeat(n) {
        set(it, random.nextBoolean())
    }
}

fun generateEquations(n: Int, random: Random): List<Pair<BooleanArray, Boolean>> { // n to n-1
    fun actual(x: BooleanArray): Boolean {
        var result = false
        repeat(n / 2) { i ->
            result = result xor x[2 * i] and x[2 * i + 1]
        }
        return result
    }
    return (1..10000).map { randomBooleanArray(n, random).let { it to actual(it) } }
}

val first = 5
val second = 10
val third = 100
val quad = 100

fun step(
    solutions: List<Expression>,
    expressionGenetics: ExpressionGenetics,
    random: Random
): List<Expression> {
    val result = mutableListOf<Expression>()
    repeat(first) { i ->
        result.add(expressionGenetics.mutate(solutions[i])) // + first
        repeat(first) { j ->
            result.add(expressionGenetics.cross(solutions[i], solutions[j])) // + first^2
        }
        repeat(second) {
            result.add(expressionGenetics.cross(solutions[i], solutions.random(random))) // + first*second
        }
    }
    repeat(third) {
        result.add(expressionGenetics.mutate(solutions.random(random))) // + third
    }
    repeat(quad) {
        result.add(expressionGenetics.random()) // + quad
    }
    val end = Math.sqrt((solutions.size - first - first * first - first * second - third - quad).toDouble()).toInt()
    repeat(end) {
        result.add(expressionGenetics.cross(solutions.random(random), solutions.random(random)))
    }

    require(result.size <= solutions.size)
    while (result.size < solutions.size) {
        result.add(expressionGenetics.random())
    }

    require(result.size == solutions.size)
    return result.map { expressionGenetics.reduce(it) }
}

fun <T> List<T>.random(random: Random) = this[random.nextInt(this.size)]