package com.lisny.tictactoe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.SparseIntArray
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.util.set
import com.lisny.tictactoe.databinding.ActivityMainBinding


const val DURATION_IN_SECONDS: Long = 60 * 3
const val SECOND_IN_MILLISECONDS: Long = 1000

val horizontalWiningIndexes = listOf(listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8))
val verticalWiningIndexes = listOf(listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8))
val diagonalWiningIndexes = listOf(listOf(0, 4, 8), listOf(2, 4, 6))

val winingIndexes = listOf(verticalWiningIndexes, horizontalWiningIndexes, diagonalWiningIndexes)
    .flatMap { it.toList() }

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val gameArray = SparseIntArray()

    private var isXTurn = true
    private var xWinCount = 0
    private var oWinCount = 0

    private val xName: String by lazy { getString(R.string.x) }
    private val oName: String by lazy { getString(R.string.o) }
    private val nobodyName: String by lazy { getString(R.string.nobody) }

    private val timer = object :
        CountDownTimer(DURATION_IN_SECONDS * SECOND_IN_MILLISECONDS, SECOND_IN_MILLISECONDS) {
        override fun onTick(millisUntilFinished: Long) = applyTicking(millisUntilFinished)
        override fun onFinish() = restartGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        applyWinCount()
        attachEvents()
        setupGrid()
        resetGrid()
    }

    private fun setupGrid() {
        binding.gridView.adapter =
            ArrayAdapter(this, R.layout.list_item, R.id.itemTextView, List(9) { 0 })
    }

    private fun attachEvents() {
        binding.startGameButton.setOnClickListener { startGame() }
        binding.gridView.onItemClickListener =
            AdapterView
                .OnItemClickListener { _, view, position, _ -> applySquareCheck(view, position) }
    }

    private fun applySquareCheck(view: View, position: Int) {
        if (!canCheck(position)) return

        applyUICheck(view)
        applyLogicCheck(position)
        switchTurn()
        applyTurnText()
        applyWinCount()
        handleEndGame()
    }

    private fun canCheck(index: Int) = gameArray[index] == 0 && getWinner() == null

    private fun applyUICheck(view: View) =
        view.setBackgroundResource(if (isXTurn) R.drawable.bg_x_square else R.drawable.bg_o_square)

    private fun applyLogicCheck(index: Int) = (if (isXTurn) 2 else 1).also { gameArray[index] = it }

    private fun switchTurn() = (!isXTurn).also { isXTurn = it }

    private fun checkOneWiningIndexes(indexes: List<Int>) =
        gameArray[indexes[0]] == gameArray[indexes[1]] && gameArray[indexes[1]] == gameArray[indexes[2]]

    private fun getWinner(): PlayerType? {
        var winner: PlayerType? = null

        winingIndexes.forEach {
            if (!(checkOneWiningIndexes(it))) return@forEach
            if (gameArray[it[0]] == 0) return@forEach
            winner = if (gameArray[it[0]] == 2) PlayerType.X else PlayerType.O
        }

        return winner
    }

    private fun startGame() {
        showHideGameView(true)
        applyTurnText()
        timer.start()
    }

    private fun restartGame() {
        resetGrid()
        showHideGameView(false)
        applyAnnouncementText()
        xWinCount = 0
        oWinCount = 0
        isXTurn = true
    }

    private fun resetGrid() {
        gameArray.clear()

        for (i in 0 until binding.gridView.childCount)
            binding.gridView.getChildAt(i).setBackgroundResource(R.drawable.bg_empty_square)
    }

    private fun handleEndGame() {
        val winner = getWinner()
        if (winner == null && !gridIsFull()) return

        binding.instructionTextView.text = getString(
            R.string.player_win_this_game,
            when {
                gridIsFull() && winner == null -> nobodyName
                winner == PlayerType.X -> xName
                else -> oName
            }
        )
        setTimeout(1000) { restartTurn() }
    }

    private fun restartTurn() {
        isXTurn = true
        resetGrid()
        applyTurnText()
    }

    private fun gridIsFull() = gameArray.size() == 9

    private fun showHideGameView(show: Boolean) {
        binding.gameContainer.visibleOrInvisible(show)
        binding.onboardingContainer.visibleOrInvisible(!show)
    }

    private fun applyTicking(millisUntilFinished: Long) {
        binding.timerTextView.text =
            formatSeconds((millisUntilFinished / SECOND_IN_MILLISECONDS).toInt())
    }

    private fun applyTurnText() =
        getString(R.string.it_s_player_s_turn, if (isXTurn) xName else oName)
            .also { binding.instructionTextView.text = it }

    private fun applyWinCount() {
        when (getWinner()) {
            PlayerType.X -> xWinCount++
            PlayerType.O -> oWinCount++
            null -> {}
        }
        binding.xCountTextView.text = getString(R.string.player_score, xName, xWinCount)
        binding.oCountTextView.text = getString(R.string.player_score, oName, oWinCount)
    }

    private fun applyAnnouncementText() {
        val winnerName = getString(
            when {
                xWinCount == oWinCount -> R.string.nobody
                xWinCount > oWinCount -> R.string.x
                else -> R.string.o
            }
        )
        val winCounts = listOf(xWinCount, oWinCount)
        val max = winCounts.maxOrNull() ?: 0
        val min = winCounts.minOrNull() ?: 0

        binding.onboardingTextView.text =
            getString(R.string.winner_announcement, winnerName, max, min)
    }
}
