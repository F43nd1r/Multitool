package com.faendir.lightning_launcher.multitool.util.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.faendir.lightning_launcher.multitool.badge.BadgeDataSource;
import com.faendir.lightning_launcher.multitool.gesture.GestureLibraryDataSource;
import com.faendir.lightning_launcher.multitool.gesture.GestureMetaDataSource;
import com.faendir.lightning_launcher.multitool.music.MusicDataSource;
import com.faendir.lightning_launcher.multitool.util.LambdaUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import java9.util.Optional;
import java9.util.stream.StreamSupport;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;

/**
 * Provides various data (including SharedPreferences) to LL
 *
 * @author F43nd1r
 * @since 15.08.2015
 */
public class DataProvider extends ContentProvider {

    private enum Mode {
        r(ParcelFileDescriptor.MODE_READ_ONLY | ParcelFileDescriptor.MODE_CREATE),
        rwt(ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE);

        private final int constant;

        Mode(int constant) {
            this.constant = constant;
        }

        public int getConstant() {
            return constant;
        }
    }

    private static final String AUTHORITY = "com.faendir.lightning_launcher.multitool.provider";
    private static final List<DataSource> DATA_SOURCES = Arrays.asList(
            new SharedPreferencesDataSource(),
            new GestureLibraryDataSource(),
            new GestureMetaDataSource(),
            new MusicDataSource(),
            new BadgeDataSource());

    private final UriMatcher uriMatcher;

    public DataProvider() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        for (int i = 0; i < DATA_SOURCES.size(); i++) {
            DataSource dataSource = DATA_SOURCES.get(i);
            uriMatcher.addURI(AUTHORITY, dataSource.getPath(), i);
        }
    }

    public static InputStream openFileForRead(Context context, Class<? extends FileDataSource> dataSourceClass) throws FileNotFoundException {
        return context.getContentResolver().openInputStream(getContentUri(dataSourceClass));
    }

    public static OutputStream openFileForWrite(Context context, Class<? extends FileDataSource> dataSourceClass) throws FileNotFoundException {
        return context.getContentResolver().openOutputStream(getContentUri(dataSourceClass), Mode.rwt.name());
    }

    public static Uri getContentUri(Class<? extends DataSource> dataSourceClass) {
        return StreamSupport.stream(DATA_SOURCES).filter(dataSourceClass::isInstance).map(dataSourceClass::cast).findAny()
                .map(dataSource -> new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).path(dataSource.getPath()).build())
                .orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public boolean onCreate() {
        return true;

    }

    @NonNull
    private <T extends DataSource> Optional<T> uriToSource(@NonNull Uri uri, Class<T> expectedClass) {
        int result = uriMatcher.match(uri);
        if (result == UriMatcher.NO_MATCH) {
            return Optional.empty();
        }
        DataSource source = DATA_SOURCES.get(result);
        return expectedClass.isInstance(source) ? Optional.of(expectedClass.cast(source)) : Optional.empty();
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return uriToSource(uri, QueryUpdateDataSource.class).map(s -> s.query(getContext(), uri, projection, selection, selectionArgs, sortOrder)).orElse(null);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {
            return uriToSource(uri, QueryUpdateDataSource.class).map(s -> s.update(getContext(), uri, values, selection, selectionArgs)).orElse(0);
        } finally {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        Mode m = exceptionToOptional((LambdaUtils.ExceptionalFunction<String, Mode, IllegalArgumentException>) Mode::valueOf).apply(mode).orElse(Mode.r);
        Optional<FileDataSource> dataSource = uriToSource(uri, FileDataSource.class);
        if (dataSource.isPresent()) {
            return ParcelFileDescriptor.open(dataSource.get().getFile(getContext()), m.getConstant());
        }
        return super.openFile(uri, mode);
    }
}
