package com.faendir.lightning_launcher.multitool.gesture;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.DeletableModel;

import java.util.List;
import java.util.UUID;

import java9.util.Optional;
import java9.util.stream.StreamSupport;

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
@Keep
public class GestureInfo implements Parcelable, DeletableModel {
    private Intent intent;
    private transient Gesture gesture;
    private String name;
    private transient BitmapDrawable drawable;
    private final ParcelUuid uuid;


    /**
     * Constructor for Gson
     */
    @SuppressWarnings("unused")
    private GestureInfo(){
        this(null,null, null);
    }

    GestureInfo(Intent intent, String name) {
        this(intent, name, null);
    }

    public GestureInfo(Intent intent, String name, ParcelUuid uuid) {
        this.intent = intent;
        this.name = name;
        if (uuid == null) {
            this.uuid = new ParcelUuid(UUID.randomUUID());
        } else {
            this.uuid = uuid;
        }
    }

    boolean isInvalid() {
        return intent == null || uuid == null;
    }

    @Nullable
    Gesture getGesture(Context context) {
        if (gesture == null) {
            Optional.ofNullable(SingleStoreGestureLibrary.getInstance(context).getGestures(uuid.toString()))
                    .ifPresent(list -> gesture = StreamSupport.stream(list).findFirst().get());
        }
        return gesture;
    }

    void setGesture(Context context, Gesture gesture) {
        this.gesture = gesture;
        SingleStoreGestureLibrary library = SingleStoreGestureLibrary.getInstance(context);
        String uuid = this.uuid.toString();
        List<Gesture> list = library.getGestures(uuid);
        if (list == null || !list.contains(gesture)) {
            if (list != null) library.removeEntry(uuid);
            library.addGesture(uuid, gesture);
            library.save();
        }
    }

    void removeGesture(Context context) {
        SingleStoreGestureLibrary library = SingleStoreGestureLibrary.getInstance(context);
        library.removeGesture(uuid.toString(), getGesture(context));
        library.save();
    }

    public Intent getIntent() {
        return intent;
    }

    void setIntent(Intent intent) {
        this.intent = intent;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUndoText(@NonNull Context context) {
        return context.getString(R.string.text_gestureDeleted);
    }

    @Override
    public int getTintColor(@NonNull Context context) {
        return Color.WHITE;
    }

    boolean hasUuid(UUID uuid) {
        return this.uuid.getUuid().equals(uuid);
    }

    public UUID getUuid() {
        return uuid.getUuid();
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        if (drawable == null) {
            int iconSize = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getLauncherLargeIconSize();
            int color = context.getResources().getColor(R.color.accent);
            Gesture gesture = getGesture(context);
            if (gesture != null) {
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
        dest.writeString(name);
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
}
