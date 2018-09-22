package com.github.nyakto.virusgame.logic

import java.util.*

class GameField(
    val width: Int,
    val height: Int
) {
    private val data = Array<Cell>(width * height) { EmptyCell() }

    constructor(size: Int) : this(size, size)

    interface Cell

    class EmptyCell : Cell

    class CrossCell(
        val crossPlayer: Int
    ) : Cell

    class CrossOutCell(
        val crossPlayer: Int,
        val crossOutPlayer: Int
    ) : Cell

    enum class CellAction {
        INVALID,
        CROSS,
        CROSS_OUT
    }

    private fun map(x: Int, y: Int) = y * width + x

    operator fun get(x: Int, y: Int) = data[map(x, y)]

    operator fun set(x: Int, y: Int, value: Cell) {
        data[map(x, y)] = value
    }

    private fun isReachable(player: Int, x: Int, y: Int): Boolean {
        val visited = Array(width * height) { false }
        visited[map(x, y)] = true
        val task = ArrayDeque<Pair<Int, Int>>()

        fun addCheckedTask(x: Int, y: Int) {
            if (x < 0) {
                return
            }
            if (y < 0) {
                return
            }
            if (x >= width) {
                return
            }
            if (y >= height) {
                return
            }
            val index = map(x, y)
            if (visited[index]) {
                return
            }
            task.add(x to y)
            visited[index] = true
        }

        fun addNeighbors(x: Int, y: Int) {
            addCheckedTask(x - 1, y)
            addCheckedTask(x + 1, y)
            addCheckedTask(x, y - 1)
            addCheckedTask(x, y + 1)
        }

        addNeighbors(x, y)

        while (task.isNotEmpty()) {
            val (cx, cy) = task.remove()
            val cell = this[cx, cy]
            when (cell) {
                is CrossCell -> if (cell.crossPlayer == player) {
                    return true
                }
                is CrossOutCell -> if (cell.crossOutPlayer == player) {
                    addNeighbors(cx, cy)
                }
            }
        }

        return false
    }

    fun getCellAction(player: Int, x: Int, y: Int): CellAction {
        val cell = this[x, y]
        return when (cell) {
            is EmptyCell -> if (isReachable(player, x, y)) {
                CellAction.CROSS
            } else {
                CellAction.INVALID
            }
            is CrossCell -> if (cell.crossPlayer != player && isReachable(player, x, y)) {
                CellAction.CROSS_OUT
            } else {
                CellAction.INVALID
            }
            else -> CellAction.INVALID
        }
    }

    companion object {
        fun init(vararg rows: Array<Cell>): GameField {
            val field = GameField(rows.first().size, rows.size)
            rows.forEachIndexed { y, row ->
                row.forEachIndexed { x, cell ->
                    field[x, y] = cell
                }
            }
            return field
        }
    }
}
