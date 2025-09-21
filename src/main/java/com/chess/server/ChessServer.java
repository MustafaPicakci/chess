package com.chess.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.chess.board.Board;
import com.chess.common.File;
import com.chess.common.Location;
import com.chess.move.MoveRecord;
import com.chess.piece.AbstractPiece;
import com.chess.piece.PieceColor;
import com.chess.squares.Square;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class ChessServer {

    private final Board board;
    private PieceColor currentTurn;
    private final ObjectMapper objectMapper;

    public ChessServer() {
        this.board = new Board();
        this.currentTurn = PieceColor.LIGHT;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public void start(int port) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/api/state", new StateHandler());
        httpServer.createContext("/api/move", new MoveHandler());
        httpServer.createContext("/", new StaticFileHandler(Path.of("ui")));
        httpServer.setExecutor(null);
        httpServer.start();
        System.out.printf("Chess server started at http://localhost:%d%n", port);
    }

    private class StateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                addCors(headers);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            sendJson(exchange, 200, buildStatePayload());
        }
    }

    private class MoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                Headers headers = exchange.getResponseHeaders();
                addCors(headers);
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendMethodNotAllowed(exchange);
                return;
            }

            String body = readBody(exchange.getRequestBody());
            Map<String, String> payload;
            try {
                payload = objectMapper.readValue(body, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
            } catch (JsonProcessingException e) {
                sendJson(exchange, 400, errorPayload("Geçersiz JSON"));
                return;
            }

            String fromNotation = Optional.ofNullable(payload.get("from")).map(String::trim).orElse(null);
            String toNotation = Optional.ofNullable(payload.get("to")).map(String::trim).orElse(null);

            if (!isValidNotation(fromNotation) || !isValidNotation(toNotation)) {
                sendJson(exchange, 400, errorPayload("Geçersiz koordinat"));
                return;
            }

            Location fromLocation = parseNotation(fromNotation);
            Location toLocation = parseNotation(toNotation);

            Square fromSquare = board.getLocationSquareMap().get(fromLocation);
            Square toSquare = board.getLocationSquareMap().get(toLocation);

            if (fromSquare == null || toSquare == null) {
                sendJson(exchange, 400, errorPayload("Tahta dışında bir kare seçtiniz"));
                return;
            }

            if (!fromSquare.isOccupied()) {
                sendJson(exchange, 400, errorPayload("Başlangıç karesinde taş yok"));
                return;
            }

            AbstractPiece piece = fromSquare.getCurrentPiece();
            if (piece.getPieceColor() != currentTurn) {
                sendJson(exchange, 400, errorPayload(String.format("Sıra %s taşlarında", currentTurn)));
                return;
            }

            if (!piece.getValidMoves(board).contains(toLocation)) {
                sendJson(exchange, 400, errorPayload("Hamle bu taş için geçerli değil"));
                return;
            }

            MoveRecord record;
            try {
                record = piece.makeMove(board, toSquare);
            } catch (IllegalStateException ex) {
                sendJson(exchange, 400, errorPayload(ex.getMessage()));
                return;
            }
            board.setLastMove(record);
            currentTurn = (currentTurn == PieceColor.LIGHT) ? PieceColor.DARK : PieceColor.LIGHT;

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("move", Map.of(
                    "from", fromNotation.toLowerCase(Locale.ROOT),
                    "to", toNotation.toLowerCase(Locale.ROOT),
                    "castling", record.isCastling(),
                    "enPassant", record.isEnPassantCapture(),
                    "promotion", record.getPromotionPiece() != null ? record.getPromotionPiece().getName() : null
            ));
            response.put("state", buildStatePayload());

            sendJson(exchange, 200, response);
        }
    }

    private Map<String, Object> buildStatePayload() {
        Map<String, Object> state = new HashMap<>();
        state.put("currentTurn", currentTurn.name());
        Map<String, Map<String, Object>> pieces = new HashMap<>();
        board.getLocationSquareMap().forEach((location, square) -> {
            if (square.isOccupied()) {
                AbstractPiece piece = square.getCurrentPiece();
                Map<String, Object> pieceInfo = new HashMap<>();
                pieceInfo.put("name", piece.getName());
                pieceInfo.put("color", piece.getPieceColor().name());
                pieces.put(formatNotation(location), pieceInfo);
            }
        });
        state.put("pieces", pieces);
        return state;
    }

    private void sendJson(HttpExchange exchange, int statusCode, Map<String, Object> payload) throws IOException {
        byte[] responseBytes = objectMapper.writeValueAsBytes(payload);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=utf-8");
        addCors(headers);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        sendJson(exchange, 405, errorPayload("Metot desteklenmiyor"));
    }

    private Map<String, Object> errorPayload(String message) {
        return Map.of("status", "error", "message", message, "state", buildStatePayload());
    }

    private void addCors(Headers headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Headers", "Content-Type");
        headers.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
    }

    private String readBody(InputStream bodyStream) throws IOException {
        return new String(bodyStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private boolean isValidNotation(String notation) {
        if (notation == null || notation.length() != 2) {
            return false;
        }
        char fileChar = Character.toUpperCase(notation.charAt(0));
        char rankChar = notation.charAt(1);
        return fileChar >= 'A' && fileChar <= 'H' && rankChar >= '1' && rankChar <= '8';
    }

    private Location parseNotation(String notation) {
        File file = File.valueOf(String.valueOf(Character.toUpperCase(notation.charAt(0))));
        int rank = Character.getNumericValue(notation.charAt(1));
        return new Location(file, rank);
    }

    private String formatNotation(Location location) {
        return ("" + location.getFile().name() + location.getRank()).toLowerCase(Locale.ROOT);
    }

    public static void main(String[] args) throws IOException {
        ChessServer server = new ChessServer();
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        server.start(port);
    }

    private static class StaticFileHandler implements HttpHandler {
        private final Path basePath;

        private StaticFileHandler(Path basePath) {
            this.basePath = basePath.toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Headers", "Content-Type");
                headers.add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(method)) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String requestedPath = exchange.getRequestURI().getPath();
            if (requestedPath == null || requestedPath.equals("/")) {
                requestedPath = "/index.html";
            }

            Path resolved = basePath.resolve(requestedPath.substring(1)).normalize();
            if (!resolved.startsWith(basePath) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
                resolved = basePath.resolve("index.html");
            }

            byte[] content = Files.readAllBytes(resolved);
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType(resolved));
            addBasicCors(headers);
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(content);
            }
        }

        private String contentType(Path path) {
            String filename = path.getFileName().toString();
            if (filename.endsWith(".html")) {
                return "text/html; charset=utf-8";
            }
            if (filename.endsWith(".css")) {
                return "text/css; charset=utf-8";
            }
            if (filename.endsWith(".js")) {
                return "text/javascript; charset=utf-8";
            }
            if (filename.endsWith(".json")) {
                return "application/json; charset=utf-8";
            }
            if (filename.endsWith(".png")) {
                return "image/png";
            }
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                return "image/jpeg";
            }
            return "application/octet-stream";
        }

        private void addBasicCors(Headers headers) {
            headers.add("Access-Control-Allow-Origin", "*");
        }
    }
}
