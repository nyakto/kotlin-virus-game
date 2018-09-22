package com.github.nyakto.virusgame.ui

import org.lwjgl.glfw.Callbacks
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class GameWindow(
    val width: Int,
    val height: Int
) {
    private var windowHandle: Long = 0
    private val backgroundColor = Color(0f, 0f, 0f)
    private val fieldColor = Color(1f, 1f, 1f)

    fun init() {
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
        GLFW.glfwSwapInterval(1)

        GLFW.glfwShowWindow(windowHandle)

        GL11.glMatrixMode(GL11.GL_PROJECTION)
        GL11.glLoadIdentity()
        GL11.glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, 1.0, -1.0)
        GL11.glMatrixMode(GL11.GL_MODELVIEW)
    }

    fun release() {
        Callbacks.glfwFreeCallbacks(windowHandle)
        GLFW.glfwDestroyWindow(windowHandle)
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null).free()
    }

    fun loop() {
        while (!GLFW.glfwWindowShouldClose(windowHandle)) {
            render()
            GLFW.glfwSwapBuffers(windowHandle)
            GLFW.glfwPollEvents()
        }
    }

    private fun render() {
        GL11.glClearColor(backgroundColor.red, backgroundColor.green, backgroundColor.blue, 0.0f)
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        drawSquare(10f, 10f, 40f, 40f, fieldColor)
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
}
