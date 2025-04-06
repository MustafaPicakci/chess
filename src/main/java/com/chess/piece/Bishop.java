package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.common.LocationFactory;
import com.chess.squares.Square;

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
        List<Location> moveCandidates = Collections.EMPTY_LIST;
        Map<Location, Square> squareMap = board.getLocationSquareMap();
        Location current = this.getCurrentSquare().getLocation();

        getMoves(moveCandidates, squareMap, current,1, 1);
        getMoves(moveCandidates, squareMap,current, 1, -1);
        getMoves(moveCandidates, squareMap,current, -1, 1);
        getMoves(moveCandidates, squareMap,current, -1, -1);


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

    private void getMoves(List<Location> moveCandiates, Map<Location, Square> squareMap, Location current, int rankOffset, int fileOffset) {
        Location next = LocationFactory.build(current, fileOffset, rankOffset);
        while (squareMap.containsKey(next)) {
            if (squareMap.get(next).isOccupied()) {
                if (squareMap.get(next).getCurrentPiece().getPieceColor().equals(this.pieceColor)) {
                    break;
                }
                moveCandiates.add(next);
                break;
            }
            moveCandiates.add(next);
            next = LocationFactory.build(next, fileOffset, rankOffset);
        }
    }


}
