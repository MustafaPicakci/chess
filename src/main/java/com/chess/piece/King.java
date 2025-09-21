package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.File;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.move.MoveRecord;
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

        if (!this.hasMoved() && square == this.getCurrentSquare()) {
            addCastlingMoves(board, moveCandidates, current);
        }

        return moveCandidates;
    }

    @Override
    public MoveRecord makeMove(Board board, Square square) {
        Location from = this.getCurrentSquare().getLocation();
        boolean isCastling = Math.abs(square.getLocation().getFile().ordinal() - from.getFile().ordinal()) == 2;
        AbstractPiece captured = moveToSquare(board, square);

        if (isCastling) {
            relocateRookForCastling(board, square, from);
        }

        return MoveRecord.builder(this, from, square.getLocation())
                .capturedPiece(captured)
                .castling(isCastling)
                .build();
    }

    private void addCastlingMoves(Board board, List<Location> moveCandidates, Location current) {
        attemptCastling(board, moveCandidates, current,
                File.H,
                new File[]{File.F, File.G},
                File.G);

        attemptCastling(board, moveCandidates, current,
                File.A,
                new File[]{File.D, File.C, File.B},
                File.C);
    }

    private void attemptCastling(Board board, List<Location> moveCandidates, Location kingLocation,
                                 File rookFile, File[] betweenFiles, File destinationFile) {
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location rookLocation = new Location(rookFile, kingLocation.getRank());
        Square rookSquare = squareMap.get(rookLocation);

        if (rookSquare == null || !rookSquare.isOccupied()) {
            return;
        }

        AbstractPiece rookPiece = rookSquare.getCurrentPiece();
        if (!(rookPiece instanceof Rook) || rookPiece.getPieceColor() != this.pieceColor || rookPiece.hasMoved()) {
            return;
        }

        for (File file : betweenFiles) {
            Location betweenLocation = new Location(file, kingLocation.getRank());
            Square betweenSquare = squareMap.get(betweenLocation);
            if (betweenSquare == null || betweenSquare.isOccupied()) {
                return;
            }
        }

        moveCandidates.add(new Location(destinationFile, kingLocation.getRank()));
    }

    private void relocateRookForCastling(Board board, Square kingDestination, Location kingFrom) {
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        File destinationFile = kingDestination.getLocation().getFile();
        int rank = kingDestination.getLocation().getRank();
        boolean kingSide = destinationFile.ordinal() > kingFrom.getFile().ordinal();

        File rookSourceFile = kingSide ? File.H : File.A;
        File rookDestinationFile = kingSide ? File.F : File.D;

        Square rookSourceSquare = squareMap.get(new Location(rookSourceFile, rank));
        Square rookDestinationSquare = squareMap.get(new Location(rookDestinationFile, rank));

        if (rookSourceSquare == null || rookDestinationSquare == null || !rookSourceSquare.isOccupied()) {
            return;
        }

        AbstractPiece rookPiece = rookSourceSquare.getCurrentPiece();
        if (rookPiece instanceof Rook) {
            ((Rook) rookPiece).moveToSquare(board, rookDestinationSquare);
        }
    }
}
