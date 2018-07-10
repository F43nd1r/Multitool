package com.faendir.lightning_launcher.multitool.animation;

import android.graphics.PointF;

/**
 * @author lukas
 * @since 10.07.18
 */
enum Animation {
    BULLDOZE("Bulldoze") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation();
            result.scale.x = isStart.x ? 1 - percent.x : percent.x;
            result.scale.y = isStart.y ? 1 - percent.y : percent.y;
            result.translate.x = isStart.x ? (containerSize.width - center.x) * percent.x : center.x * (percent.x - 1);
            result.translate.y = isStart.y ? (containerSize.height - center.y) * percent.y : center.y * (percent.y - 1);
            return result;
        }
    },
    CARD("Card Stack") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation();
            if (!isStart.x && isStart.y) {
                result.translate.x = containerSize.width * (percent.x - 1);
                result.alpha = percent.x;
            } else if (isStart.x && !isStart.y) {
                result.translate.y = containerSize.height * (percent.y - 1);
                result.alpha = percent.y;
            }
            return result;
        }
    },
    FLIP("Flip") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation();
            if (isStart.x != percent.x >= 0.5) {
                result.scale.x = isStart.x ? 1 - percent.x * 2 : percent.x * 2 - 1;
                result.translate.x = isStart.x ? 2 * percent.x * (containerSize.width - center.x) : 2 * (percent.x - 1) * center.x;
            } else {
                result.alpha = 0;
            }
            if (isStart.y != percent.y >= 0.5) {
                result.scale.y = isStart.y ? 1 - percent.y * 2 : percent.y * 2 - 1;
                result.translate.y = isStart.y ? 2 * percent.y * (containerSize.height - center.y) : 2 * center.y * (percent.y - 1);
            } else {
                result.alpha = 0;
            }
            return result;
        }
    },
    FLIP_3D("Flip 3D") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation();
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                if (isStart.x != percent.x >= 0.5) {
                    float x = isStart.x ? percent.x : percent.x - 1;
                    result.rotation.y = -x / 2;
                    result.translate.x = x * containerSize.width;
                    result.pivot.x = -center.x + containerSize.width / 2;
                } else {
                    result.alpha = 0;
                }
            } else {
                if (isStart.y != percent.y >= 0.5) {
                    float y = isStart.y ? percent.y : percent.y - 1;
                    result.rotation.x = y / 2;
                    result.translate.y = y * containerSize.height;
                    result.pivot.y = -center.y + containerSize.height / 2;
                } else {
                    result.alpha = 0;
                }
            }
            return result;
        }
    },
    SHRINK("Shrink") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation().onlyUnpinnedItems();
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                float x = (isStart.x ? percent.x : 1 - percent.x) * 0.75f;
                result.scale.x = result.scale.y = 1 - x;
                result.translate.x = (containerSize.width - center.x) * x;
                result.translate.y = (containerSize.height / 2 - center.y) * x;
            } else {
                float y = (isStart.y ? percent.y : 1 - percent.y) * 0.75f;
                result.scale.x = result.scale.y = 1 - y;
                result.translate.x = (containerSize.width / 2 - center.x) * y;
                result.translate.y = (containerSize.height - center.y) * y;
            }
            return result;
        }
    },
    TURN("Turn") {
        @Override
        public Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart) {
            Transformation result = new Transformation().onlyUnpinnedItems();
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                if (isStart.y) {
                    float x = isStart.x ? percent.x : percent.x - 1;
                    result.turn = x / 4;
                    result.translate.x = x * containerSize.width;
                }
            } else {
                if (isStart.x) {
                    float y = isStart.y ? percent.y : percent.y - 1;
                    result.turn = -y / 4;
                    result.translate.y = y * containerSize.height;
                }
            }
            result.pivot.x = -center.x;
            result.pivot.y = -center.y;
            return result;
        }
    };
    private final String label;

    Animation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract Transformation getTransformation(PointF percent, Size containerSize, PointF center, PointB isStart);
}
