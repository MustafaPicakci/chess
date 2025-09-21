package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.move.MoveRecord;
import com.chess.squares.Square;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Knight extends AbstractPiece implements Movable {
    public Knight(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "Knight";
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

        int[][] offsets = {
                {1, 2}, {2, 1}, {2, -1}, {1, -2},
                {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}
        };

        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = square.getLocation();
        List<Location> moveCandidates = new ArrayList<>();

        for (int[] offset : offsets) {
            Location next = LocationFactory.build(current, offset[0], offset[1]);
            if (next == null) {
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
    public MoveRecord makeMove(Board board, Square square) {
        Location from = this.getCurrentSquare().getLocation();
        AbstractPiece captured = moveToSquare(board, square);
        return MoveRecord.builder(this, from, square.getLocation())
                .capturedPiece(captured)
                .build();
    }
}
