package com.faendir.lightning_launcher.multitool.animation;

import android.app.AlertDialog;
import android.graphics.Point;
import android.graphics.PointF;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.EventHandler;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.JavaScript;
import com.faendir.lightning_launcher.multitool.proxy.PropertySet;
import com.faendir.lightning_launcher.multitool.proxy.Script;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import java9.util.stream.Stream;

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

    static PointF center(Item item, boolean absolute) {
        double radius = item.getRotation() * Math.PI / 180;
        double sine = Math.abs(Math.sin(radius));
        double cosine = Math.abs(Math.cos(radius));
        double width = item.getWidth() * item.getScaleX();
        double height = item.getHeight() * item.getScaleY();
        PointF result = new PointF((float) (width * cosine + height * sine) / 2, (float) (height * cosine + width * sine) / 2);
        if (absolute) {
            result.x += item.getPositionX();
            result.y += item.getPositionY();
        }
        return result;
    }

    @Override
    public void setup() {
        Script script = utils.installNormalScript();
        Container container = utils.getContainer();
        utils.addEventHandler(container.getProperties(), PropertySet.POSITION_CHANGED, EventHandler.RUN_SCRIPT, script.getId() + "/" + getClass().getName());
        String tag = container.getTag(TAG_ANIMATION);
        Config config = tag != null ? GSON.fromJson(tag, Config.class) : new Config();
        new AlertDialog.Builder(utils.getLightningContext()).setTitle("Choose an animation style")
                .setItems(Stream.of(Animation.values()).map(Animation::getLabel).toArray(CharSequence[]::new), (dialog, which) -> {
                    config.animation = Animation.values()[which];
                    container.setTag(TAG_ANIMATION, GSON.toJson(config));
                })
                .setNegativeButton("Disable", (dialog, which) -> {
                    config.animation = null;
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
        if (config == null || config.animation == null) return;
        Size containerSize = new Size(container.getWidth(), container.getHeight());
        PointF position = new PointF(container.getPositionX(), container.getPositionY());
        Point activePage = pageOf(position, containerSize);
        PointF percent = new PointF(position.x / containerSize.width - activePage.x, position.y / containerSize.height - activePage.y);
        for (Item item : container.getAllItems()) {
            PointF center = center(item, true);
            Point page = pageOf(center, containerSize);
            PointB onPage = new PointB(page.x == activePage.x, page.y == activePage.y);
            if ((onPage.x || page.x == activePage.x + 1) && (onPage.y || page.y == activePage.y + 1)) {
                config.animation.getTransformation(percent, containerSize, makePageRelative(center, containerSize), onPage).transform(item);
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

    private static class Config {
        Animation animation = null;
    }
}
