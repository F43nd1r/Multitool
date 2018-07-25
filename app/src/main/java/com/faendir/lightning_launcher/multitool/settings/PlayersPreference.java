package com.faendir.lightning_launcher.multitool.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import androidx.preference.MultiSelectListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author F43nd1r
 * @since 08.11.2016
 */

public class PlayersPreference extends MultiSelectListPreference implements SummaryPreference, HasPlayerEntries {
    public PlayersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        discoverPlayers(context.getPackageManager());
    }

    private List<CharSequence> getSelectedEntries() {
        List<CharSequence> entries = Arrays.asList(getEntries());
        List<CharSequence> values = Arrays.asList(getEntryValues());
        return StreamSupport.stream(getValues()).map(values::indexOf).filter(index -> index >= 0)
                .map(entries::get).sorted().collect(Collectors.toList());
    }

    @Override
    public CharSequence getSummaryText() {
        return TextUtils.join(", ", getSelectedEntries());
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        if (a.getBoolean(index, false)) {
            return StreamSupport.stream(getContext().getPackageManager().queryBroadcastReceivers(new Intent(Intent.ACTION_MEDIA_BUTTON), 0))
                    .map(info -> info.activityInfo.packageName).collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }
}
