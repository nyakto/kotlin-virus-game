package com.github.nyakto.virusgame

import com.github.nyakto.virusgame.ui.GameWindow


object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        val gameWindow = GameWindow(640, 480)
        gameWindow.init()
        gameWindow.loop()
        gameWindow.release()
    }
}
