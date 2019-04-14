package com.faendir.lightning_launcher.multitool.scriptmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.faendir.lightning_launcher.multitool.Loader;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.fastadapter.DeletableModel;


/**
 * Created by Lukas on 22.08.2015.
 * Represents a script
 */
@Keep
public class Script extends net.pierrox.lightning_launcher.api.Script implements Comparable<Script>, DeletableModel {

    public Script(net.pierrox.lightning_launcher.api.Script script) {
        this(script.getName(), script.getId(), script.getText(), script.getFlags(), script.getPath());
    }

    public Script(String name, int id, String code, int flags, String path) {
        super(id, code, name, path, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Script script = (Script) o;

        return getId() == script.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    @Override
    public int compareTo(@NonNull Script another) {
        return getName().toLowerCase().compareTo(another.getName().toLowerCase());
    }

    @Override
    public String getUndoText(@NonNull Context context) {
        return context.getString(R.string.text_scriptDeleted);
    }

    @Override
    public int getTintColor() {
        return isDisabled() ? Color.RED : Color.WHITE;
    }

    public boolean isDisabled() {
        return (getFlags() & Loader.FLAG_DISABLED) != 0;
    }

    @Override
    public Drawable getIcon(@NonNull Context context) {
        return ContextCompat.getDrawable(context, R.drawable.ic_file_white);
    }
}
