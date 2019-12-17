package com.alsaril.bool

import java.lang.StringBuilder

interface Solution {
    fun eval(args: BooleanArray): BooleanArray
    fun diff(args: BooleanArray, sample: BooleanArray): Int
    override fun toString(): String
}

class ExpressionSolution(val expressions: List<Expression>) : Solution {
    override fun eval(args: BooleanArray): BooleanArray {
        return expressions.map { it.eval(args) }.toBooleanArray()
    }

    override fun diff(args: BooleanArray, sample: BooleanArray) =
        (eval(args) zip sample).map { if (it.first xor it.second) 1 else 0 }.first()

    override fun toString(): String {
        val sb = StringBuilder()
        expressions.forEachIndexed {i, e ->
            sb.append("y[$i] = ").append(e.toString()).append("\n")
        }
        return sb.toString()
    }
}

class SolutionGenetics(private val n: Int, private val expressionGenetics: ExpressionGenetics) :
    Genetics<ExpressionSolution> {
    override fun mutate(t: ExpressionSolution) = ExpressionSolution(t.expressions.map(expressionGenetics::mutate))

    override fun cross(a: ExpressionSolution, b: ExpressionSolution) =
        ExpressionSolution((a.expressions zip b.expressions).map { expressionGenetics.cross(it.first, it.second) })

    override fun reduce(t: ExpressionSolution) = ExpressionSolution(t.expressions.map(expressionGenetics::reduce))

    override fun random() = ExpressionSolution((1..n).map { expressionGenetics.random() })
}