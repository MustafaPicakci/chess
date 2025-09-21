package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.Location;
import com.chess.move.MoveRecord;
import com.chess.squares.Square;

import java.util.List;

public interface Movable {

    List<Location> getValidMoves(Board board);
    List<Location> getValidMoves(Board board, Square square);
    MoveRecord makeMove(Board board, Square square);

}
