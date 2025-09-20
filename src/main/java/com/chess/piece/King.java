package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.squares.Square;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class King extends AbstractPiece implements Movable {
    public King(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "King";
    }

    @Override
    public List<Location> getValidMoves(Board board) {
        return getValidMoves(board, this.getCurrentSquare());
    }

    @Override
    public List<Location> getValidMoves(Board board, Square square) {
        if (board == null || square == null) {
            return List.of();
        }

        int[][] offsets = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        List<Location> moveCandidates = new ArrayList<>();
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = square.getLocation();

        for (int[] offset : offsets) {
            Location next = LocationFactory.build(current, offset[0], offset[1]);
            if (next == null || !squareMap.containsKey(next)) {
                continue;
            }

            Square nextSquare = squareMap.get(next);
            if (!nextSquare.isOccupied() || nextSquare.getCurrentPiece().getPieceColor() != this.pieceColor) {
                moveCandidates.add(next);
            }
        }

        return moveCandidates;
    }

    @Override
    public void makeMove(Square square) {
        moveToSquare(square);
    }
}
