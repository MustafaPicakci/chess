package com.chess.runner;

import com.chess.board.Board;
import com.chess.piece.*;

public class Game {
    public static void main(String[] args) {


        PieceColor pieceColor = PieceColor.DARK;
        Movable pawn=new Pawn(pieceColor);

        Game.printPiece(pawn);

    }

    public static void printPiece(Movable piece) {
        System.out.println(piece.getValidMoves(null));

    }
}
