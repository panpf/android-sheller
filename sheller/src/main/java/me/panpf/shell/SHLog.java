package me.panpf.shell;

import android.util.Log;

import androidx.annotation.NonNull;

class SHLog {
    public static void d(@NonNull String format, @NonNull Object... args) {
        Log.d(Sheller.TAG, String.format(format, args));
    }

    public static void d(@NonNull String msg) {
        Log.d(Sheller.TAG, msg);
    }

    public static void i(@NonNull String format, @NonNull Object... args) {
        Log.i(Sheller.TAG, String.format(format, args));
    }

    public static void i(@NonNull String msg) {
        Log.i(Sheller.TAG, msg);
    }

    public static void w(@NonNull String format, @NonNull Object... args) {
        Log.w(Sheller.TAG, String.format(format, args));
    }

    public static void w(@NonNull String msg) {
        Log.w(Sheller.TAG, msg);
    }

    public static void e(@NonNull String format, @NonNull Object... args) {
        Log.e(Sheller.TAG, String.format(format, args));
    }

    public static void e(@NonNull String msg) {
        Log.e(Sheller.TAG, msg);
    }
}
