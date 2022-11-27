package com.microsoft.mobile.polymer.mishtu.kaizala_utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.View;

import androidx.annotation.Keep;

import com.microsoft.mobile.polymer.mishtu.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Keep
public final class LanguageUtils {

    /**
     * Controls whether localization should be enabled
     */
    public static final boolean LOCALIZATION_ENABLED = true;

    /**
     * To add new languages, add the language code in LangSupported enum.
     * If the language code has country code specified too, add only the language code part(i.e., 'pt' in cae of 'pt-BR') in the list defined in doesAppSpecifyCountryForLang() method.
     */

    private static final int DEVICE_LANG_PREF_START_INDEX = 1;
    //index of the language code on splitting concatenated string of language and country code
    private static final int LANG_CODE_SPLIT_INDEX = 0;
    //index of the country code on splitting concatenated string of language and country code
    private static final int COUNTRY_CODE_SPLIT_INDEX = 1;
    //size of array obtained on splitting concatenated string of language and country code
    private static final int LANG_COUNTRY_CODE_SPLIT_SIZE = 2;
    private static final String LANG_COUNTRY_CODE_SEPARATOR = "-";

    private static Configuration sConfiguration;

    /* Languages with country specified are mentioned as single string with language and country code concatenated by '_' (eg, pt_br) */
    private enum LangSupported {
        en(1),
        hi(2),
        te(3),
        ta(12),
        kn(45),
        ml(46),
        bn(8),
        pa_IN(22),
        gu(10),
        mr(11),
        as(12),
        kon(13),
        or(14);

        /*fil(4),
        id(5),
        pt_BR(6),
        sw(7),
        es(9),
        th(13),
        tr(14),
        vi(15),
        zh_CN(16),
        fr(17),
        de(18),
        bg(19),
        nl(20),
        pt_PT(21),
        ro(23),
        sr(24),
        uk(25),
        ru(26),
        ja(27),
        zh_TW(28),
        hr(29),
        cs(30),
        da(31),
        fi(32),
        el(33),
        hu(34),
        it(35),
        nb(36),
        ko(37),
        pl(38),
        sk(39),
        sv(40),
        et(41),
        lv(42),
        lt(43),
        sl(44),
        ar(47),
        he(48);*/

        private int langValue;
        LangSupported(int langValue) {
            this.langValue = langValue;
        }

        public String getName() {
            // this is done as '-' can't be used while declaring enums, but is needed for locale name
            return name().replace("_", LANG_COUNTRY_CODE_SEPARATOR);
        }
    }

    // Add languages to list for which country is specified in app
    private static boolean doesAppSpecifyCountryForLang(String lang) {
        return (Arrays.asList("pt", "bn", "zh", "pa")).contains(lang);
    }

    /* The language codes "he", "yi", and "id" are mapped internally to their old codes "iw",
     * "ji", and "in" respectively for backward compatibility. On calling getLanguage() for these locales, the old code is returned.
     * Hence adding this method to get the latest language code for the internal old code.
     * Only supporting "id" right now, so adding a case only for that.
     */
    private static String getLatestLanguageCode(String langCode) {
        switch(langCode) {
            case "in":
                return "id";
            case "iw":
                return "he";
            default:
                return langCode;
        }
    }

    public static List<String> getLanguages() {
        List<String> languages = new ArrayList<>();
        //Adding device language
        languages.add("_" + getDeviceLanguage());
        for (LangSupported lang : LangSupported.values()) {
            languages.add(lang.getName());
        }
        return languages;
    }


    public static List<String> getLanguageCodesForLanguageScreenInFRE() {
        List<String> languages = new ArrayList<>();
        for (LangSupported lang : LangSupported.values()) {
            languages.add(lang.getName());
        }
        return languages;
    }

    public static String getDeviceLanguage() {
        String lang = getLatestLanguageCode(getDeviceLocale().getLanguage());
        if (doesAppSpecifyCountryForLang(lang)) {
            lang = addCountryCode(lang, getDeviceLocale().getCountry());
        }
        return lang;
    }

    private static Locale getDeviceLocale() {
        return Resources.getSystem().getConfiguration().locale;
    }

    public static String getDeviceDisplayLanguage() {
        return getLocaleDisplayTitle(getDeviceLanguage());
    }

