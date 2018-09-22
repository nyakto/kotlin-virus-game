package com.github.nyakto.virusgame

import org.lwjgl.Version
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.NULL


object Application {
    // The window handle
    private var window: Long = 0

    private val view_rotx = 20.0f
    private val view_roty = 30.0f

    private val view_rotz: Float = 0.toFloat()

    private var gear1: Int = 0
    private var gear2: Int = 0
    private var gear3: Int = 0

    private var angle: Float = 0.toFloat()

    private fun init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set()

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw IllegalStateException("Unable to initialize GLFW")

        // Configure GLFW
        glfwDefaultWindowHints() // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE) // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE) // the window will be resizable

        // Create the window
        window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL)
        if (window == NULL)
            throw RuntimeException("Failed to create the GLFW window")

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
        }

        // Get the thread stack and push a new frame
        stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // int*
            val pHeight = stack.mallocInt(1) // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight)

            // Get the resolution of the primary monitor
            val vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor())

            // Center the window
            glfwSetWindowPos(
                window,
                (vidmode.width() - pWidth.get(0)) / 2,
                (vidmode.height() - pHeight.get(0)) / 2
            )
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window)
        GL.createCapabilities()
        // Enable v-sync
        glfwSwapInterval(1)

        // Make the window visible
        glfwShowWindow(window)

        System.err.println("GL_VENDOR: " + glGetString(GL_VENDOR))
        System.err.println("GL_RENDERER: " + glGetString(GL_RENDERER))
        System.err.println("GL_VERSION: " + glGetString(GL_VERSION))

        stackPush().use { s ->
            // setup ogl
            glEnable(GL_CULL_FACE)
            glEnable(GL_LIGHTING)
            glEnable(GL_LIGHT0)
            glEnable(GL_DEPTH_TEST)

            // make the gears
            gear1 = glGenLists(1)
            glNewList(gear1, GL_COMPILE)
            glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, s.floats(0.8f, 0.1f, 0.0f, 1.0f))
            gear(1.0f, 4.0f, 1.0f, 20, 0.7f)
            glEndList()

            gear2 = glGenLists(1)
            glNewList(gear2, GL_COMPILE)
            glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, s.floats(0.0f, 0.8f, 0.2f, 1.0f))
            gear(0.5f, 2.0f, 2.0f, 10, 0.7f)
            glEndList()

            gear3 = glGenLists(1)
            glNewList(gear3, GL_COMPILE)
            glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, s.floats(0.2f, 0.2f, 1.0f, 1.0f))
            gear(1.3f, 2.0f, 0.5f, 10, 0.7f)
            glEndList()
        }

        glEnable(GL_NORMALIZE)
    }

    private fun loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f)

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
//            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT) // clear the framebuffer

            render()

            glfwSwapBuffers(window) // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents()
        }
    }

    private fun render() {
        angle += 2.0f

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        glPushMatrix()
        glRotatef(view_rotx, 1.0f, 0.0f, 0.0f)
        glRotatef(view_roty, 0.0f, 1.0f, 0.0f)
        glRotatef(view_rotz, 0.0f, 0.0f, 1.0f)

        stackPush().use { s -> glLightfv(GL_LIGHT0, GL_POSITION, s.floats(5.0f, 5.0f, 10.0f, 0.0f)) }

        glPushMatrix()
        glTranslatef(-3.0f, -2.0f, 0.0f)
        glRotatef(angle, 0.0f, 0.0f, 1.0f)
        glCallList(gear1)
        glPopMatrix()

        glPushMatrix()
        glTranslatef(3.1f, -2.0f, 0.0f)
        glRotatef(-2.0f * angle - 9.0f, 0.0f, 0.0f, 1.0f)
        glCallList(gear2)
        glPopMatrix()

        glPushMatrix()
        glTranslatef(-3.1f, 4.2f, 0.0f)
        glRotatef(-2.0f * angle - 25.0f, 0.0f, 0.0f, 1.0f)
        glCallList(gear3)
        glPopMatrix()

        glPopMatrix()
    }

    private fun gear(inner_radius: Float, outer_radius: Float, width: Float, teeth: Int, tooth_depth: Float) {
        var angle: Float
        val da: Float

        val r1 = outer_radius - tooth_depth / 2.0f
        val r2 = outer_radius + tooth_depth / 2.0f

        da = 2.0f * Math.PI.toFloat() / teeth.toFloat() / 4.0f

        glShadeModel(GL_FLAT)

        glNormal3f(0.0f, 0.0f, 1.0f)

        var i: Int

        /* draw front face */
        glBegin(GL_QUAD_STRIP)
        i = 0
        while (i <= teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
            if (i < teeth) {
                glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
                glVertex3f(r1 * Math.cos((angle + 3.0f * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3.0f * da).toDouble()).toFloat(),
                    width * 0.5f)
            }
            i++
        }
        glEnd()

        /* draw front sides of teeth */
        glBegin(GL_QUADS)
        i = 0
        while (i < teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + da).toDouble()).toFloat(), r2 * Math.sin((angle + da).toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + 2.0f * da).toDouble()).toFloat(), r2 * Math.sin((angle + 2.0f * da).toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r1 * Math.cos((angle + 3.0f * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3.0f * da).toDouble()).toFloat(), width * 0.5f)
            i++
        }
        glEnd()

        /* draw back face */
        glBegin(GL_QUAD_STRIP)
        i = 0
        while (i <= teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(r1 * Math.cos((angle + 3 * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3 * da).toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)
            i++
        }
        glEnd()

        /* draw back sides of teeth */
        glBegin(GL_QUADS)
        i = 0
        while (i < teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glVertex3f(r1 * Math.cos((angle + 3 * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3 * da).toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + 2 * da).toDouble()).toFloat(), r2 * Math.sin((angle + 2 * da).toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + da).toDouble()).toFloat(), r2 * Math.sin((angle + da).toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)
            i++
        }
        glEnd()

        /* draw outward faces of teeth */
        glBegin(GL_QUAD_STRIP)
        i = 0
        while (i < teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r1 * Math.cos(angle.toDouble()).toFloat(), r1 * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)

            var u = r2 * Math.cos((angle + da).toDouble()).toFloat() - r1 * Math.cos(angle.toDouble()).toFloat()
            var v = r2 * Math.sin((angle + da).toDouble()).toFloat() - r1 * Math.sin(angle.toDouble()).toFloat()

            val len = Math.sqrt((u * u + v * v).toDouble()).toFloat()

            u /= len
            v /= len

            glNormal3f(v, -u, 0.0f)
            glVertex3f(r2 * Math.cos((angle + da).toDouble()).toFloat(), r2 * Math.sin((angle + da).toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + da).toDouble()).toFloat(), r2 * Math.sin((angle + da).toDouble()).toFloat(), -width * 0.5f)
            glNormal3f(Math.cos(angle.toDouble()).toFloat(), Math.sin(angle.toDouble()).toFloat(), 0.0f)
            glVertex3f(r2 * Math.cos((angle + 2 * da).toDouble()).toFloat(), r2 * Math.sin((angle + 2 * da).toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r2 * Math.cos((angle + 2 * da).toDouble()).toFloat(), r2 * Math.sin((angle + 2 * da).toDouble()).toFloat(), -width * 0.5f)

            u = r1 * Math.cos((angle + 3 * da).toDouble()).toFloat() - r2 * Math.cos((angle + 2 * da).toDouble()).toFloat()
            v = r1 * Math.sin((angle + 3 * da).toDouble()).toFloat() - r2 * Math.sin((angle + 2 * da).toDouble()).toFloat()

            glNormal3f(v, -u, 0.0f)
            glVertex3f(r1 * Math.cos((angle + 3 * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3 * da).toDouble()).toFloat(), width * 0.5f)
            glVertex3f(r1 * Math.cos((angle + 3 * da).toDouble()).toFloat(), r1 * Math.sin((angle + 3 * da).toDouble()).toFloat(), -width * 0.5f)
            glNormal3f(Math.cos(angle.toDouble()).toFloat(), Math.sin(angle.toDouble()).toFloat(), 0.0f)
            i++
        }
        glVertex3f(r1 * Math.cos(0.0).toFloat(), r1 * Math.sin(0.0).toFloat(), width * 0.5f)
        glVertex3f(r1 * Math.cos(0.0).toFloat(), r1 * Math.sin(0.0).toFloat(), -width * 0.5f)
        glEnd()

        glShadeModel(GL_SMOOTH)

        /* draw inside radius cylinder */
        glBegin(GL_QUAD_STRIP)
        i = 0
        while (i <= teeth) {
            angle = i.toFloat() * 2.0f * Math.PI.toFloat() / teeth

            glNormal3f(-Math.cos(angle.toDouble()).toFloat(), -Math.sin(angle.toDouble()).toFloat(), 0.0f)
            glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), -width * 0.5f)
            glVertex3f(inner_radius * Math.cos(angle.toDouble()).toFloat(), inner_radius * Math.sin(angle.toDouble()).toFloat(), width * 0.5f)
            i++
        }
        glEnd()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println("Hello LWJGL " + Version.getVersion() + "!")

        init()
        loop()

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window)
        glfwDestroyWindow(window)

        // Terminate GLFW and free the error callback
        glfwTerminate()
        glfwSetErrorCallback(null).free()
    }
}
