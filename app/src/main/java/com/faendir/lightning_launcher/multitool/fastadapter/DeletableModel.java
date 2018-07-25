package com.faendir.lightning_launcher.multitool.fastadapter;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * @author F43nd1r
 * @since 13.10.2017
 */

public interface DeletableModel extends Model {
    String getUndoText(@NonNull Context context);

    void setName(String name);
}
