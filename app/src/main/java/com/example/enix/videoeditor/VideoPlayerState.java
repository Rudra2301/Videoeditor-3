package com.example.enix.videoeditor;

public class VideoPlayerState {
    private int currentTime = 0;
    private String filename;
    private String messageText;
    private int start = 0;
    private int stop = 0;

    public String getMessageText() {
        return this.messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStart() {
        return this.start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return this.stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public void reset() {
        this.stop = 0;
        this.start = 0;
    }

    public int getDuration() {
        return this.stop - this.start;
    }

    public int getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(int currentTime) {
        this.currentTime = currentTime;
    }

    public boolean isValid() {
        return this.stop > this.start;
    }
}
