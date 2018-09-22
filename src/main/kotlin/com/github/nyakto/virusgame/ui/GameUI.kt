package com.github.nyakto.virusgame.ui

import com.github.nyakto.virusgame.logic.GameField
import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class GameUI {
    val field = GameField(10)
    private val width = 480
    private val height = 480
    private val gap = 2f
    private val cellSize = (height - (field.height + 1) * gap) / field.height
    private var windowHandle: Long = 0
    private val backgroundColor = Color(.4f, .4f, 1f)
    private val cellColor = Color(.93f, .93f, .93f)
    private val playerColors = arrayOf(
        Color(1f, 0f, 0f),
        Color(0f, 0f, 1f),
        Color(0f, 1f, 0f)
    )
    private val crossOutPoints = arrayOf(
        0.1f to 0.2f,
        0.7f to 0.1f,
        0.15f to 0.5f,
        0.8f to 0.25f,
        0.4f to 0.7f,
        0.95f to 0.5f,
        0.55f to 0.95f
    )
    private var mouseX = 0.0
    private var mouseY = 0.0
    private var currentPlayer = 0
    private var turns = 2
    private var opponentInitialized = false

    fun init() {
        field[0, 0] = GameField.CrossCell(currentPlayer)
        GLFWErrorCallback.createPrint(System.err).set()
        if (!GLFW.glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
        windowHandle = GLFW.glfwCreateWindow(width, height, "Вирусы!", MemoryUtil.NULL, MemoryUtil.NULL)
        if (windowHandle == MemoryUtil.NULL) {
            throw RuntimeException("Failed to create the GLFW window")
        }

        GLFW.glfwSetKeyCallback(windowHandle) { window, key, _, action, _ ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE) {
                GLFW.glfwSetWindowShouldClose(window, true)
            }
        }
        GLFW.glfwSetCursorPosCallback(windowHandle, { _, x, y ->
            mouseX = x
            mouseY = y
        })
        GLFW.glfwSetMouseButtonCallback(windowHandle, { _, button, action, _ ->
            if (action == GLFW.GLFW_PRESS && button == 0) {
                val x = Math.ceil(mouseX / (cellSize + gap)).toInt() - 1
                val y = Math.ceil(mouseY / (cellSize + gap)).toInt() - 1
                onCellClick(x, y)
            }
        })

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1)
            val pHeight = stack.mallocInt(1)
            GLFW.glfwGetWindowSize(windowHandle, pWidth, pHeight)
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())
            GLFW.glfwSetWindowPos(
                windowHandle,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            )
        }

        GLFW.glfwMakeContextCurrent(windowHandle)
        GL.createCapabilities()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GLFW.glfwSwapInterval(1)

        GLFW.glfwShowWindow(windowHandle)

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, 1.0, -1.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
    }

    fun onCellClick(x: Int, y: Int) {
        val action = field.getCellAction(currentPlayer, x, y)
        when (action) {
            GameField.CellAction.INVALID -> return
            GameField.CellAction.CROSS -> {
                field[x, y] = GameField.CrossCell(currentPlayer)
            }
            GameField.CellAction.CROSS_OUT -> {
                field[x, y] = GameField.CrossOutCell((currentPlayer + 1) % 2, currentPlayer)
            }
        }
        if (--turns <= 0) {
            currentPlayer = (currentPlayer + 1) % 2
            turns = 3
            if (!opponentInitialized) {
                opponentInitialized = true
                turns--
                field[field.width - 1, field.height - 1] = GameField.CrossCell(currentPlayer)
            }
        }
    }

    fun release() {
        Callbacks.glfwFreeCallbacks(windowHandle)
        GLFW.glfwDestroyWindow(windowHandle)
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null).free()
    }

    fun loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            val startRenderTime = System.currentTimeMillis()
            render()
            GLFW.glfwSwapBuffers(windowHandle)
            GLFW.glfwPollEvents()
            val endRenderTime = System.currentTimeMillis()
            val delay = 16 + startRenderTime - endRenderTime
            if (delay > 0) {
                Thread.sleep(delay)
            }
        }
    }

    private fun render() {
        GL11.glClearColor(backgroundColor.red, backgroundColor.green, backgroundColor.blue, 0.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        (0 until field.width).forEach { x ->
            val offsetX = gap + x * (gap + cellSize)
            (0 until field.height).forEach { y ->
                val offsetY = gap + y * (gap + cellSize)
                drawSquare(offsetX, offsetY, cellSize, cellSize, cellColor)
                val cell = field[x, y]
                when (cell) {
                    is GameField.CrossCell -> {
                        drawCross(offsetX, offsetY, cellSize, cellSize, playerColors[cell.crossPlayer])
                    }
                    is GameField.CrossOutCell -> {
                        drawCross(offsetX, offsetY, cellSize, cellSize, playerColors[cell.crossPlayer])
                        drawCrossOut(offsetX, offsetY, cellSize, cellSize, playerColors[cell.crossOutPlayer])
                    }
                }
            }
        }
    }

    private fun drawSquare(x: Float, y: Float, width: Float, height: Float, color: Color) {
        GL11.glColor3f(color.red, color.green, color.blue)
        GL11.glPushMatrix()
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glVertex2f(x, y)
        GL11.glVertex2f(x + width, y)
        GL11.glVertex2f(x + width, y + height)
        GL11.glVertex2f(x, y + height)
        GL11.glEnd()
        GL11.glPopMatrix()
    }

    private fun drawCross(x: Float, y: Float, width: Float, height: Float, color: Color) {
        val horizontalPadding = width / 8
        val verticalPadding = height / 8
        GL11.glLineWidth(8f)
        GL11.glColor3f(color.red, color.green, color.blue)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex3f(x + horizontalPadding, y + verticalPadding, 0f)
        GL11.glVertex3f(x + width - horizontalPadding, y + height - verticalPadding, 0f)
        GL11.glEnd()
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex3f(x + width - horizontalPadding, y + verticalPadding, 0f)
        GL11.glVertex3f(x + horizontalPadding, y + height - verticalPadding, 0f)
        GL11.glEnd()
    }

    private fun drawCrossOut(x: Float, y: Float, width: Float, height: Float, color: Color) {
        GL11.glLineWidth(8f)
        GL11.glColor3f(color.red, color.green, color.blue)
        var prevX = x + crossOutPoints[0].first * width
        var prevY = y + crossOutPoints[0].second * height
        (1 until crossOutPoints.size).forEach { index ->
            val nextX = x + crossOutPoints[index].first * width
            val nextY = y + crossOutPoints[index].second * height
            GL11.glBegin(GL11.GL_LINES)
            GL11.glVertex3f(prevX, prevY, 0f)
            GL11.glVertex3f(nextX, nextY, 0f)
            GL11.glEnd()
            prevX = nextX
            prevY = nextY
        }
    }
}
