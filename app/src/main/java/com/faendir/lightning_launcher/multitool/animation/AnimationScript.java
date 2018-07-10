package com.faendir.lightning_launcher.multitool.animation;

import android.app.AlertDialog;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.ViewPropertyAnimator;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

import static com.faendir.lightning_launcher.multitool.util.Utils.GSON;

/**
 * @author lukas
 * @since 09.07.18
 */
public class AnimationScript implements JavaScript.Setup, JavaScript.Normal {
    private static final String TAG_ANIMATION = "animation";
    private final Utils utils;

    public AnimationScript(Utils utils) {
        this.utils = utils;
    }

    @Override
    public void setup() {
        Script script = utils.installNormalScript();
        Container container = utils.getContainer();
        utils.addEventHandler(container.getProperties(), PropertySet.POSITION_CHANGED, EventHandler.RUN_SCRIPT, script.getId() + "/" + getClass().getName());
        String tag = container.getTag(TAG_ANIMATION);
        Config config = tag != null ? GSON.fromJson(tag, Config.class) : new Config();
        new AlertDialog.Builder(utils.getLightningContext()).setTitle("Choose an animation style")
                .setItems(new CharSequence[]{"Bulldoze", "Card Stack", "Flip", "Flip 3D", "Shrink", "Turn"}, (dialog, which) -> {
                    config.animation = which;
                    container.setTag(TAG_ANIMATION, GSON.toJson(config));
                })
                .setNegativeButton("Disable", (dialog, which) -> {
                    config.animation = -1;
                    container.setTag(TAG_ANIMATION, GSON.toJson(config));
                })
                .show();
    }

    @Override
    public void run() {
        Container container = utils.getContainer();
        String tag = container.getTag(TAG_ANIMATION);
        if (tag == null) return;
        Config config = GSON.fromJson(tag, Config.class);
        if (config == null || config.animation < 0) return;
        Size containerSize = new Size(container.getWidth(), container.getHeight());
        PointF position = new PointF(container.getPositionX(), container.getPositionY());
        Point activePage = pageOf(position, containerSize);
        PointF percent = new PointF(position.x / containerSize.width - activePage.x, position.y / containerSize.height - activePage.y);
        Animation animation;
        switch (config.animation) {
            case 0:
                animation = new Bulldoze(percent, containerSize);
                break;
            case 1:
                animation = new Card(percent, containerSize);
                break;
            case 2:
                animation = new Flip(percent, containerSize);
                break;
            case 3:
                animation = new Flip3D(percent, containerSize);
                break;
            case 4:
                animation = new Shrink(percent, containerSize);
                break;
            case 5:
                animation = new Turn(percent, containerSize);
                break;
            default:
                throw new RuntimeException("Invalid animation id");
        }
        for (Item item : container.getAllItems()) {
            PointF center = center(item);
            Point page = pageOf(center, containerSize);
            PointB onPage = new PointB(page.x == activePage.x, page.y == activePage.y);
            if ((onPage.x || page.x == activePage.x + 1) && (onPage.y || page.y == activePage.y + 1)) {
                animation.getTransformation(makePageRelative(center, containerSize), onPage).transform(item);
            } else {
                new Transformation().transform(item);
            }
        }
    }

    private Point pageOf(PointF position, Size containerSize) {
        return new Point((int) Math.floor(position.x / containerSize.width), (int) Math.floor(position.y / containerSize.height));
    }

    private PointF makePageRelative(PointF point, Size containerSize) {
        return new PointF(positiveModulo(point.x, containerSize.width), positiveModulo(point.y, containerSize.height));
    }

    private float positiveModulo(float i, int modulo) {
        float result = i % modulo;
        if (result < 0) result += modulo;
        return result;
    }

    private PointF center(Item item) {
        double radius = item.getRotation() * Math.PI / 180;
        double sine = Math.abs(Math.sin(radius));
        double cosine = Math.abs(Math.cos(radius));
        double width = item.getWidth() * item.getScaleX();
        double height = item.getHeight() * item.getScaleY();
        return new PointF((float) (item.getPositionX() + (width * cosine + height * sine) / 2), (float) (item.getPositionY() + (height * cosine + width * sine) / 2));
    }

