package com.github.nyakto.virusgame

import com.github.nyakto.virusgame.ui.GameUI


object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        val gameWindow = GameUI()
        gameWindow.init()
        gameWindow.loop()
        gameWindow.release()
    }
}
