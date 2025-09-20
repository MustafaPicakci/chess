package com.chess.piece;

import com.chess.squares.Square;

public abstract class AbstractPiece implements Movable {
    protected String name;
    protected PieceColor pieceColor;
    protected Square currentSquare;

    public AbstractPiece(PieceColor pieceColor) {
        this.pieceColor = pieceColor;
    }

    public String getName() {
        return name;
    }

    public PieceColor getPieceColor() {
        return pieceColor;
    }

    public Square getCurrentSquare() {
        return currentSquare;
    }


    public void setCurrentSquare(Square currentSquare) {
        this.currentSquare = currentSquare;
    }

    protected void moveToSquare(Square destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination square cannot be null");
        }

        Square source = this.currentSquare;
        if (source != null) {
            source.setCurrentPiece(null);
            source.setOccupied(false);
        }

        AbstractPiece occupyingPiece = destination.getCurrentPiece();
        if (occupyingPiece != null && occupyingPiece.getPieceColor() == this.pieceColor) {
            throw new IllegalStateException("Cannot capture a friendly piece");
        }

        if (occupyingPiece != null) {
            occupyingPiece.setCurrentSquare(null);
        }

        destination.setCurrentPiece(this);
        destination.setOccupied(true);
        this.currentSquare = destination;
    }

    @Override
    public String toString() {
        return "AbstractPiece{" +
                "name='" + name + '\'' +
                ", pieceColor=" + pieceColor +
                ", currentSquare=" + currentSquare +
                '}';
    }
}
