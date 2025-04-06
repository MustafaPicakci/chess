package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.squares.Square;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class King extends AbstractPiece implements Movable {
    private Movable bishop;
    private Movable rook;

    public King(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "King";
    }

    public King(PieceColor pieceColor, Movable bishop, Movable rook) {
        super(pieceColor);
        this.name = "King";
        this.bishop = bishop;
        this.rook = rook;
    }

    @Override
    public List<Location> getValidMoves(Board board) {
        List<Location> moveCandidates = Collections.emptyList();

        moveCandidates.addAll(bishop.getValidMoves(board, this.getCurrentSquare()));
        moveCandidates.addAll(rook.getValidMoves(board, this.getCurrentSquare()));

        Location current = this.currentSquare.getLocation();


        return moveCandidates.stream().filter(candiate -> (
                Math.abs(candiate.getFile().ordinal() - current.getFile().ordinal()) == 1 &&
                        Math.abs(candiate.getRank() - current.getRank()) == 1)).collect(Collectors.toList());

    }

    @Override
    public List<Location> getValidMoves(Board board, Square square) {
        return null;
    }

    @Override
    public void makeMove(Square square) {
        System.out.println(this.getName() + "-> makeMove()");
    }
}
