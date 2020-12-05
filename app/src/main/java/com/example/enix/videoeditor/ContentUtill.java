package com.example.enix.videoeditor;

import android.database.Cursor;

public class ContentUtill {
    public static long getLong(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
    }

    public static String getTime(Cursor cursor, String columnName) {
        return TimeUtils.toFormattedTime(getInt(cursor, columnName));
    }

    public static int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }
}
