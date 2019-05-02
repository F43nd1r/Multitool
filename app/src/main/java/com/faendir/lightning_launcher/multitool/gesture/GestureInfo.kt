package com.faendir.lightning_launcher.multitool.gesture

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.gesture.Gesture
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.fastadapter.DeletableModel
import java.util.*

/**
 * Created on 26.01.2016.
 *
 * @author F43nd1r
 */
@Keep
class GestureInfo(intent: Intent, override var name: String, val uuid: ParcelUuid = ParcelUuid(UUID.randomUUID())) : Parcelable, DeletableModel {
    var intent: Intent = intent
        internal set
    @Transient
    private var gesture: Gesture? = null
    @Transient
    private var drawable: BitmapDrawable? = null

    override val tintColor: Int
        get() = Color.WHITE


    /**
     * Constructor for Gson
     */
    private constructor() : this(Intent(), "")

    internal fun getGesture(context: Context): Gesture? {
        if (gesture == null) {
            gesture = SingleStoreGestureLibrary.getInstance(context).getGestures(uuid.toString())?.first()
        }
        return gesture
    }

    internal fun setGesture(context: Context, gesture: Gesture) {
        this.gesture = gesture
        val library = SingleStoreGestureLibrary.getInstance(context)
        val uuid = this.uuid.toString()
        val list = library.getGestures(uuid)
        if (list == null || !list.contains(gesture)) {
            if (list != null) library.removeEntry(uuid)
            library.addGesture(uuid, gesture)
            library.save()
        }
    }

    internal fun removeGesture(context: Context) {
        getGesture(context)?.let {
            val library = SingleStoreGestureLibrary.getInstance(context)
            library.removeGesture(uuid.toString(), it)
            library.save()
        }
    }

    override fun getUndoText(context: Context): String = context.getString(R.string.text_gestureDeleted)

    internal fun hasUuid(uuid: UUID): Boolean = this.uuid.uuid == uuid

    override fun getIcon(context: Context): Drawable {
        if (drawable == null) {
            val iconSize = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).launcherLargeIconSize
            val color = ContextCompat.getColor(context, R.color.accent)
            val gesture = getGesture(context)
            if (gesture != null) {
                val bitmap = gesture.toBitmap(iconSize, iconSize, iconSize / 20, color)
                drawable = BitmapDrawable(context.resources, bitmap)
            }
        }
        return drawable!!
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(intent, flags)
        dest.writeParcelable(gesture, flags)
        dest.writeString(name)
        dest.writeParcelable(uuid, flags)
    }

    companion object CREATOR : Parcelable.Creator<GestureInfo> {
        override fun createFromParcel(source: Parcel): GestureInfo {
            val loader = GestureInfo::class.java.classLoader
            val intent = source.readParcelable<Intent>(loader)!!
            val gesture = source.readParcelable<Gesture>(loader)!!
            val label = source.readString()!!
            val uuid = source.readParcelable<ParcelUuid>(loader)!!
            val info = GestureInfo(intent, label, uuid)
            //set directly as we don't have a context and we know it is already in the database
            info.gesture = gesture
            return info
        }

        override fun newArray(size: Int): Array<GestureInfo?> {
            return arrayOfNulls(size)
        }
    }
}