    public static String getAppDisplayLanguage(String lang) {
        return getLocaleDisplayTitle(lang);
    }

    private static String getLocaleDisplayTitle(String lang) {
        Locale locale = createLocale(lang);
        return isCountrySpecificLanguage(lang) ? locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")"  : locale.getDisplayLanguage(locale);
    }

    public static String getAppLanguagePreference(SharedPreferences sharedPreferences, String key) {
        return sharedPreferences.getString(key, null);
    }

    public static void setAppLanguagePreference(SharedPreferences sharedPreferences, String key, String langValue) {
        sharedPreferences.edit().putString(key, langValue).apply();
    }

    /*public static Configuration getConfiguration() {
        if (sConfiguration == null) {
            setAndUpdateAppLocale();
        }
        return sConfiguration;
    }
*/
    private static void updateAppLocale(Context context, String lang) {
        Locale locale = createLocale(lang);
        Locale.setDefault(locale);
        Configuration config = context.getResources().getConfiguration();
        config.setLocale(locale);
        sConfiguration = config;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public static boolean isDeviceLanguageSupported() {
        String deviceLang = getDeviceLanguage();
        for (LangSupported lang : LangSupported.values()) {
            if (lang.getName().equals(deviceLang)) {
                return true;
            }
        }
        return false;
    }

    public static String getDeviceOrFallbackLanguage(String fallbackLang) {
        return isDeviceLanguageSupported() ? getDeviceLanguage() : fallbackLang;
    }

    private static String findAndSetLangPreferenceValue(SharedPreferences sharedPreferences, String langPreferenceKey, String fallBackLang) {
        String lang = LanguageUtils.getDeviceOrFallbackLanguage(fallBackLang);
        String langValue;
        //Persist device language, and in case the device language isn't supported, fallback to the last held language(or English in case of first time app start)
        if (lang.equals(LanguageUtils.getDeviceLanguage())) {
            langValue = "_" + lang;
        } else {
            langValue = lang;
        }
        setAppLanguagePreference(sharedPreferences, langPreferenceKey, langValue);
        return lang;
    }

    /*public static void setAndUpdateAppLocaleIfNeeded(Context context) {
        if (!getDefaultLocaleCode().equals(getAppLanguage(context))) {
            setAndUpdateAppLocale(context);
        }
    }
    public static void setAndUpdateAppLocale(Context context) {
        String lang;
        if(LOCALIZATION_ENABLED) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            String langPreferenceKey = context.getString(R.string.settings_key_language);
            lang = getAppLanguagePreference(sharedPreferences, langPreferenceKey);
            lang = findApplicableLangPreference(sharedPreferences, langPreferenceKey, lang);
        } else {
            lang = getDefaultLanguage();
        }
        updateAppLocale(context, lang);
    }*/

    public static String findApplicableLangPreference(SharedPreferences sharedPreferences, String langPreferenceKey, String lang) {
        // If first time app start, else if preference is phone's language
        if(lang == null) {
            lang = findAndSetLangPreferenceValue(sharedPreferences, langPreferenceKey, getDefaultLanguage());
        } else if(isDeviceLangPreference(lang)) {
            lang = findAndSetLangPreferenceValue(sharedPreferences, langPreferenceKey, scrubDeviceLangPreferenceValue(lang));
        }
        return lang;
    }

    /*public static void displayToastOnLangChange(final Activity activity) {
        String displayString = getRestartAppDisplayString(activity);
        ViewUtilities.runOnUI(activity,new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, displayString, Toast.LENGTH_LONG).show();
            }
        });
    }*/

    /*public static String getRestartAppDisplayString(final Activity activity) {
        String langValue = getAppLanguagePreference(PreferenceManager.getDefaultSharedPreferences(activity), activity.getString(R.string.settings_key_language));
        boolean isDeviceLangAvailable = true;
        if(isDeviceLangPreference(langValue)) {
            langValue = getDeviceOrFallbackLanguage(scrubDeviceLangPreferenceValue(langValue));
            if (!langValue.equals(getDeviceLanguage()))
                isDeviceLangAvailable = false;
        }
        Resources res = activity.getResources();
        Configuration conf = res.getConfiguration();
        Locale savedLocale = conf.locale;
        conf.setLocale(createLocale(langValue));
        res.updateConfiguration(conf, null);
        final String displayString;
        if(isDeviceLangAvailable) {
            displayString = activity.getString(R.string.restart_app_on_language_change);
        } else {
            displayString = String.format(activity.getString(R.string.restart_app_on_language_not_supported), getDeviceDisplayLanguage());
        }

        conf.setLocale(savedLocale);
        res.updateConfiguration(conf, null);
        return displayString;
    }*/

    /*@Keep
    public static String getAppLanguage(Context context) {
        String lang = getDefaultLanguage();
        if (LOCALIZATION_ENABLED) {
            if(context != null) {
                lang = getAppLanguagePreference(PreferenceManager.getDefaultSharedPreferences(context), context.getString(R.string.settings_key_language));
                if (lang == null) {
                    lang = getDefaultLanguage();
                } else if (isDeviceLangPreference(lang)) {
                    lang = scrubDeviceLangPreferenceValue(lang);
                }
            }
        }
        return lang;
    }*/

//    public static Locale getAppLocale() {
//        return createLocale(getAppLanguage());
//    }

    public static boolean isDeviceLangPreference(String langValue) {
        return langValue.startsWith("_");
    }

    private static String scrubDeviceLangPreferenceValue(String langValue) {
        // remove '_' at index 0 of device language
        return langValue.substring(DEVICE_LANG_PREF_START_INDEX);
    }

    private static boolean isCountrySpecificLanguage(String langValue) {
        //If it is a country specific language, it would be of the form 'pt_br'. On splitting, we'll get strings 'pt' and 'br'
        return langValue.split(LANG_COUNTRY_CODE_SEPARATOR).length >=  LANG_COUNTRY_CODE_SPLIT_SIZE;
    }

    private static String addCountryCode(String lang, String countryCode) {
        return lang.concat(LANG_COUNTRY_CODE_SEPARATOR).concat(countryCode);
    }

    private static Locale createLocale(String langValue) {
        if(! isCountrySpecificLanguage(langValue)) {
            return new Locale(langValue, getDeviceLocale().getCountry());
        }
        // splitting langValue to get language code and country code.
        String[] langParts = langValue.split(LANG_COUNTRY_CODE_SEPARATOR);
        return new Locale(langParts[LANG_CODE_SPLIT_INDEX], langParts[COUNTRY_CODE_SPLIT_INDEX]);
    }

    public static String getDefaultLanguage() {
        return LangSupported.en.getName();
    }

    /**
     * @return Returns the default locale using different method parameters based on app version.
     */
    public static Locale getDefaultLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Locale.getDefault(Locale.Category.DISPLAY);
        }
        return Locale.getDefault();
    }

    /* For internal use, not to be made public */
    private static String getDefaultLocaleCode() {
        Locale defaultLocale = getDefaultLocale();
        String defaultLangCode = defaultLocale.getLanguage();
        return doesAppSpecifyCountryForLang(defaultLangCode) ? addCountryCode(defaultLangCode, defaultLocale.getCountry()) : defaultLangCode;
    }

    public static String getLanguageCode(String name) {
        switch (name) {
            case "Hindi": return "hi";
            case "Tamil": return "ta";
            case "Telugu": return "te";
            case "Kannada": return "Kn";
            case "Malayalam": return "ml";
            case "Bengali": return "bn";
            case "Punjabi": return "pa";
            case "Gujarati": return "gu";
            case "Marathi": return "mr";
            case "Assamese": return "as";
            case "Konkani": return "kok";
            case "Odia": return "or";
            case "English":
            default: return "en";
        }
    }
    /*public static boolean isRtlLayout() {
        Context context = ContextContainer.getContext();
        Configuration config = context.getResources().getConfiguration();
        return (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }*/

    /* Used to isolate the directionality of string so that the bidirectional algorithm
    displays it correctly irrespective of the context's directionality in which it is used.
    Specifically, we have used this to correctly display phone numbers as phone numbers with a space
    in between would be treated as separate words and displayed in wrong order in RTL
     */
    /*public static String getDirectionalIsolate(String string) {
        return BidiFormatter.getInstance(isRtlLayout()).unicodeWrap(string);
    }*/

    public static boolean isRtlLayout(Context context) {
        Configuration config = context.getResources().getConfiguration();
        return (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }
}