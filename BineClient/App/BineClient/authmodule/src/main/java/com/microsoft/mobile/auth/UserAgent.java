package com.microsoft.mobile.auth;

import android.os.Build;

import androidx.annotation.Keep;

public class UserAgent {
    @Keep
    public static String getUserAgent() {

        return String.format("ZUMO (lang=%s; os=%s; os_version=%s; arch=%s)", "Java", "Android", Build.VERSION.RELEASE,
                Build.CPU_ABI);
    }
}
