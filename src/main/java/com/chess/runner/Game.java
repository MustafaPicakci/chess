package com.chess.runner;

import com.chess.board.Board;
import com.chess.common.File;
import com.chess.common.Location;
import com.chess.piece.*;
import com.chess.squares.Square;

import java.util.Scanner;

public class Game {
    public static void main(String[] args) {

        Board board = new Board();
        board.printBoard();

        Scanner scanner = new Scanner(System.in);
        PieceColor currentTurn = PieceColor.LIGHT;

        while (true) {
            String line = scanner.nextLine();
            if (line == null) {
                continue;
            }

            line = line.trim();
            if (line.isEmpty()) {
                System.out.println("Lütfen bir hamle girin (örn. E2-E4) ya da 'quit' yazın.");
                continue;
            }

            if ("quit".equalsIgnoreCase(line)) {
                System.out.println("Oyun sonlandırıldı.");
                break;
            }

            String[] fromTo = line.split("-");
            if (fromTo.length != 2 || fromTo[0].length() < 2 || fromTo[1].length() < 2) {
                System.out.println("Geçersiz hamle formatı. Örnek kullanım: E2-E4");
                continue;
            }

            if (!Character.isLetter(fromTo[0].charAt(0)) || !Character.isLetter(fromTo[1].charAt(0)) ||
                    !Character.isDigit(fromTo[0].charAt(1)) || !Character.isDigit(fromTo[1].charAt(1))) {
                System.out.println("Geçersiz hamle formatı. Örnek kullanım: E2-E4");
                continue;
            }

            File fromFile = File.valueOf(String.valueOf(Character.toUpperCase(fromTo[0].charAt(0))));
            File toFile = File.valueOf(String.valueOf(Character.toUpperCase(fromTo[1].charAt(0))));

            int fromRank = Character.getNumericValue(fromTo[0].charAt(1));
            int toRank = Character.getNumericValue(fromTo[1].charAt(1));

            if (fromRank < 1 || fromRank > 8 || toRank < 1 || toRank > 8) {
                System.out.println("Satranç tahtasında 1 ile 8 arasında rank kullanılabilir.");
                continue;
            }

            Square fromSq = board.getLocationSquareMap().get(new Location(fromFile, fromRank));
            Square toSq = board.getLocationSquareMap().get(new Location(toFile, toRank));

            if (fromSq == null) {
                System.out.println("Başlangıç karesi tahtada bulunamadı.");
                continue;
            }

            if (!fromSq.isOccupied()) {
                System.out.println("Seçilen başlangıç karesinde taş yok.");
                continue;
            }

            AbstractPiece movingPiece = fromSq.getCurrentPiece();
            if (movingPiece.getPieceColor() != currentTurn) {
                System.out.printf("Sıra %s taşlarında.%n", currentTurn);
                continue;
            }

            if (toSq == null) {
                System.out.println("Hedef karesi tahtada bulunamadı.");
                continue;
            }

            Location targetLocation = toSq.getLocation();
            if (!movingPiece.getValidMoves(board).contains(targetLocation)) {
                System.out.println("Geçersiz hamle.");
                continue;
            }

            movingPiece.makeMove(toSq);
            currentTurn = (currentTurn == PieceColor.LIGHT) ? PieceColor.DARK : PieceColor.LIGHT;

            board.printBoard();
        }
    }

    public static void printPiece(Movable piece) {
        System.out.println(piece.getValidMoves(null));

    }
}
