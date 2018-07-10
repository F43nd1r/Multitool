package com.faendir.lightning_launcher.multitool.animation;

/**
 * @author lukas
 * @since 10.07.18
 */
class PointB {
    boolean x;
    boolean y;

    PointB(boolean x, boolean y) {
        this.x = x;
        this.y = y;
    }

    boolean any() {
        return x || y;
    }

    boolean both() {
        return x && y;
    }
}