    private static class Flip3D extends Animation {
        Flip3D(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF center, PointB isStart) {
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
    }

    private static class Turn extends Animation {
        Turn(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF center, PointB isStart) {
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
    }

    private static abstract class Animation {
        final PointF percent;
        final Size containerSize;

        Animation(PointF percent, Size containerSize) {
            this.percent = percent;
            this.containerSize = containerSize;
        }

        public abstract Transformation getTransformation(PointF center, PointB isStart);
    }

    private static class Bulldoze extends Animation {
        Bulldoze(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF center, PointB isStart) {
            Transformation result = new Transformation();
            result.scale.x = isStart.x ? 1 - percent.x : percent.x;
            result.scale.y = isStart.y ? 1 - percent.y : percent.y;
            result.translate.x = isStart.x ? (containerSize.width - center.x) * percent.x : center.x * (percent.x - 1);
            result.translate.y = isStart.y ? (containerSize.height - center.y) * percent.y : center.y * (percent.y - 1);
            return result;
        }
    }

    private static class Card extends Animation {
        Card(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF center, PointB isStart) {
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
    }

    private static class Flip extends Animation {
        Flip(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF cent, PointB isStart) {
            Transformation result = new Transformation();
            if (isStart.x != percent.x >= 0.5) {
                result.scale.x = isStart.x ? 1 - percent.x * 2 : percent.x * 2 - 1;
                result.translate.x = isStart.x ? 2 * percent.x * (containerSize.width - cent.x) : 2 * (percent.x - 1) * cent.x;
            } else {
                result.alpha = 0;
            }
            if (isStart.y != percent.y >= 0.5) {
                result.scale.y = isStart.y ? 1 - percent.y * 2 : percent.y * 2 - 1;
                result.translate.y = isStart.y ? 2 * percent.y * (containerSize.height - cent.y) : 2 * cent.y * (percent.y - 1);
            } else {
                result.alpha = 0;
            }
            return result;
        }
    }

    private static class Shrink extends Animation {
        Shrink(PointF percent, Size containerSize) {
            super(percent, containerSize);
        }

        @Override
        public Transformation getTransformation(PointF cent, PointB isStart) {
            Transformation result = new Transformation().onlyUnpinnedItems();
            if (Math.abs(percent.x - 0.5) <= Math.abs(percent.y - 0.5)) {
                float x = (isStart.x ? percent.x : 1 - percent.x) * 0.75f;
                result.scale.x = result.scale.y = 1 - x;
                result.translate.x = (containerSize.width - cent.x) * x;
                result.translate.y = (containerSize.height / 2 - cent.y) * x;
            } else {
                float y = (isStart.y ? percent.y : 1 - percent.y) * 0.75f;
                result.scale.x = result.scale.y = 1 - y;
                result.translate.x = (containerSize.width / 2 - cent.x) * y;
                result.translate.y = (containerSize.height - cent.y) * y;
            }
            return result;
        }
    }

    private static class Transformation {
        PointF scale = new PointF(1, 1);
        PointF translate = new PointF(0, 0);
        PointF pivot = new PointF(0, 0);
        float turn = 0;
        PointF rotation = new PointF(0, 0);
        float alpha = 1;
        boolean partial = true;

        private void transform(Item item) {
            String pinMode = item.getProperties().getString(PropertySet.ITEM_PIN_MODE);
            PointB transform = new PointB(!pinMode.contains("X"), !pinMode.contains("Y"));
            if (partial ? transform.any() : transform.both()) {
                item.getRootView().setPivotX(item.getWidth() / 2 + pivot.x);
                item.getRootView().setPivotY(item.getHeight() / 2 + pivot.y);
                ViewPropertyAnimator animator = item.getRootView().animate().setDuration(0).alpha(alpha).rotation(turn * 360);
                if (transform.x) animator.scaleX(scale.x).translationX(translate.x).rotationX(rotation.x * 360);
                if (transform.y) animator.scaleY(scale.y).translationY(translate.y).rotationY(rotation.y * 360);
                animator.start();
            }
        }

        Transformation onlyUnpinnedItems() {
            partial = false;
            return this;
        }
    }

    private static class Config {
        int animation = -1;
    }

    private static class Size {
        int width;
        int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private static class PointB {
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
}
