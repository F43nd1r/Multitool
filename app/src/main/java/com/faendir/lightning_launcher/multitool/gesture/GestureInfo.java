package com.faendir.lightning_launcher.multitool.gesture;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.omniadapter.model.Component;

import java.util.List;
import java.util.UUID;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
public class GestureInfo implements Parcelable, Component {
    private Intent intent;
    private transient Gesture gesture;
    private String label;
    private transient BitmapDrawable drawable;
    private final ParcelUuid uuid;
    private final State state = new State();

    GestureInfo(Intent intent, String label) {
        this(intent, label, null);
    }

    private GestureInfo(Intent intent, String label, ParcelUuid uuid) {
        this.intent = intent;
        this.label = label;
        if (uuid == null) {
            this.uuid = new ParcelUuid(UUID.randomUUID());
        } else {
            this.uuid = uuid;
        }
    }

    boolean isInvalid(){
        return intent == null || uuid == null;
    }

    @Nullable
    Gesture getGesture(Context context) {
        if (gesture == null) {
            List<Gesture> gestures = SingletonGestureLibrary.getGlobal(context).getGestures(uuid.toString());
            if (gestures != null && gestures.size() > 0) {
                gesture = gestures.get(0);
            }
        }
        return gesture;
    }

    void setGesture(Context context, Gesture gesture) {
        this.gesture = gesture;
        GestureLibrary library = SingletonGestureLibrary.getGlobal(context);
        String uuid = this.uuid.toString();
        List<Gesture> list = library.getGestures(uuid);
        if (list == null || !list.contains(gesture)) {
            if (list != null) library.removeEntry(uuid);
            library.addGesture(uuid, gesture);
            library.save();
        }
    }

    void removeGesture(Context context) {
        GestureLibrary library = SingletonGestureLibrary.getGlobal(context);
        library.removeGesture(uuid.toString(), getGesture(context));
        library.save();
    }

    Intent getIntent() {
        return intent;
    }

    void setIntent(Intent intent) {
        this.intent = intent;
    }

    public String getText() {
        return label;
    }

    void setLabel(String label) {
        this.label = label;
    }

    boolean hasUuid(UUID uuid) {
        return this.uuid.getUuid().equals(uuid);
    }

    Drawable getImage(Context context) {
        if (drawable == null) {
            int iconSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconSize();
            //noinspection deprecation
            int color = context.getResources().getColor(R.color.accent);
            Gesture gesture = getGesture(context);
            if(gesture != null) {
                Bitmap bitmap = gesture.toBitmap(iconSize, iconSize, iconSize / 20, color);
                drawable = new BitmapDrawable(context.getResources(), bitmap);
            }
        }
        return drawable;
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
        dest.writeParcelable(uuid, flags);
    }

    public static final Creator<GestureInfo> CREATOR = new Creator<GestureInfo>() {
        @Override
        public GestureInfo createFromParcel(Parcel source) {
            ClassLoader loader = getClass().getClassLoader();
            Intent intent = source.readParcelable(loader);
            Gesture gesture = source.readParcelable(loader);
            String label = source.readString();
            ParcelUuid uuid = source.readParcelable(loader);
            GestureInfo info = new GestureInfo(intent, label, uuid);
            //set directly as we don't have a context and we know it is already in the database
            info.gesture = gesture;
            return info;
        }

        @Override
        public GestureInfo[] newArray(int size) {
            return new GestureInfo[size];
        }
    };

    @NonNull
    @Override
    public State getState() {
        return state;
    }
}
