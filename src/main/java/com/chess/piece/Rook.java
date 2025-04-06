package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.squares.Square;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Rook extends AbstractPiece implements Movable {
    public Rook(PieceColor pieceColor) {
        super(pieceColor);
        this.name = "Rook";
    }

    @Override
    public List<Location> getValidMoves(Board board) {
        List<Location> moveCandidates = Collections.EMPTY_LIST;
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = this.getCurrentSquare().getLocation();

        getFileCandidates(moveCandidates, squareMap, current, -1);
        getFileCandidates(moveCandidates, squareMap, current, 1);
        getRankCandidates(moveCandidates, squareMap, current, -1);
        getRankCandidates(moveCandidates, squareMap, current, 1);

        return moveCandidates;
    }

    @Override
    public List<Location> getValidMoves(Board board, Square square) {
        return null;
    }

    @Override
    public void makeMove(Square square) {
        System.out.println(this.getName() + "-> makeMove()");
    }


    private void getRankCandidates(List<Location> moveCandiates, Map<Location, Square> squareMap, Location current, int offset) {
        Location next = LocationFactory.build(current, current.getFile().ordinal(), offset);
        while (squareMap.containsKey(next)) {
            if (squareMap.get(next).isOccupied()) {
                if (squareMap.get(next).getCurrentPiece().getPieceColor().equals(this.pieceColor)) {
                    break;
                }
                moveCandiates.add(next);
                break;
            }
            moveCandiates.add(next);
            next = LocationFactory.build(next, next.getFile().ordinal(), offset);
        }
    }

    private void getFileCandidates(List<Location> moveCandiates, Map<Location, Square> squareMap, Location current, int offset) {
        Location next = LocationFactory.build(current, offset, 0);
        while (squareMap.containsKey(next)) {
            if (squareMap.get(next).isOccupied()) {
                if (squareMap.get(next).getCurrentPiece().getPieceColor().equals(this.pieceColor)) {
                    break;
                }
                moveCandiates.add(next);
                break;
            }
            moveCandiates.add(next);
            next = LocationFactory.build(next, offset, 0);
        }
    }


}
