package com.msr.bine_android.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.paging.DataSource;
import androidx.paging.PositionalDataSource;

import com.msr.bine_android.BuildConfig;
import com.msr.bine_android.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AppUtils {
    private static final String DEFAULT_FALLBACK_STORAGE_PATH = "/storage/sdcard0";
    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    public static final String IS_DOWNLOAD_START = "isDownloadStart";
    public static final String IS_DOWNLOAD_ENDED = "isDownloadEnded";
    public static final String IS_FILE_DELETED = "isFileDeleted";

    public static boolean checkIsPermissionsEnabled(Context context) {
        int locationPermission = ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION);
        int storagePermission = ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE);
        return locationPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     *
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     *
     * @param location The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static long getInternalAvailableStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        stat.restat(Environment.getDataDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        return bytesAvailable;
    }

    public static long getSDCardAvailableStorage(String path) {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        long availableSpace = -1L;
        StatFs stat = new StatFs(path);
        availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
        return availableSpace / SIZE_MB;

    }

    public static boolean externalMemoryAvailable(Context context) {
        return ContextCompat.getExternalFilesDirs(context, null).length >= 2;
    }

    static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * @return paths to all available volumes in the system (include emulated)
     */
    /*public static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectories(Context context) {
        ArrayList<StorageDirectoryParcelable> volumes;
        if (SDK_INT >= Build.VERSION_CODES.N) {
            volumes = getStorageDirectoriesNew(context);
        } else {
            volumes = getStorageDirectoriesLegacy();
        }
        return volumes;
    }*/

    /**
     * @return All available storage volumes (including internal storage, SD-Cards)
     */
    /*@TargetApi(Build.VERSION_CODES.N)
    private static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectoriesNew(Context context) {
        // Final set of paths
        ArrayList<StorageDirectoryParcelable> volumes = new ArrayList<>();
        StorageManager sm = context.getSystemService(StorageManager.class);
        assert sm != null;
        for (StorageVolume volume : sm.getStorageVolumes()) {
            if (!volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                    && !volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                continue;
            }
            File path = getVolumeDirectory(volume);
            //volumes.add(new StorageDirectoryParcelable(path.getPath()));
        }
        return volumes;
    }*/

    /**
     * Returns all available SD-Cards in the system (include emulated)
     *
     * <p>Warning: Hack! Based on Android source code of version 4.3 (API 18) Because there was no
     * standard way to get it before android N
     *
     * @return All available SD-Cards in the system (include emulated)
     */
    /*private static synchronized ArrayList<StorageDirectoryParcelable> getStorageDirectoriesLegacy() {
        List<String> rv = new ArrayList<>();

        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                // Check for actual existence of the directory before adding to list
                if (new File(DEFAULT_FALLBACK_STORAGE_PATH).exists()) {
                    rv.add(DEFAULT_FALLBACK_STORAGE_PATH);
                } else {
                    // We know nothing else, use Environment's fallback
                    rv.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String[] folders = DIR_SEPARATOR.split(path);
            final String lastFolder = folders[folders.length - 1];
            boolean isDigit = false;
            try {
                Integer.valueOf(lastFolder);
                isDigit = true;
            } catch (NumberFormatException ignored) {
            }
            rawUserId = isDigit ? lastFolder : "";
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (SDK_INT >= Build.VERSION_CODES.M) rv.clear();

        // Assign a label and icon to each directory
        ArrayList<StorageDirectoryParcelable> volumes = new ArrayList<>();
        for (String file : rv) {
            File f = new File(file);
            //volumes.add(new StorageDirectoryParcelable(file));
        }
        return volumes;
    }*/


    @TargetApi(Build.VERSION_CODES.N)
    private static File getVolumeDirectory(StorageVolume volume) {
        try {
            Field f = StorageVolume.class.getDeclaredField("mPath");
            f.setAccessible(true);
            return (File) f.get(volume);
        } catch (Exception e) {
            // This shouldn't fail, as mPath has been there in every version
            throw new RuntimeException(e);
        }
    }

    public static String getHash(String string) {
        MessageDigest digest = null;
        String hash = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(string.getBytes());

            hash = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hash;
    }

    private static String bytesToHexString(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        return fileOrDirectory.delete();
    }

    public static boolean isDeviceRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = {"/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"/system/xbin/which", "su"});
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            if (in.readLine() != null) return true;
            return false;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    public static String getUserAgent(Context context, String applicationName) {
        String versionName;
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        return applicationName + "/" + versionName + " (Linux;Android " + Build.VERSION.RELEASE
                + ") " + ExoPlayerLibraryInfo.VERSION_SLASHY;
    }


    public static @Nullable
    UUID getDrmUuid(String drmScheme) {
        switch (drmScheme) {
            case "widevine":
                return C.WIDEVINE_UUID;
            case "playready":
                return C.PLAYREADY_UUID;
            case "clearkey":
                return C.CLEARKEY_UUID;
            default:
                try {
                    return UUID.fromString(drmScheme);
                } catch (RuntimeException e) {
                    return null;
                }
        }
    }


    public static String getDeviceLocale(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        return locale.getLanguage();
    }

   /* public static String getFilterForSelectedLocale(Context context, String filter) {
        if (context.getString(R.string.category_all).equals(filter)) {
            return Category.ALL.name();
        } else if (context.getString(R.string.category_hindi).equals(filter)) {
            return Category.HINDI.name();
        } else if (context.getString(R.string.category_kannada).equals(filter)) {
            return Category.KANNADA.name();
        } else if (context.getString(R.string.category_tamil).equals(filter)) {
            return Category.TAMIL.name();
        } else if (context.getString(R.string.category_english).equals(filter)) {
            return Category.ENGLISH.name();
        } else if (context.getString(R.string.category_bhojpuri).equals(filter)) {
            return Category.BHOJPURI.name();
        } else if (context.getString(R.string.category_telugu).equals(filter)) {
            return Category.TELUGU.name();
        }
        return Category.ALL.name();
    }*/

   /* public static void showIpConnectionDialog(SettingsActivity settingsActivity, DialogClickCancelListener dialogClickCancelListener, UserSetting settingsModel) {
        final Dialog dialog = new Dialog(settingsActivity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ipaddress);
        EditText inputIpAddress = dialog.findViewById(R.id.idInput);
        TextView btnOkay = dialog.findViewById(R.id.idBtnOk);
        TextView btnCancel = dialog.findViewById(R.id.idBtnCancel);
        inputIpAddress.setText(settingsModel.value);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = inputIpAddress.getText().toString().trim();
                dialogClickCancelListener.onOkClicked(ipAddress, dialog);
            }
        });
        dialog.show();
    }

    public static void showSSIDDialog(SettingsActivity settingsActivity,
                                      UserSetting settingsModel,
                                      DialogClickCancelListener dialogClickCancelListener) {
        final Dialog dialog = new Dialog(settingsActivity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ssid);
        TextView label = dialog.findViewById(R.id.idTitle);
        label.setText(settingsModel.title);
        EditText inputSSID = dialog.findViewById(R.id.idInput);
        inputSSID.setHint(settingsModel.title);
        TextView btnOkay = dialog.findViewById(R.id.idBtnOk);
        TextView btnCancel = dialog.findViewById(R.id.idBtnCancel);
        inputSSID.setText(settingsModel.value);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = inputSSID.getText().toString().trim();
                dialogClickCancelListener.onOkClicked(ssid, dialog);
            }
        });
        dialog.show();
    }


    public static void showDownloadCountDialog(SettingsActivity settingsActivity,
                                      UserSetting settingsModel,
                                      DialogClickCancelListener dialogClickCancelListener) {
        final Dialog dialog = new Dialog(settingsActivity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ssid);
        TextView label = dialog.findViewById(R.id.idTitle);
        label.setText("Thread Count");
        EditText inputSSID = dialog.findViewById(R.id.idInput);
        inputSSID.setInputType(InputType.TYPE_CLASS_NUMBER);
        inputSSID.setHint("Number of threads used to download segments");
        TextView btnOkay = dialog.findViewById(R.id.idBtnOk);
        TextView btnCancel = dialog.findViewById(R.id.idBtnCancel);
        inputSSID.setText(settingsModel.value);
        btnCancel.setOnClickListener(v -> dialog.cancel());
        btnOkay.setOnClickListener(v -> {
            String count = inputSSID.getText().toString().trim();
            dialogClickCancelListener.onOkClicked(count, dialog);
        });
        dialog.show();
    }

    public static void showLogoutDialog(Activity settingsActivity, DialogClickCancelListener dialogClickCancelListener) {
        final Dialog dialog = new Dialog(settingsActivity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_logout);
        TextView btnOkay = dialog.findViewById(R.id.idBtnOk);
        TextView btnCancel = dialog.findViewById(R.id.idBtnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickCancelListener.onOkClicked("Logout", dialog);
            }
        });
        dialog.show();
    }

    public static void showLanguageSelectionDialog(Activity activity, String language, DialogClickCancelListener dialogClickCancelListener) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_language_selection);
        Button btnOkay = dialog.findViewById(R.id.idBtnOk);
        Button btnCancel = dialog.findViewById(R.id.idBtnCancel);
        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        RadioButton radioButtonEnglish = dialog.findViewById(R.id.radioEnglish);
        RadioButton radioButtonHindi = dialog.findViewById(R.id.radioHindi);
        RadioButton radioButtonKannada = dialog.findViewById(R.id.radioKannada);
        final String[] selectedLanguage = new String[1];
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClickCancelListener.onOkClicked(selectedLanguage[0], dialog);
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    String language = rb.getText().toString();
                    if (language.equalsIgnoreCase(activity.getResources().getString(R.string.str_english))) {
                        selectedLanguage[0] = "en";
                    } else if (language.equalsIgnoreCase(activity.getResources().getString(R.string.str_hindi))) {
                        selectedLanguage[0] = "hi";
                    } else {
                        selectedLanguage[0] = "kn";
                    }
                }
            }
        });
        switch (language) {
            case "en":
                selectedLanguage[0] = activity.getResources().getString(R.string.str_english);
                radioButtonEnglish.setChecked(true);
                break;
            case "hi":
                selectedLanguage[0] = activity.getResources().getString(R.string.str_hindi);
                radioButtonHindi.setChecked(true);
                break;
            case "kn":
                selectedLanguage[0] = activity.getResources().getString(R.string.str_kannada);
                radioButtonKannada.setChecked(true);
                break;
        }
        dialog.show();
    }*/

    /*public static void showReferralCodeDialog(Activity activity, DialogClickCancelListener dialogClickCancelListener) {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_ipaddress);
        EditText inputIpAddress = dialog.findViewById(R.id.idInput);
        inputIpAddress.setMaxEms(6);
        inputIpAddress.setAllCaps(true);
        inputIpAddress.setHint(R.string.referral_code);
        TextView title = dialog.findViewById(R.id.idTitle);
        title.setText(R.string.enter_referral_code);
        TextView btnOkay = dialog.findViewById(R.id.idBtnOk);
        TextView btnCancel = dialog.findViewById(R.id.idBtnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = inputIpAddress.getText().toString().trim();
                dialogClickCancelListener.onOkClicked(ipAddress,dialog);
            }
        });
        dialog.show();
    }*/

    public static String languageForLocale(String locale, Context context) {
        switch (locale) {
            case "en":
                return context.getResources().getString(R.string.str_english);
            case "hi":
                return "Hindi";
            case "kn":
                return "Kannada";
        }
        return "en";
    }

    public static void showShareDialog(Activity activity, String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                "To watch latest movies in high quality without incurring any data costs please download the Mishtu Application from playstore: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    /*public static Intent getNextIntent(Context context, DataRepository dataRepository) {
        String token = dataRepository.getUserId();
        if (TextUtils.isEmpty(token)) {
            return new Intent(context, LoginActivity.class);
        } else if (!dataRepository.isTncAccepted()) {
            return new Intent(context, TermsAndConditionsActivity.class);
        } else if (!AppUtils.checkIsPermissionsEnabled(context)) {
            return new Intent(context, PermissionsActivity.class);
        } else {
            return new Intent(context, MainActivity.class);
        }
    }*/

    public static boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
            int mode = Settings.Secure.getInt(
                    context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF
            );
            return mode != Settings.Secure.LOCATION_MODE_OFF;
        }
    }

    //https://stackoverflow.com/questions/54489506/recyclerview-scrolls-down-on-data-update-using-room-and-paginglistadapter/58048254#58048254
    public static class RoomFactoryWrapper<T> extends DataSource.Factory<Integer, T> {
        final DataSource.Factory<Integer, T> m_wrappedFactory;

        public RoomFactoryWrapper(@NonNull DataSource.Factory<Integer, T> wrappedFactory) {
            m_wrappedFactory = wrappedFactory;
        }

        @NonNull
        @Override
        public DataSource<Integer, T> create() {
            return new DataSourceWrapper<>((PositionalDataSource<T>) m_wrappedFactory.create());
        }

        static class DataSourceWrapper<T> extends PositionalDataSource<T> {
            final PositionalDataSource<T> m_wrappedSource;

            DataSourceWrapper(PositionalDataSource<T> wrappedSource) {
                m_wrappedSource = wrappedSource;
            }

            @Override
            public void addInvalidatedCallback(@NonNull InvalidatedCallback onInvalidatedCallback) {
                m_wrappedSource.addInvalidatedCallback(onInvalidatedCallback);
            }

            @Override
            public void removeInvalidatedCallback(
                    @NonNull InvalidatedCallback onInvalidatedCallback) {
                m_wrappedSource.removeInvalidatedCallback(onInvalidatedCallback);
            }

            @Override
            public void invalidate() {
                m_wrappedSource.invalidate();
            }

            @Override
            public boolean isInvalid() {
                return m_wrappedSource.isInvalid();
            }

            @Override
            public void loadInitial(@NonNull LoadInitialParams params,
                                    @NonNull LoadInitialCallback<T> callback) {
                // Workaround for paging bug: https://issuetracker.google.com/issues/123834703
                // edit initial load position to start 1/2 load ahead of requested position
                int newStartPos = params.placeholdersEnabled
                        ? params.requestedStartPosition
                        : Math.max(0, params.requestedStartPosition - (params.requestedLoadSize / 2));
                m_wrappedSource.loadInitial(new LoadInitialParams(
                        newStartPos,
                        params.requestedLoadSize,
                        params.pageSize,
                        params.placeholdersEnabled
                ), callback);
            }

            @Override
            public void loadRange(@NonNull LoadRangeParams params,
                                  @NonNull LoadRangeCallback<T> callback) {
                m_wrappedSource.loadRange(params, callback);
            }
        }
    }

    private static final long MEGABYTE = 1024L * 1024L;


    public static float bytesToMB(float bytes) {
        return bytes / MEGABYTE;
    }

    public static int secondsToMinute(int seconds) {
        return seconds / 60;
    }

    public static String formatHoursAndMinutes(int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + " hr " + minutes + " min";
    }

    /*public static void showDialogDeleteOrder(Context context, String imageUrl, OnOrderDeleteDialogListeners onDialogClickListener) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.setContentView(R.layout.dialog_delete_order);
        TextView btnKeep = dialog.findViewById(R.id.btnKeep);
        TextView btnDelete = dialog.findViewById(R.id.btnDelete);
        ImageView imgBanner=dialog.findViewById(R.id.imgBanner);
        Glide.with(context)
                .load(imageUrl)
                .centerCrop()
                .into(imgBanner);

        btnDelete.setOnClickListener(v -> {
            onDialogClickListener.onDialogDeleteClicked();
            dialog.dismiss();
        });

        btnKeep.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }*/
}
