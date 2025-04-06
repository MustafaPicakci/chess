package com.chess.runner;

import com.chess.board.Board;
import com.chess.piece.*;

public class Game {
    public static void main(String[] args) {

        Board board = new Board();
        board.printBoard();


    }

    public static void printPiece(Movable piece) {
        System.out.println(piece.getValidMoves(null));

    }
}
