package com.example.bingogame


class BingoGameLogic {


    var minePositions = mutableListOf<Pair<Int, Int>>()
        private set


    fun generateBoard(): Array<IntArray> {
        val numbers = (1..75).shuffled().take(25)
        val board = Array(5) { IntArray(5) }
        var index = 0
        for (row in 0 until 5) {
            for (col in 0 until 5) {
                board[row][col] = numbers[index++]
            }
        }


        val allPositions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 5) {
            for (c in 0 until 5) {
                allPositions.add(Pair(r, c))
            }
        }

        minePositions.clear()
        minePositions.addAll(allPositions.shuffled().take(3))

        return board
    }


    fun checkBingo(board: Array<IntArray>, marked: Array<BooleanArray>): List<Int>? {
        for (i in 0 until 5) {
            if ((0 until 5).all { j -> marked[i][j] }) return (0 until 5).map { j -> board[i][j] }
            if ((0 until 5).all { j -> marked[j][i] }) return (0 until 5).map { j -> board[j][i] }
        }
        if ((0 until 5).all { i -> marked[i][i] }) return (0 until 5).map { i -> board[i][i] }
        if ((0 until 5).all { i -> marked[i][4 - i] }) return (0 until 5).map { i -> board[i][4 - i] }
        return null
    }
}