package com.example.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {
    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEPTH_CHUNK_SIZE = 5;
    private static final int MAX_DEPTH = 5;

    // MIN_CHUNK = "00000", MAX_CHUNK = "zzzzz"
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    private String path;

    public CommentPath(String path) {
        this.path = path;
    }

    public static CommentPath create(String path) {
        if(isDepthOverflow(path)) {
            throw new IllegalStateException("Depth overflow");
        }

        return new CommentPath(path);
    }

    private static boolean isDepthOverflow(String path) {
        return calculateDepth(path) > MAX_DEPTH;
    }

    private static int calculateDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    public int getDepth() {
        return calculateDepth(path);
    }

    public boolean isRoot() {
        return calculateDepth(path) == 1;
    }

    public String getParentPath() {
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE);
    }

    public CommentPath createChildCommentPath(String descendantsTopPath) {
        if(descendantsTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK);
        }

        String childrenTopPath = findChildrenTopPath(descendantsTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    private String findChildrenTopPath(String descendantsTopPath) {
        return descendantsTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        String lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if(isChunkOverflow(lastChunk)) {
            throw new IllegalStateException("Chunk overflow");
        }

        String result = convertPath(plusOne(lastChunk));
        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private static String convertPath(int value) {
        int charsetLength = CHARSET.length();

        String result = "";
        for(int i = 0; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return result;
    }

    private static int plusOne(String lastChunk) {
        int charsetLength = CHARSET.length();

        int value = 0;
        for(char c : lastChunk.toCharArray()) {
            value = value * charsetLength + CHARSET.indexOf(c);
        }
        value += 1;

        return value;
    }

    private boolean isChunkOverflow(String lastChunk) {
        return MAX_CHUNK.equals(lastChunk);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentPath that = (CommentPath) o;
        return Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getPath());
    }
}
