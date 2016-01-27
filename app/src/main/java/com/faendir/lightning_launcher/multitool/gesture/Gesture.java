package com.faendir.lightning_launcher.multitool.gesture;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 26.01.2016.
 */
public class Gesture implements Parcelable, ImageText {
    private Intent intent;
    private android.gesture.Gesture gesture;
    private String label;

    public Gesture(android.gesture.Gesture gesture, Intent intent, String label) {
        this.gesture = gesture;
        this.intent = intent;
        this.label = label;
    }

    public android.gesture.Gesture getGesture() {
        return gesture;
    }

    public Intent getIntent() {
        return intent;
    }

    public String getText() {
        return label;
    }

    @Override
    public Drawable getImage(Context context) {
        int iconSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconSize();
        return new BitmapDrawable(context.getResources(),
                gesture.toBitmap(iconSize, iconSize, iconSize / 20, context.getResources().getColor(R.color.accent)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(intent, flags);
        dest.writeParcelable(gesture, flags);
        dest.writeString(label);
    }

    public static final Creator<Gesture> CREATOR = new Creator<Gesture>() {
        @Override
        public Gesture createFromParcel(Parcel source) {
            ClassLoader loader = getClass().getClassLoader();
            Intent intent = source.readParcelable(loader);
            android.gesture.Gesture gesture = source.readParcelable(loader);
            String label = source.readString();
            return new Gesture(gesture, intent, label);
        }

        @Override
        public Gesture[] newArray(int size) {
            return new Gesture[size];
        }
    };
}
