package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.squares.Square;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Bishop extends AbstractPiece implements Movable {
    public Bishop(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "Bishop";
    }

    @Override
    public List<Location> getValidMoves(Board board) {
        return getValidMoves(board, this.getCurrentSquare());
    }

    @Override
    public List<Location> getValidMoves(Board board, Square square) {
        if (board == null || square == null) {
            return Collections.emptyList();
        }

        List<Location> moveCandidates = new ArrayList<>();
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = square.getLocation();

        collectMoves(moveCandidates, squareMap, current, 1, 1);
        collectMoves(moveCandidates, squareMap, current, 1, -1);
        collectMoves(moveCandidates, squareMap, current, -1, 1);
        collectMoves(moveCandidates, squareMap, current, -1, -1);

        return moveCandidates;
    }

    @Override
    public void makeMove(Square square) {
        moveToSquare(square);
    }

    private void collectMoves(List<Location> moveCandidates, Map<Location, Square> squareMap, Location current, int fileOffset, int rankOffset) {
        Location next = LocationFactory.build(current, fileOffset, rankOffset);
        while (next != null && squareMap.containsKey(next)) {
            Square nextSquare = squareMap.get(next);
            if (nextSquare.isOccupied()) {
                if (nextSquare.getCurrentPiece().getPieceColor() != this.pieceColor) {
                    moveCandidates.add(next);
                }
                break;
            }
            moveCandidates.add(next);
            next = LocationFactory.build(next, fileOffset, rankOffset);
        }
    }


}
