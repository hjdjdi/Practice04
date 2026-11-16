package com.example.bingogame

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var roundTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var bingoGrid: GridLayout
    private lateinit var startButton: Button

    private lateinit var dbHelper: DatabaseHelper
    private val gameLogic = BingoGameLogic()

    // Store board numbers and marked status
    private var board = Array(5) { IntArray(5) }
    private var marked = Array(5) { BooleanArray(5) }
    private var buttons = Array(5) { arrayOfNulls<Button>(5) }

    private var round = 1
    private var gameStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind UI components
        usernameEditText = findViewById(R.id.etUsername)
        roundTextView = findViewById(R.id.tvRound)
        statusTextView = findViewById(R.id.tvStatus)
        bingoGrid = findViewById(R.id.gridBingo)
        startButton = findViewById(R.id.btnStart)

        dbHelper = DatabaseHelper(this)
        roundTextView.text = "Round: $round"

        setupGridButtons()

        startButton.setOnClickListener {
            startNewGame()
        }
    }



    private fun setupGridButtons() {
        bingoGrid.removeAllViews()
        bingoGrid.columnCount = 5

        for (row in 0 until 5) {
            for (col in 0 until 5) {
                val button = Button(this)
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 0
                    rowSpec = GridLayout.spec(row, 1f)
                    columnSpec = GridLayout.spec(col, 1f)
                    setMargins(4, 4, 4, 4)
                }
                button.layoutParams = params
                button.textSize = 14f

                button.setOnClickListener {
                    onCellClicked(row, col)
                }

                bingoGrid.addView(button)
                buttons[row][col] = button
            }
        }
    }


    private fun startNewGame() {
        val username = usernameEditText.text.toString().trim()
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        round = 1
        roundTextView.text = "Round: $round"
        statusTextView.text = "Game started, click cells to mark"
        gameStarted = true
        resetBoard()
    }

    private fun resetBoard() {
        board = gameLogic.generateBoard()
        marked = Array(5) { BooleanArray(5) }

        for (row in 0 until 5) {
            for (col in 0 until 5) {
                buttons[row][col]?.apply {
                    text = board[row][col].toString()
                    isEnabled = true
                    setBackgroundColor(Color.LTGRAY)
                    setTextColor(Color.BLACK)
                }
            }
        }
    }


    private fun onCellClicked(row: Int, col: Int) {
        if (!gameStarted || marked[row][col]) return


        if (gameLogic.minePositions.contains(Pair(row, col))) {
            marked[row][col] = true
            buttons[row][col]?.apply {
                setBackgroundColor(Color.RED) // 踩雷变红
                isEnabled = false
            }
            gameStarted = false
            statusTextView.text = "Boom! You hit a mine. Game Over."
            Toast.makeText(this, "Game Over!", Toast.LENGTH_SHORT).show()


            Handler(Looper.getMainLooper()).postDelayed({
                resetBoard()
                gameStarted = true
                statusTextView.text = "New round started"
            }, 1500)
            return
        }


        marked[row][col] = true
        buttons[row][col]?.apply {
            setBackgroundColor(Color.parseColor("#4CAF50")) // Green
            isEnabled = false
        }


        val bingoNumbers = gameLogic.checkBingo(board, marked)
        if (bingoNumbers != null) {
            gameStarted = false

            val username = usernameEditText.text.toString().trim()
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val numbersText = bingoNumbers.joinToString(",")

            // Save to SQLite Database
            val insertId = dbHelper.insertRecord(username, round, numbersText, time)

            statusTextView.text = if (insertId != -1L) {
                "Bingo! Numbers: $numbersText saved to DB"
            } else {
                "Bingo! But failed to save to DB"
            }

            Toast.makeText(this, "Bingo!", Toast.LENGTH_SHORT).show()

            round++
            roundTextView.text = "Round: $round"


            Handler(Looper.getMainLooper()).postDelayed({
                resetBoard()
                gameStarted = true
                statusTextView.text = "New round started"
            }, 1500)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}