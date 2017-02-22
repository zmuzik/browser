package com.sabaibrowser.view;

public class Placement {
    public int x, y;
    public float opacity;

    public Placement(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Placement(int x, int y, float opacity) {
        this.x = x;
        this.y = y;
        this.opacity = opacity;
    }
}
