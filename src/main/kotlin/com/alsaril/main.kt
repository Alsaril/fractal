package com.alsaril

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*

data class Line(val x1: Double, val y1: Double, val x2: Double, val y2: Double)

fun main() {
    val lines = Collections.synchronizedList(LinkedList<Line>())
    lines.add(Line(-50.0, -50.0, 50.0, -50.0))

    val w = 800
    val h = 800

    val screen = JScreen(w, h)
    screen.setDrawListener { p ->
        p.clear()
        lines.forEach { l ->
            repeat(4) {
                screen.reset()
                screen.rotate(90 * it)
                screen.scale(scale)
                screen.translate(tx, ty)
                p.drawLine(l.x1, l.y1, l.x2, l.y2)
            }
        }
    }

    screen.addMouseListener(object : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            update(lines)
            screen.redraw()
        }
    })

    screen.addKeyListener(object : KeyAdapter() {
        override fun keyTyped(e: KeyEvent) {
            val oldScale = scale
            when (e.keyChar) {
                'q' -> scale *= 1.5
                'e' -> scale /= 1.5
                'w' -> ty += 20
                's' -> ty -= 20
                'a' -> tx += 20
                'd' -> tx -= 20
            }
            val k = scale / oldScale
            tx = w / 2 * (1 - k) + k * tx // долбанутые формулы, на которые пара часов ушла, ахах
            ty  = h / 2 * (1 - k) + k * ty
            screen.redraw()
        }
    })

    screen.show()
}

fun update(lines: MutableList<Line>) {
    val iterator = lines.listIterator()
    while (iterator.hasNext()) {
        val (xf, yf, xt, yt) = iterator.next()
        iterator.remove()

        val dx = (xt - xf) / 3
        val dy = (yt - yf) / 3

        val x1 = xf + dx
        val y1 = yf + dy

        val x2 = xt - dx
        val y2 = yt - dy

        val x3 = x1 + dy * FACTOR
        val y3 = y1 - dx * FACTOR

        val x4 = x2 + dy * FACTOR
        val y4 = y2 - dx * FACTOR

        iterator.add(Line(xf, yf, x1, y1))
        iterator.add(Line(x1, y1, x3, y3))
        iterator.add(Line(x3, y3, x4, y4))
        iterator.add(Line(x4, y4, x2, y2))
        iterator.add(Line(x2, y2, xt, yt))
    }
}

val FACTOR = 5

@Volatile
var scale = 1.0
@Volatile
var tx = 400.0
@Volatile
var ty = 400.0
