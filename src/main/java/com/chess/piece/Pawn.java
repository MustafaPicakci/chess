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
        int startingRank = this.pieceColor == PieceColor.LIGHT ? 2 : 7;

        Location singleStep = LocationFactory.build(current, 0, direction);
        if (singleStep != null) {
            Square singleStepSquare = squareMap.get(singleStep);
            if (!singleStepSquare.isOccupied()) {
                moveCandidates.add(singleStep);

                boolean canDoubleStep = square == this.getCurrentSquare() && current.getRank() == startingRank && !this.hasMoved();
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

        MoveRecord lastMove = board.getLastMove();
        if (lastMove != null && lastMove.getPiece() instanceof Pawn && lastMove.isPawnDoubleAdvance()) {
            Location lastTo = lastMove.getTo();
            if (lastMove.getPiece().getPieceColor() != this.pieceColor && lastTo.getRank().equals(current.getRank())) {
                int fileDifference = Math.abs(lastTo.getFile().ordinal() - current.getFile().ordinal());
                if (fileDifference == 1) {
                    int targetRank = current.getRank() + direction;
                    if (targetRank >= 1 && targetRank <= 8) {
                        Location enPassantTarget = new Location(lastTo.getFile(), targetRank);
                        Square enPassantSquare = squareMap.get(enPassantTarget);
                        if (enPassantSquare != null && !enPassantSquare.isOccupied()) {
                            moveCandidates.add(enPassantTarget);
                        }
                    }
                }
            }
        }

        return moveCandidates;
    }

    @Override
    public MoveRecord makeMove(Board board, Square square) {
        Location from = this.getCurrentSquare().getLocation();
        Location to = square.getLocation();

        boolean doubleAdvance = Math.abs(to.getRank() - from.getRank()) == 2;
        boolean enPassantCapture = false;
        AbstractPiece capturedPiece = square.getCurrentPiece();

        if (!square.isOccupied() && !from.getFile().equals(to.getFile())) {
            MoveRecord lastMove = board.getLastMove();
            if (lastMove != null && lastMove.getPiece() instanceof Pawn && lastMove.isPawnDoubleAdvance() &&
                    lastMove.getPiece().getPieceColor() != this.pieceColor &&
                    lastMove.getTo().getFile().equals(to.getFile()) &&
                    lastMove.getTo().getRank().equals(from.getRank())) {

                Square capturedSquare = board.getLocationSquareMap().get(lastMove.getTo());
                if (capturedSquare != null && capturedSquare.isOccupied()) {
                    capturedPiece = capturedSquare.getCurrentPiece();
                    capturedSquare.setCurrentPiece(null);
                    capturedSquare.setOccupied(false);
                    if (capturedPiece != null) {
                        capturedPiece.setCurrentSquare(null);
                    }
                    enPassantCapture = true;
                }
            }
        }

        AbstractPiece moveCapture = moveToSquare(board, square);
        if (moveCapture != null) {
            capturedPiece = moveCapture;
        }

        isFirstMove = false;

        AbstractPiece promotionPiece = null;
        boolean promotionRankReached = (this.pieceColor == PieceColor.LIGHT && to.getRank() == 8)
                || (this.pieceColor == PieceColor.DARK && to.getRank() == 1);

        if (promotionRankReached) {
            Queen promotedPiece = new Queen(this.pieceColor);
            promotedPiece.setCurrentSquare(square);
            promotedPiece.hasMoved = true;
            square.setCurrentPiece(promotedPiece);
            promotionPiece = promotedPiece;
            this.setCurrentSquare(null);
        }

        MoveRecord.Builder builder = MoveRecord.builder(this, from, to)
                .capturedPiece(capturedPiece)
                .pawnDoubleAdvance(doubleAdvance)
                .enPassantCapture(enPassantCapture)
                .promotionPiece(promotionPiece);

        if (promotionPiece != null) {
            builder.movingPiece(promotionPiece);
        }

        return builder.build();
    }
}
