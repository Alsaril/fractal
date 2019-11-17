package com.alsaril

import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

data class Line(
    var x1: Double,
    var y1: Double,
    var x2: Double,
    var y2: Double,
    val parent: Line?,
    val index: Int,
    var version: Int
)

fun main() {
    val lines = Collections.synchronizedList(LinkedList<Line>())
    val version = AtomicInteger()
    val root = Line(-50.0, -50.0, 50.0, -50.0, null, 0, 0)
    lines.add(root)

    val w = 800
    val h = 800

    val screen = JScreen(w, h)
    screen.setDrawListener { p ->
        p.clear()
        lines.forEach { l ->
            if (l.version != version.get()) {
                relocate(l, version.get())
            }
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
            update(lines, version.get())
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
                'z' -> {
                    FACTOR += 0.1
                    version.incrementAndGet()
                }
                'x' -> {
                    FACTOR -= 0.1
                    version.incrementAndGet()
                }
            }
            val k = scale / oldScale
            tx = w / 2 * (1 - k) + k * tx // долбанутые формулы, на которые пара часов ушла, ахах
            ty = h / 2 * (1 - k) + k * ty
            screen.redraw()
        }
    })

    screen.show()
}

fun relocate(line: Line, version: Int) {
    if (line.version == version || line.parent == null) return
    relocate(line.parent, version)
    val (xf, yf, xt, yt) = line.parent

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

    when (line.index) {
        0 -> {
            line.x1 = xf
            line.y1 = yf
            line.x2 = x1
            line.y2 = y1
        }
        1 -> {
            line.x1 = x1
            line.y1 = y1
            line.x2 = x3
            line.y2 = y3
        }
        2 -> {
            line.x1 = x3
            line.y1 = y3
            line.x2 = x4
            line.y2 = y4
        }
        3 -> {
            line.x1 = x4
            line.y1 = y4
            line.x2 = x2
            line.y2 = y2
        }
        4 -> {
            line.x1 = x2
            line.y1 = y2
            line.x2 = xt
            line.y2 = yt
        }
    }
    line.version++
}

fun update(lines: MutableList<Line>, version: Int) {
    val iterator = lines.listIterator()
    while (iterator.hasNext()) {
        val line = iterator.next()
        val (xf, yf, xt, yt) = line
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

        iterator.add(Line(xf, yf, x1, y1, line, 0, version))
        iterator.add(Line(x1, y1, x3, y3, line, 1, version))
        iterator.add(Line(x3, y3, x4, y4, line, 2, version))
        iterator.add(Line(x4, y4, x2, y2, line, 3, version))
        iterator.add(Line(x2, y2, xt, yt, line, 4, version))
    }
}

@Volatile
var FACTOR = 5.0
@Volatile
var scale = 1.0
@Volatile
var tx = 400.0
@Volatile
var ty = 400.0
