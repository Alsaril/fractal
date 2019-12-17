package com.alsaril.bool

import com.alsaril.bool.OperatorExpression.Companion.isLeaf
import com.alsaril.bool.OperatorExpression.Companion.isNode
import java.util.*

interface Expression {
    fun eval(args: BooleanArray): Boolean
    override fun toString(): String
}

object TrueExpression : Expression {
    override fun eval(args: BooleanArray) = true
    override fun toString() = "1"
}

class VariableExpression(private val index: Int) : Expression {
    override fun eval(args: BooleanArray) = args[index]
    override fun toString() = "x[$index]"
}

class OperatorExpression(
    val type: OperatorType,
    val a: Expression,
    val b: Expression
) : Expression {
    override fun eval(args: BooleanArray): Boolean {
        val aValue = a.eval(args)
        val bValue = b.eval(args)
        return when (type) {
            OperatorType.AND -> aValue and bValue
            OperatorType.XOR -> aValue xor bValue
        }
    }

    override fun toString(): String {
        val symbol = when (type) {
            OperatorType.AND -> "&"
            OperatorType.XOR -> "^"
        }
        val aS = if (isLeaf(a)) a.toString() else "($a)"
        val bS = if (isLeaf(b)) b.toString() else "($b)"
        return "$aS $symbol $bS"
    }

    enum class OperatorType {
        AND, XOR;

        companion object {
            fun random(random: Random) = values().let { it[random.nextInt(it.size)] }
        }
    }

    companion object {
         fun isLeaf(expression: Expression) = expression is TrueExpression || expression is VariableExpression
         fun isNode(expression: Expression) = expression is OperatorExpression
    }
}

class ExpressionGenetics(private val n: Int, private val maxDepth: Int, private val random: Random) :
    Genetics<Expression> {

    override fun mutate(t: Expression) = mutateExpression(t, n, maxDepth, random).apply {
        //println("was    $t\nbecame $this")
    }
    override fun cross(a: Expression, b: Expression) = crossExpressions(a, b, random)
    override fun reduce(t: Expression) = reduceExpression(t, n, maxDepth, random)
    override fun random() = randomExpression(n, maxDepth, random)

    companion object {
        private fun mutateExpression(
            expression: Expression,
            variables: Int,
            maxDepth: Int,
            random: Random
        ): Expression {
            // go to random subtree and replace it with generated
            if (random.nextDouble() < 0.001) return expression
            when (expression) {
                is TrueExpression -> return randomExpression(variables, maxDepth, random)
                is VariableExpression -> return randomExpression(variables, maxDepth, random)
                is OperatorExpression -> {
                    val r = random.nextDouble()
                    return when {
                        r < 0.3 -> OperatorExpression(
                            expression.type,
                            randomExpression(variables, maxDepth - 1, random),
                            expression.b
                        )
                        r < 0.6 -> OperatorExpression(
                            expression.type,
                            expression.a,
                            randomExpression(variables, maxDepth - 1, random)
                        )
                        else -> randomExpression(variables, maxDepth, random)
                    }
                }
                else -> throw IllegalStateException()
            }
        }

        private fun crossExpressions(a: Expression, b: Expression, random: Random): Expression {
            if (isLeaf(a) || isLeaf(b)) return b
            val nodeA = a as OperatorExpression
            val nodeB = b as OperatorExpression
            val r = random.nextDouble()
            return when {
                r < 0.3 -> OperatorExpression(nodeA.type, crossExpressions(nodeA.a, nodeB.a, random), nodeA.b)
                r < 0.6 -> OperatorExpression(nodeA.type, nodeA.a, crossExpressions(nodeA.b, nodeB.b, random))
                else -> b
            }
        }

        private fun reduceExpression(
            expression: Expression,
            variables: Int,
            maxDepth: Int,
            random: Random
        ): Expression {
            if (maxDepth == 0) {
                return if (random.nextBoolean()) TrueExpression else VariableExpression(random.nextInt(variables))
            }
            when {
                isLeaf(expression) -> return expression
                isNode(expression) -> {
                    expression as OperatorExpression
                    if (expression.type == OperatorExpression.OperatorType.AND) {
                        if (expression.a is TrueExpression) return reduceExpression(
                            expression.b,
                            variables,
                            maxDepth - 1,
                            random
                        )
                        if (expression.b is TrueExpression) return reduceExpression(
                            expression.a,
                            variables,
                            maxDepth - 1,
                            random
                        )
                    }
                    return OperatorExpression(
                        expression.type,
                        reduceExpression(expression.a, variables, maxDepth - 1, random),
                        reduceExpression(expression.b, variables, maxDepth - 1, random)
                    )
                }
                else -> throw IllegalStateException()
            }
        }

        private fun randomExpression(variables: Int, maxDepth: Int, random: Random): Expression {
            if (maxDepth == 0) return TrueExpression
            return when (random.nextInt(3)) {
                0 -> TrueExpression
                1 -> VariableExpression(random.nextInt(variables))
                2 -> OperatorExpression(
                    OperatorExpression.OperatorType.random(random),
                    randomExpression(variables, maxDepth - 1, random),
                    randomExpression(variables, maxDepth - 1, random)
                )
                else -> throw IllegalStateException()
            }
        }
    }
}