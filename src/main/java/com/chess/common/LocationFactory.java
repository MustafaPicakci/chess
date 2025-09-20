package com.chess.common;

public class LocationFactory {
    private static final File[] files = File.values();

    private static final int MIN_RANK = 1;
    private static final int MAX_RANK = 8;

    public static Location build(Location current, Integer fileOffset, Integer rankOffset) {
        int currentFile = current.getFile().ordinal();
        int targetFile = currentFile + fileOffset;
        int targetRank = current.getRank() + rankOffset;

        if (targetFile < 0 || targetFile >= files.length) {
            return null;
        }

        if (targetRank < MIN_RANK || targetRank > MAX_RANK) {
            return null;
        }

        return new Location(files[targetFile], targetRank);
    }
}
