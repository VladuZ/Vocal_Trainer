package com.example.testing;

public class Note {
    private String content;
    private int position;
    private int length;

    public Note(String content, int position, int length) {
        this.content = content;
        this.position = position;
        this.length = length;
    }

    public String getContent() {
        return content;
    }

    public int getPosition() {
        return position;
    }

    public int getLength() {
        return length;
    }
}