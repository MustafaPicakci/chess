package com.chess.piece;

import com.chess.board.Board;
import com.chess.common.File;
import com.chess.common.Location;
import com.chess.move.MoveRecord;
import com.chess.squares.Square;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PieceMovementTest {

    private Board board;
    private Map<Location, Square> squareMap;

    @BeforeEach
    void setUp() {
        board = new Board();
        squareMap = board.getLocationSquareMap();
        clearBoard();
    }

    @DisplayName("Fil engel olmadığı sürece tüm köşegen karelerini üretir")
    @Test
    void bishopMovesAlongDiagonalsUntilBlocked() {
        Bishop bishop = new Bishop(PieceColor.LIGHT);
        placePiece(bishop, File.D, 4);

        Set<Location> expected = setOf(
                loc(File.E, 5), loc(File.F, 6), loc(File.G, 7), loc(File.H, 8),
                loc(File.C, 5), loc(File.B, 6), loc(File.A, 7),
                loc(File.E, 3), loc(File.F, 2), loc(File.G, 1),
                loc(File.C, 3), loc(File.B, 2), loc(File.A, 1)
        );

        assertEquals(expected, toSet(bishop.getValidMoves(board)));
    }

    @DisplayName("Kale dost taşta durur ve rakibi yiyerek hamleyi sonlandırır")
    @Test
    void rookStopsBeforeFriendlyPieceAndCapturesEnemy() {
        Rook rook = new Rook(PieceColor.LIGHT);
        placePiece(rook, File.D, 4);

        Knight friendlyKnight = new Knight(PieceColor.LIGHT);
        placePiece(friendlyKnight, File.B, 4);

        Pawn enemyPawn = new Pawn(PieceColor.DARK);
        placePiece(enemyPawn, File.D, 6);

        Set<Location> expected = setOf(
                loc(File.E, 4), loc(File.F, 4), loc(File.G, 4), loc(File.H, 4),
                loc(File.C, 4),
                loc(File.D, 5), loc(File.D, 6),
                loc(File.D, 3), loc(File.D, 2), loc(File.D, 1)
        );

        assertEquals(expected, toSet(rook.getValidMoves(board)));
    }

    @DisplayName("At merkezde sekiz farklı L hamlesi görebilir")
    @Test
    void knightHasEightLMovesWhenCenter() {
        Knight knight = new Knight(PieceColor.LIGHT);
        placePiece(knight, File.D, 4);

        Set<Location> expected = setOf(
                loc(File.C, 6), loc(File.E, 6),
                loc(File.F, 5), loc(File.F, 3),
                loc(File.E, 2), loc(File.C, 2),
                loc(File.B, 3), loc(File.B, 5)
        );

        assertEquals(expected, toSet(knight.getValidMoves(board)));
    }

    @DisplayName("Vezir kale ve fil hareketlerini aynı anda kapsar")
    @Test
    void queenCombinesRookAndBishopMovement() {
        Queen queen = new Queen(PieceColor.LIGHT);
        placePiece(queen, File.D, 4);

        Pawn enemyPawn = new Pawn(PieceColor.DARK);
        placePiece(enemyPawn, File.G, 7);

        Rook friendlyRook = new Rook(PieceColor.LIGHT);
        placePiece(friendlyRook, File.D, 6);

        Set<Location> expected = setOf(
                loc(File.E, 4), loc(File.F, 4), loc(File.G, 4), loc(File.H, 4),
                loc(File.C, 4), loc(File.B, 4), loc(File.A, 4),
                loc(File.D, 5),
                loc(File.D, 3), loc(File.D, 2), loc(File.D, 1),
                loc(File.E, 5), loc(File.F, 6), loc(File.G, 7),
                loc(File.C, 5), loc(File.B, 6), loc(File.A, 7),
                loc(File.E, 3), loc(File.F, 2), loc(File.G, 1),
                loc(File.C, 3), loc(File.B, 2), loc(File.A, 1)
        );

        assertEquals(expected, toSet(queen.getValidMoves(board)));
    }

    @DisplayName("Şah her yönde yalnızca bir kare ilerleyebilir")
    @Test
    void kingMovesOneSquareInAnyDirection() {
        King king = new King(PieceColor.LIGHT);
        placePiece(king, File.D, 4);

        Set<Location> expected = setOf(
                loc(File.C, 4), loc(File.E, 4),
                loc(File.D, 3), loc(File.D, 5),
                loc(File.C, 3), loc(File.C, 5),
                loc(File.E, 3), loc(File.E, 5)
        );

        assertEquals(expected, toSet(king.getValidMoves(board)));
    }

    @DisplayName("Beyaz piyon ilk hamlede tek ya da çift kare ilerler")
    @Test
    void pawnInitialMoveAllowsSingleOrDoubleStepIfClear() {
        Pawn pawn = new Pawn(PieceColor.LIGHT);
        Square start = placePiece(pawn, File.D, 2);

        Set<Location> expectedFirstMove = setOf(loc(File.D, 3), loc(File.D, 4));
        assertEquals(expectedFirstMove, toSet(pawn.getValidMoves(board)));

        Square target = getSquare(File.D, 4);
        assertTrue(pawn.getValidMoves(board).contains(target.getLocation()));
        MoveRecord firstMoveRecord = pawn.makeMove(board, target);
        board.setLastMove(firstMoveRecord);

        Set<Location> expectedAfterMove = setOf(loc(File.D, 5));
        assertEquals(expectedAfterMove, toSet(pawn.getValidMoves(board)));
        assertFalse(start.isOccupied());
        assertTrue(target.isOccupied());
        assertSame(pawn, target.getCurrentPiece());
    }

    @DisplayName("Beyaz piyon önü kapalıysa ilerleyemez ama çaprazda taş yiyebilir")
    @Test
    void pawnCannotAdvanceWhenBlockedButCanCaptureDiagonally() {
        Pawn pawn = new Pawn(PieceColor.LIGHT);
        placePiece(pawn, File.D, 4);

        Pawn blockingPawn = new Pawn(PieceColor.LIGHT);
        placePiece(blockingPawn, File.D, 5);

        Knight enemyKnightLeft = new Knight(PieceColor.DARK);
        placePiece(enemyKnightLeft, File.C, 5);

        Knight enemyKnightRight = new Knight(PieceColor.DARK);
        placePiece(enemyKnightRight, File.E, 5);

        Set<Location> expected = setOf(loc(File.C, 5), loc(File.E, 5));
        assertEquals(expected, toSet(pawn.getValidMoves(board)));
    }

    @DisplayName("moveToSquare kaynağı temizler ve rakip taşı tahtadan kaldırır")
    @Test
    void pieceMakeMoveCapturesEnemyAndClearsSourceSquare() {
        Rook rook = new Rook(PieceColor.LIGHT);
        placePiece(rook, File.D, 4);

        Bishop enemyBishop = new Bishop(PieceColor.DARK);
        Square enemySquare = placePiece(enemyBishop, File.D, 6);

        MoveRecord captureRecord = rook.makeMove(board, enemySquare);
        board.setLastMove(captureRecord);

        assertFalse(getSquare(File.D, 4).isOccupied());
        assertTrue(enemySquare.isOccupied());
        assertSame(rook, enemySquare.getCurrentPiece());
        assertNull(enemyBishop.getCurrentSquare());
    }

    @DisplayName("Fil dost taş tarafından engellendiğinde o yönde ilerleyemez")
    @Test
    void bishopStopsWhenFriendlyBlocking() {
        Bishop bishop = new Bishop(PieceColor.LIGHT);
        placePiece(bishop, File.D, 4);

        Knight friendlyKnight = new Knight(PieceColor.LIGHT);
        placePiece(friendlyKnight, File.E, 5);

        Set<Location> expected = setOf(
                loc(File.C, 5), loc(File.B, 6), loc(File.A, 7),
                loc(File.C, 3), loc(File.B, 2), loc(File.A, 1),
                loc(File.E, 3), loc(File.F, 2), loc(File.G, 1)
        );

        assertEquals(expected, toSet(bishop.getValidMoves(board)));
    }

    @DisplayName("At köşede yalnızca iki kareye gidebilir")
    @Test
    void knightMovesRestrictedNearEdge() {
        Knight knight = new Knight(PieceColor.DARK);
        placePiece(knight, File.A, 1);

        Set<Location> expected = setOf(loc(File.B, 3), loc(File.C, 2));
        assertEquals(expected, toSet(knight.getValidMoves(board)));
    }

    @DisplayName("Siyah piyon aşağı doğru ilerler ve yalnızca rakip taşları çapraz yer")
    @Test
    void blackPawnMovesDownwardsAndCapturesOnlyEnemies() {
        Pawn pawn = new Pawn(PieceColor.DARK);
        placePiece(pawn, File.D, 7);

        Pawn friendlyPawn = new Pawn(PieceColor.DARK);
        placePiece(friendlyPawn, File.E, 6);

        Bishop enemyBishop = new Bishop(PieceColor.LIGHT);
        placePiece(enemyBishop, File.C, 6);

        Set<Location> expectedFirstMove = setOf(loc(File.D, 6), loc(File.D, 5), loc(File.C, 6));
        assertEquals(expectedFirstMove, toSet(pawn.getValidMoves(board)));

        Square captureSquare = getSquare(File.C, 6);
        MoveRecord captureRecord = pawn.makeMove(board, captureSquare);
        board.setLastMove(captureRecord);

        Set<Location> expectedAfterCapture = setOf(loc(File.C, 5));
        assertEquals(expectedAfterCapture, toSet(pawn.getValidMoves(board)));
        assertFalse(getSquare(File.D, 7).isOccupied());
        assertTrue(captureSquare.isOccupied());
        assertNull(enemyBishop.getCurrentSquare());
        assertSame(pawn, captureSquare.getCurrentPiece());
    }

    @DisplayName("Şah dost taşın bulunduğu kareyi hamle listesine eklemez")
    @Test
    void kingExcludesFriendlyOccupiedSquares() {
        King king = new King(PieceColor.DARK);
        placePiece(king, File.D, 4);

        Rook friendlyRook = new Rook(PieceColor.DARK);
        placePiece(friendlyRook, File.D, 5);

        Knight enemyKnight = new Knight(PieceColor.LIGHT);
        placePiece(enemyKnight, File.E, 5);

        Set<Location> expected = setOf(
                loc(File.C, 4), loc(File.E, 4),
                loc(File.C, 3), loc(File.D, 3), loc(File.E, 3),
                loc(File.C, 5), loc(File.E, 5)
        );

        assertEquals(expected, toSet(king.getValidMoves(board)));
        assertFalse(king.getValidMoves(board).contains(loc(File.D, 5)));
    }

    @DisplayName("Şah kısa rok hamlesini gerçekleştirebilir")
    @Test
    void kingCanCastleKingSide() {
        King king = new King(PieceColor.LIGHT);
        Rook rook = new Rook(PieceColor.LIGHT);
        placePiece(king, File.E, 1);
        placePiece(rook, File.H, 1);

        assertTrue(king.getValidMoves(board).contains(loc(File.G, 1)));

        Square kingTarget = getSquare(File.G, 1);
        MoveRecord castleRecord = king.makeMove(board, kingTarget);
        board.setLastMove(castleRecord);

        assertTrue(castleRecord.isCastling());
        assertSame(king, kingTarget.getCurrentPiece());
        assertFalse(getSquare(File.E, 1).isOccupied());
        Square rookDestination = getSquare(File.F, 1);
        assertTrue(rookDestination.isOccupied());
        assertSame(rook, rookDestination.getCurrentPiece());
        assertFalse(getSquare(File.H, 1).isOccupied());
    }

    @DisplayName("Şah uzun rok hamlesini gerçekleştirebilir")
    @Test
    void kingCanCastleQueenSide() {
        King king = new King(PieceColor.DARK);
        Rook rook = new Rook(PieceColor.DARK);
        placePiece(king, File.E, 8);
        placePiece(rook, File.A, 8);

        assertTrue(king.getValidMoves(board).contains(loc(File.C, 8)));

        Square kingTarget = getSquare(File.C, 8);
        MoveRecord castleRecord = king.makeMove(board, kingTarget);
        board.setLastMove(castleRecord);

        assertTrue(castleRecord.isCastling());
        assertSame(king, kingTarget.getCurrentPiece());
        Square rookDestination = getSquare(File.D, 8);
        assertTrue(rookDestination.isOccupied());
        assertSame(rook, rookDestination.getCurrentPiece());
        assertFalse(getSquare(File.A, 8).isOccupied());
    }

    @DisplayName("Beyaz piyon son sıraya ulaştığında otomatik vezire terfi eder")
    @Test
    void pawnPromotionCreatesQueen() {
        Pawn pawn = new Pawn(PieceColor.LIGHT);
        placePiece(pawn, File.A, 7);

        Square promotionSquare = getSquare(File.A, 8);
        MoveRecord promotionRecord = pawn.makeMove(board, promotionSquare);
        board.setLastMove(promotionRecord);

        assertNotNull(promotionRecord.getPromotionPiece());
        assertEquals("Queen", promotionRecord.getPromotionPiece().getName());
        assertSame(promotionRecord.getPromotionPiece(), promotionSquare.getCurrentPiece());
        assertNull(pawn.getCurrentSquare());
        assertEquals(PieceColor.LIGHT, promotionRecord.getPromotionPiece().getPieceColor());
    }

    @DisplayName("Beyaz piyon en passant ile rakip piyonu yiyebilir")
    @Test
    void whitePawnCapturesEnPassant() {
        Pawn whitePawn = new Pawn(PieceColor.LIGHT);
        placePiece(whitePawn, File.E, 5);

        Pawn blackPawn = new Pawn(PieceColor.DARK);
        placePiece(blackPawn, File.D, 5);

        MoveRecord lastMove = MoveRecord.builder(blackPawn, new Location(File.D, 7), new Location(File.D, 5))
                .pawnDoubleAdvance(true)
                .build();
        board.setLastMove(lastMove);

        Location enPassantTarget = new Location(File.D, 6);
        assertTrue(whitePawn.getValidMoves(board).contains(enPassantTarget));

        Square targetSquare = getSquare(File.D, 6);
        MoveRecord captureRecord = whitePawn.makeMove(board, targetSquare);
        board.setLastMove(captureRecord);

        assertTrue(captureRecord.isEnPassantCapture());
        assertEquals(blackPawn, captureRecord.getCapturedPiece());
        assertSame(whitePawn, targetSquare.getCurrentPiece());
        assertFalse(getSquare(File.D, 5).isOccupied());
    }

    private void clearBoard() {
        for (Square square : squareMap.values()) {
            if (square.isOccupied()) {
                square.getCurrentPiece().setCurrentSquare(null);
                square.reset();
            }
        }
        board.setLastMove(null);
    }

    private Square placePiece(AbstractPiece piece, File file, int rank) {
        Square square = getSquare(file, rank);
        piece.setCurrentSquare(square);
        square.setCurrentPiece(piece);
        square.setOccupied(true);
        return square;
    }

    private Square getSquare(File file, int rank) {
        Square square = squareMap.get(loc(file, rank));
        assertNotNull(square, "Requested square does not exist on the board");
        return square;
    }

    private Location loc(File file, int rank) {
        return new Location(file, rank);
    }

    private Set<Location> setOf(Location... locations) {
        return new HashSet<>(Arrays.asList(locations));
    }

    private Set<Location> toSet(List<Location> locations) {
        return new HashSet<>(locations);
    }
}
