package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.squares.Square;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Pawn extends AbstractPiece implements Movable {


    private boolean isFirstMove = true;

    public Pawn(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "Pawn";
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

        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = square.getLocation();
        List<Location> moveCandidates = new ArrayList<>();

        int direction = this.pieceColor == PieceColor.LIGHT ? 1 : -1;

        Location singleStep = LocationFactory.build(current, 0, direction);
        if (singleStep != null) {
            Square singleStepSquare = squareMap.get(singleStep);
            if (!singleStepSquare.isOccupied()) {
                moveCandidates.add(singleStep);

                boolean canDoubleStep = square == this.getCurrentSquare() && isFirstMove;
                if (canDoubleStep) {
                    Location doubleStep = LocationFactory.build(current, 0, 2 * direction);
                    if (doubleStep != null) {
                        Square doubleStepSquare = squareMap.get(doubleStep);
                        if (!doubleStepSquare.isOccupied()) {
                            moveCandidates.add(doubleStep);
                        }
                    }
                }
            }
        }

        int[] captureOffsets = {-1, 1};
        for (int fileOffset : captureOffsets) {
            Location capture = LocationFactory.build(current, fileOffset, direction);
            if (capture == null) {
                continue;
            }

            Square captureSquare = squareMap.get(capture);
            if (captureSquare.isOccupied() && captureSquare.getCurrentPiece().getPieceColor() != this.pieceColor) {
                moveCandidates.add(capture);
            }
        }

        return moveCandidates;
    }

    @Override
    public void makeMove(Square square) {
        if (isFirstMove) {
            isFirstMove = false;
        }
        moveToSquare(square);
    }
}
