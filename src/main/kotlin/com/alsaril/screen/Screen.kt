package com.alsaril.screen

import org.apache.commons.math3.linear.MatrixUtils
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.KeyListener
import java.awt.event.MouseListener
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.cos
import kotlin.math.sin

interface Screen {
    fun show()
    fun redraw()
    fun scale(factor: Double)
    fun rotate(angle: Double)
    fun translate(tx: Double, ty: Double)
    fun reset()
    fun addMouseListener(l: MouseListener)
    fun addKeyListener(l: KeyListener)
    fun setDrawListener(draw: (Painter) -> Unit)
}

interface Painter {
    fun clear()
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double)
}

class JScreen(w: Int, h: Int) : Screen {
    private var drawListener: ((Painter) -> Unit)? = null
    @Volatile
    private var state = MatrixUtils.createRealIdentityMatrix(3)

    private val frame = JFrame()
    private val panel: JPanel

    init {
        panel = object : JPanel() {
            override fun paint(g: Graphics) {
                draw(g)
            }
        }

        panel.preferredSize = Dimension(w, h)
        frame.add(panel)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.pack()
    }

    override fun show() {
        frame.isVisible = true
    }

    override fun redraw() {
        panel.repaint()
    }

    override fun scale(factor: Double) {
        val scaleMatrix = MatrixUtils.createRealDiagonalMatrix(doubleArrayOf(factor, factor, 1.0))
        state = scaleMatrix.multiply(state)
    }

    @Synchronized
    override fun rotate(angle: Double) {
        val sin = sin(angle / 180.0 * Math.PI)
        val cos = cos(angle / 180.0 * Math.PI)
        val rotateMatrix = MatrixUtils.createRealMatrix(
            arrayOf(
                doubleArrayOf(cos, -sin, 0.0),
                doubleArrayOf(sin, cos, 0.0),
                doubleArrayOf(0.0, 0.0, 1.0)
            )
        )
        state = rotateMatrix.multiply(state)
    }

    @Synchronized
    override fun translate(tx: Double, ty: Double) {
        val translateMatrix = MatrixUtils.createRealIdentityMatrix(3)
        translateMatrix.setEntry(0, 2, tx)
        translateMatrix.setEntry(1, 2, ty)
        translateMatrix.setEntry(2, 2, 1.0)
        state = translateMatrix.multiply(state)
    }

    @Synchronized
    override fun reset() {
        state = MatrixUtils.createRealIdentityMatrix(3)
    }

    override fun addMouseListener(l: MouseListener) {
        frame.addMouseListener(l)
    }

    override fun addKeyListener(l: KeyListener) {
        frame.addKeyListener(l)
    }

    override fun setDrawListener(draw: (Painter) -> Unit) {
        this.drawListener = draw
    }

    @Synchronized
    private fun draw(g: Graphics) {
        painter.setGraphics(g)
        drawListener?.invoke(painter)
    }

    private val painter = object : Painter {
        @Volatile
        private var graphics: Graphics? = null

        override fun clear() {
            graphics?.clearRect(0, 0, w, h)
        }

        override fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double) {
            val f = MatrixUtils.createColumnRealMatrix(doubleArrayOf(x1, y1, 1.0))
            val s = MatrixUtils.createColumnRealMatrix(doubleArrayOf(x2, y2, 1.0))

            val f_ = state.multiply(f)
            val s_ = state.multiply(s)

            val realX1 = f_.getEntry(0, 0).toInt()
            val realY1 = f_.getEntry(1, 0).toInt()
            val realX2 = s_.getEntry(0, 0).toInt()
            val realY2 = s_.getEntry(1, 0).toInt()

            //if (contains(realX1, realY1) || contains(realX2, realY2)) {
            graphics?.drawLine(realX1, realY1, realX2, realY2)
            // }
        }

        fun setGraphics(g: Graphics) {
            this.graphics = g
        }

        private fun contains(x: Int, y: Int) = x in 0 until w && y in 0 until h

    }
}
