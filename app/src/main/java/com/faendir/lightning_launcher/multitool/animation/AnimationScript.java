package com.faendir.lightning_launcher.multitool.animation;

import android.app.AlertDialog;
import android.graphics.Point;
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
                .setItems(new CharSequence[]{"Bulldoze", "Card Style", "Flip", "Shrink"}, (dialog, which) -> {
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
        int cWidth = container.getWidth();
        int cHeight = container.getHeight();
        float posX = container.getPositionX();
        float posY = container.getPositionY();
        int cPageX = (int) Math.floor(posX / cWidth);
        int cPageY = (int) Math.floor(posY / cHeight);
        float percentX = (posX - cPageX * cWidth) / cWidth;
        float percentY = (posY - cPageY * cHeight) / cHeight;
        Animation animation;
        switch (config.animation) {
            case 0:
                animation = new Bulldoze(percentX, percentY, cWidth, cHeight);
                break;
            case 1:
                animation = new Card(percentX, percentY, cWidth, cHeight);
                break;
            case 2:
                animation = new Flip(percentX, percentY, cWidth, cHeight);
                break;
            case 3:
                animation = new Shrink(percentX, percentY, cWidth, cHeight);
                break;
            default:
                throw new RuntimeException("Invalid animation id");
        }
        for (Item item : container.getAllItems()) {
            Point cent = center(item);
            int pageX = (int) Math.floor((float) cent.x / cWidth);
            int pageY = (int) Math.floor((float) cent.y / cHeight);
            boolean onPageX = pageX == cPageX;
            boolean onPageY = pageY == cPageY;
            if ((onPageX || pageX == cPageX + 1) && (onPageY || pageY == cPageY + 1)) {
                Transformation transformation = animation.getTransformation(cent, onPageX, onPageY);
                String pinMode = item.getProperties().getString(PropertySet.ITEM_PIN_MODE);
                boolean transformX = !pinMode.contains("X");
                boolean transformY = !pinMode.contains("Y");
                if (((transformX || transformY) && transformation.partial) || (transformX && transformY)) {
                    ViewPropertyAnimator a = item.getRootView().animate().setDuration(0).alpha(transformation.alpha);
                    if (transformX) a.scaleX(transformation.scaleX).translationX(transformation.translateX);
                    if (transformY) a.scaleY(transformation.scaleY).translationY(transformation.translateY);
                    a.start();
                }
            }
        }
    }

    private Point center(Item item) {
        double r = item.getRotation();
        r = r * Math.PI / 180;
        double sin = Math.abs(Math.sin(r));
        double cos = Math.abs(Math.cos(r));
        double w = item.getWidth() * item.getScaleX();
        double h = item.getHeight() * item.getScaleY();
        return new Point((int) (item.getPositionX() + (w * cos + h * sin) * 0.5), (int) (item.getPositionY() + (h * cos + w * sin) * 0.5));
    }

    private static abstract class Animation {
        final float percentX;
        final float percentY;
        final int cWidth;
        final int cHeight;

        Animation(float percentX, float percentY, int cWidth, int cHeight) {
            this.percentX = percentX;
            this.percentY = percentY;
            this.cWidth = cWidth;
            this.cHeight = cHeight;
        }

        public abstract Transformation getTransformation(Point center, boolean isLeft, boolean isTop);
    }

    private static class Bulldoze extends Animation {
        Bulldoze(float percentX, float percentY, int cWidth, int cHeight) {
            super(percentX, percentY, cWidth, cHeight);
        }

        @Override
        public Transformation getTransformation(Point cent, boolean isLeft, boolean isTop) {
            Transformation result = new Transformation();
            result.scaleX = isLeft ? 1 - percentX : percentX;
            result.scaleY = isTop ? 1 - percentY : percentY;
            result.translateX = (isLeft ? (cWidth - cent.x) * percentX : cent.x * (percentX - 1));
            result.translateY = (isTop ? (cHeight - cent.y) * percentY : cent.y * (percentY - 1));
            return result;
        }
    }

    private static class Card extends Animation {
        Card(float percentX, float percentY, int cWidth, int cHeight) {
            super(percentX, percentY, cWidth, cHeight);
        }

        @Override
        public Transformation getTransformation(Point center, boolean isLeft, boolean isTop) {
            Transformation result = new Transformation();
            if (!isLeft && isTop) {
                result.translateX = cWidth * (percentX - 1);
                result.alpha = percentX;
            } else if (isLeft && !isTop) {
                result.translateY = cHeight * (percentY - 1);
                result.alpha = percentY;
            }
            return result;
        }
    }

    private static class Flip extends Animation {
        Flip(float percentX, float percentY, int cWidth, int cHeight) {
            super(percentX, percentY, cWidth, cHeight);
        }

        @Override
        public Transformation getTransformation(Point cent, boolean isLeft, boolean isTop) {
            Transformation result = new Transformation();
            if (isLeft != percentX >= 0.5) {
                result.scaleX = isLeft ? 1 - percentX * 2 : percentX * 2 - 1;
                result.translateX = isLeft ? 2 * percentX * (cWidth - cent.x) : 2 * cent.x * (percentX - 1);
            } else {
                result.alpha = 0;
            }
            if (isTop != percentY >= 0.5) {
                result.scaleY = isTop ? 1 - percentY * 2 : percentY * 2 - 1;
                result.translateY = isTop ? 2 * percentY * (cHeight - cent.y) : 2 * cent.y * (percentY - 1);
            } else {
                result.alpha = 0;
            }
            return result;
        }
    }

    private static class Shrink extends Animation {
        Shrink(float percentX, float percentY, int cWidth, int cHeight) {
            super(percentX, percentY, cWidth, cHeight);
        }

        @Override
        public Transformation getTransformation(Point cent, boolean isLeft, boolean isTop) {
            Transformation result = new Transformation();
            result.partial = false;
            if (Math.abs(percentX - 0.5) <= Math.abs(percentY - 0.5)) {
                result.scaleX = result.scaleY = isLeft ? 1 - percentX * 0.75f : 0.25f + percentX * 0.75f;
                result.translateX = isLeft ? (cWidth - cent.x) * percentX * 0.75f : (cWidth - cent.x) * (0.75f - percentX * 0.75f);
                result.translateY = isLeft ? (cHeight / 2 - cent.y) * percentX * 0.75f : (cHeight / 2 - cent.y) * (0.75f - percentX * 0.75f);
            } else {
                result.scaleX = result.scaleY = isTop ? 1 - percentY * 0.75f : 0.25f + percentY * 0.75f;
                result.translateX = isTop ? (cWidth / 2 - cent.x) * percentY * 0.75f : (cWidth / 2 - cent.x) * (0.75f - percentY * 0.75f);
                result.translateY = isTop ? (cHeight - cent.y) * percentY * 0.75f : (cHeight - cent.y) * (0.75f - percentY * 0.75f);
            }
            return result;
        }
    }

    private static class Transformation {
        float scaleX = 1;
        float scaleY = 1;
        float translateX = 0;
        float translateY = 0;
        float alpha = 1;
        boolean partial = true;
    }

    private static class Config {
        int animation;

        Config() {
            animation = -1;
        }
    }
}
