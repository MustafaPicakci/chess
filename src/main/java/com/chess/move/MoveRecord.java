package com.chess.move;

import com.chess.common.Location;
import com.chess.piece.AbstractPiece;

public class MoveRecord {
    private AbstractPiece piece;
    private final Location from;
    private final Location to;
    private final AbstractPiece capturedPiece;
    private final boolean pawnDoubleAdvance;
    private final boolean enPassantCapture;
    private final boolean castling;
    private final AbstractPiece promotionPiece;

    private MoveRecord(Builder builder) {
        this.piece = builder.piece;
        this.from = builder.from;
        this.to = builder.to;
        this.capturedPiece = builder.capturedPiece;
        this.pawnDoubleAdvance = builder.pawnDoubleAdvance;
        this.enPassantCapture = builder.enPassantCapture;
        this.castling = builder.castling;
        this.promotionPiece = builder.promotionPiece;
    }

    public AbstractPiece getPiece() {
        return piece;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public AbstractPiece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isPawnDoubleAdvance() {
        return pawnDoubleAdvance;
    }

    public boolean isEnPassantCapture() {
        return enPassantCapture;
    }

    public boolean isCastling() {
        return castling;
    }

    public AbstractPiece getPromotionPiece() {
        return promotionPiece;
    }

    public void setPiece(AbstractPiece piece) {
        this.piece = piece;
    }

    public static Builder builder(AbstractPiece piece, Location from, Location to) {
        return new Builder(piece, from, to);
    }

    public static class Builder {
        private AbstractPiece piece;
        private final Location from;
        private final Location to;
        private AbstractPiece capturedPiece;
        private boolean pawnDoubleAdvance;
        private boolean enPassantCapture;
        private boolean castling;
        private AbstractPiece promotionPiece;

        private Builder(AbstractPiece piece, Location from, Location to) {
            this.piece = piece;
            this.from = from;
            this.to = to;
        }

        public Builder capturedPiece(AbstractPiece capturedPiece) {
            this.capturedPiece = capturedPiece;
            return this;
        }

        public Builder pawnDoubleAdvance(boolean pawnDoubleAdvance) {
            this.pawnDoubleAdvance = pawnDoubleAdvance;
            return this;
        }

        public Builder enPassantCapture(boolean enPassantCapture) {
            this.enPassantCapture = enPassantCapture;
            return this;
        }

        public Builder castling(boolean castling) {
            this.castling = castling;
            return this;
        }

        public Builder promotionPiece(AbstractPiece promotionPiece) {
            this.promotionPiece = promotionPiece;
            return this;
        }

        public Builder movingPiece(AbstractPiece piece) {
            this.piece = piece;
            return this;
        }

        public MoveRecord build() {
            return new MoveRecord(this);
        }
    }
}
