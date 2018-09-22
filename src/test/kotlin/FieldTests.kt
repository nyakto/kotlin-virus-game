import com.github.nyakto.virusgame.logic.GameField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FieldTests {
    /**
     * --------------------------
     * | X1 | X1 |    |    |    |
     * |    | X1 | X1 | X1 |    |
     * | X2 | X2 | Z1 | Z1 |    |
     * | X2 |    |    | Z1 | Z1 |
     * | X2 | Z1 | Z1 | Z1 | X2 |
     * --------------------------
     */
    private val field = GameField.init(
        arrayOf(
            GameField.CrossCell(1),
            GameField.CrossCell(1),
            GameField.EmptyCell(),
            GameField.EmptyCell(),
            GameField.EmptyCell()
        ),
        arrayOf(
            GameField.EmptyCell(),
            GameField.CrossCell(1),
            GameField.CrossCell(1),
            GameField.CrossCell(1),
            GameField.EmptyCell()
        ),
        arrayOf(
            GameField.CrossCell(2),
            GameField.CrossCell(2),
            GameField.CrossOutCell(2, 1),
            GameField.CrossOutCell(2, 1),
            GameField.EmptyCell()
        ),
        arrayOf(
            GameField.CrossCell(2),
            GameField.EmptyCell(),
            GameField.EmptyCell(),
            GameField.CrossOutCell(2, 1),
            GameField.CrossOutCell(2, 1)
        ),
        arrayOf(
            GameField.CrossCell(2),
            GameField.CrossOutCell(2, 1),
            GameField.CrossOutCell(2, 1),
            GameField.CrossOutCell(2, 1),
            GameField.CrossCell(2)
        )
    )

    @Test
    fun `возможные ходы игрока 1`() {
        val possibleMoves = mapOf(
            (2 to 0) to GameField.CellAction.CROSS,
            (3 to 0) to GameField.CellAction.CROSS,
            (4 to 0) to GameField.CellAction.CROSS,
            (0 to 1) to GameField.CellAction.CROSS,
            (4 to 1) to GameField.CellAction.CROSS,
            (0 to 2) to GameField.CellAction.CROSS_OUT,
            (0 to 3) to GameField.CellAction.CROSS_OUT,
            (1 to 2) to GameField.CellAction.CROSS_OUT,
            (4 to 2) to GameField.CellAction.CROSS,
            (1 to 3) to GameField.CellAction.CROSS,
            (2 to 3) to GameField.CellAction.CROSS,
            (0 to 4) to GameField.CellAction.CROSS_OUT,
            (4 to 4) to GameField.CellAction.CROSS_OUT
        )
        (0 until field.width).forEach { x ->
            (0 until field.height).forEach { y ->
                val expectedAction = possibleMoves[x to y] ?: GameField.CellAction.INVALID
                val actualAction = field.getCellAction(1, x, y)
                assertEquals(
                    expectedAction,
                    actualAction,
                    { "ошибка в координатах ($x, $y)" }
                )
            }
        }
    }

    @Test
    fun `возможные ходы игрока 2`() {
        val possibleMoves = mapOf(
            (0 to 1) to GameField.CellAction.CROSS,
            (1 to 1) to GameField.CellAction.CROSS_OUT,
            (2 to 1) to GameField.CellAction.CROSS_OUT,
            (1 to 3) to GameField.CellAction.CROSS,
            (2 to 3) to GameField.CellAction.CROSS
        )
        (0 until field.width).forEach { x ->
            (0 until field.height).forEach { y ->
                val expectedAction = possibleMoves[x to y] ?: GameField.CellAction.INVALID
                val actualAction = field.getCellAction(2, x, y)
                assertEquals(
                    expectedAction,
                    actualAction,
                    { "ошибка в координатах ($x, $y)" }
                )
            }
        }
    }
}
