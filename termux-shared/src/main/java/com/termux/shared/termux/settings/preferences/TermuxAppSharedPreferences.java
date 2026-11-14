package com.termux.shared.termux.settings.preferences;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.shared.android.PackageUtils;
import com.termux.shared.settings.preferences.AppSharedPreferences;
import com.termux.shared.settings.preferences.SharedPreferenceUtils;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.logger.Logger;
import com.termux.shared.data.DataUtils;
import com.termux.shared.termux.TermuxUtils;
import com.termux.shared.termux.settings.preferences.TermuxPreferenceConstants.TERMUX_APP;
import com.termux.shared.errors.Error;
import com.termux.shared.file.FileUtils;
import com.termux.shared.termux.settings.properties.TermuxPropertyConstants;

public class TermuxAppSharedPreferences extends AppSharedPreferences {

    private int MIN_FONTSIZE;
    private int MAX_FONTSIZE;
    private int DEFAULT_FONTSIZE;

    private static final String LOG_TAG = "TermuxAppSharedPreferences";

    private TermuxAppSharedPreferences(@NonNull Context context) {
        super(context,
            SharedPreferenceUtils.getPrivateSharedPreferences(context,
                TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION),
            SharedPreferenceUtils.getPrivateAndMultiProcessSharedPreferences(context,
                TermuxConstants.TERMUX_DEFAULT_PREFERENCES_FILE_BASENAME_WITHOUT_EXTENSION));

        setFontVariables(context);
    }

    /**
     * Get {@link TermuxAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link TermuxConstants#TERMUX_PACKAGE_NAME}.
     * @return Returns the {@link TermuxAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    @Nullable
    public static TermuxAppSharedPreferences build(@NonNull final Context context) {
        Context termuxPackageContext = PackageUtils.getContextForPackage(context, TermuxConstants.TERMUX_PACKAGE_NAME);
        if (termuxPackageContext == null)
            return null;
        else
            return new TermuxAppSharedPreferences(termuxPackageContext);
    }

    /**
     * Get {@link TermuxAppSharedPreferences}.
     *
     * @param context The {@link Context} to use to get the {@link Context} of the
     *                {@link TermuxConstants#TERMUX_PACKAGE_NAME}.
     * @param exitAppOnError If {@code true} and failed to get package context, then a dialog will
     *                       be shown which when dismissed will exit the app.
     * @return Returns the {@link TermuxAppSharedPreferences}. This will {@code null} if an exception is raised.
     */
    public static TermuxAppSharedPreferences build(@NonNull final Context context, final boolean exitAppOnError) {
        Context termuxPackageContext = TermuxUtils.getContextForPackageOrExitApp(context, TermuxConstants.TERMUX_PACKAGE_NAME, exitAppOnError);
        if (termuxPackageContext == null)
            return null;
        else
            return new TermuxAppSharedPreferences(termuxPackageContext);
    }



    public boolean shouldShowTerminalToolbar() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_SHOW_TERMINAL_TOOLBAR, TERMUX_APP.DEFAULT_VALUE_SHOW_TERMINAL_TOOLBAR);
    }

    public void setShowTerminalToolbar(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_SHOW_TERMINAL_TOOLBAR, value, false);
    }

    public boolean toogleShowTerminalToolbar() {
        boolean currentValue = shouldShowTerminalToolbar();
        setShowTerminalToolbar(!currentValue);
        return !currentValue;
    }



    public boolean isTerminalMarginAdjustmentEnabled() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_MARGIN_ADJUSTMENT, TERMUX_APP.DEFAULT_TERMINAL_MARGIN_ADJUSTMENT);
    }

    public void setTerminalMarginAdjustment(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_MARGIN_ADJUSTMENT, value, false);
    }



    public boolean isSoftKeyboardEnabled() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_SOFT_KEYBOARD_ENABLED, TERMUX_APP.DEFAULT_VALUE_KEY_SOFT_KEYBOARD_ENABLED);
    }

    public void setSoftKeyboardEnabled(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_SOFT_KEYBOARD_ENABLED, value, false);
    }

    public boolean isSoftKeyboardEnabledOnlyIfNoHardware() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_SOFT_KEYBOARD_ENABLED_ONLY_IF_NO_HARDWARE, TERMUX_APP.DEFAULT_VALUE_KEY_SOFT_KEYBOARD_ENABLED_ONLY_IF_NO_HARDWARE);
    }

    public void setSoftKeyboardEnabledOnlyIfNoHardware(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_SOFT_KEYBOARD_ENABLED_ONLY_IF_NO_HARDWARE, value, false);
    }

    public void setVirtualVolumeKeysDisabled(boolean value) {
        String path = TermuxConstants.TERMUX_PROPERTIES_PRIMARY_FILE_PATH;
        String newValue = value ? "volume" : "virtual";
        StringBuilder sb = new StringBuilder();
        Error error = FileUtils.readTextFromFile(null, path, null, sb, true);
        if (error != null) {
            Logger.logError(LOG_TAG, "Failed to read properties: " + error.getMessage());
            return;
        }
        String content = sb.toString();
        String regex = "(?m)^\\s*#?\\s*volume-keys\\s*=.*$";
        String replacement = "volume-keys = " + newValue;
        if (content.matches("(?s).*" + regex + ".*")) {
            content = content.replaceAll(regex, replacement);
        } else {
            if (!content.endsWith("\n")) content += "\n";
            content += replacement + "\n";
        }
        error = FileUtils.writeTextToFile(null, path, null, content, false);
        if (error != null) {
            Logger.logError(LOG_TAG, "Failed to write properties: " + error.getMessage());
        }
    }



    public boolean areVirtualVolumeKeysDisabled() {
        String path = TermuxConstants.TERMUX_PROPERTIES_PRIMARY_FILE_PATH;
        StringBuilder sb = new StringBuilder();
        Error error = FileUtils.readTextFromFile(null, path, null, sb, true);
        if (error != null) return false;
        String content = sb.toString();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?m)^\\s*volume-keys\\s*=\\s*(\\S+)").matcher(content);
        if (m.find()) {
            return "volume".equals(m.group(1).trim());
        }
        return false;
    }

    public void updateExtraKeys() {
        // Настройки видимости (по умолчанию: true для тех, что видны, false для скрытых)
        boolean showPaste = SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_paste_key", true);
        boolean showCopy = SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_copy_all_key", true);
        boolean showClearPaste = SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_clear_paste_key", true);
        boolean useShort = SharedPreferenceUtils.getBoolean(mSharedPreferences, "use_short_names", true);
        
        // Настройки для скрытых по умолчанию (стрелки, TAB, HOME, END)
        boolean showArrows = SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_arrows", false);
        boolean showTabHomeEnd = SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_tab_home_end", false);

        String p = useShort ? "Pst" : "PASTE";
        String c = useShort ? "Cp_All" : "COPY_ALL";
        String cl = useShort ? "Pst_Clr" : "CLEAR_AND_PASTE";

        // 1-й ряд: ESC, CTRL, ALT, (стрелки), PST, MNU
        StringBuilder row1 = new StringBuilder("[");
        row1.append("'ESC','CTRL','ALT'");
        if (showArrows) row1.append(",'UP','DOWN','LEFT','RIGHT'");
        if (showPaste) row1.append(",'").append(p).append("'");
        row1.append(",'Mnu']");

        // 2-й ряд: CP_ALL, PST_CLR, (TAB/HOME/END), PGUP, PGDN
        StringBuilder row2 = new StringBuilder("[");
        boolean first = true;
        if (showCopy) { row2.append("'").append(c).append("'"); first = false; }
        if (showClearPaste) { if (!first) row2.append(","); row2.append("'").append(cl).append("'"); first = false; }
        if (showTabHomeEnd) { 
            if (!first) row2.append(","); 
            row2.append("'TAB','HOME','END'"); 
            first = false; 
        }
        if (!first) row2.append(",");
        row2.append("'PGUP','PGDN']");

        String extraKeys = "[" + row1.toString() + ", " + row2.toString() + "]";
        
        String path = TermuxConstants.TERMUX_PROPERTIES_PRIMARY_FILE_PATH;
        StringBuilder sb = new StringBuilder();
        Error error = FileUtils.readTextFromFile(null, path, null, sb, true);
        if (error != null) return;
        String propContent = sb.toString();
        
        // Заменяем extra-keys
        String regex = "(?m)^\\s*#?\\s*extra-keys\\s*=.*$";
        String replacement = "extra-keys = " + extraKeys;
        if (propContent.matches("(?s).*" + regex + ".*")) {
            propContent = propContent.replaceAll(regex, replacement);
        } else {
            if (!propContent.endsWith("\n")) propContent += "\n";
            propContent += replacement + "\n";
        }
        
        // Заменяем стиль на all (чтобы не было иконок)
        String styleRegex = "(?m)^\\s*#?\\s*extra-keys-style\\s*=.*$";
        String styleReplacement = "extra-keys-style = all";
        if (propContent.matches("(?s).*" + styleRegex + ".*")) {
            propContent = propContent.replaceAll(styleRegex, styleReplacement);
        } else {
            if (!propContent.endsWith("\n")) propContent += "\n";
            propContent += styleReplacement + "\n";
        }
        
        FileUtils.writeTextToFile(null, path, null, propContent, false);
    }

    public void setShowPasteKey(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "show_paste_key", value, false);
        updateExtraKeys();
    }
    public void setShowCopyAllKey(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "show_copy_all_key", value, false);
        updateExtraKeys();
    }
    public void setShowClearPasteKey(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "show_clear_paste_key", value, false);
        updateExtraKeys();
    }

    public void setUseShortNames(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "use_short_names", value, false);
        updateExtraKeys();
    }
    public boolean isUseShortNames() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "use_short_names", true);
    }

    public void setShowArrows(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "show_arrows", value, false);
        updateExtraKeys();
    }
    public boolean isShowArrows() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_arrows", false);
    }
    public void setShowTabHomeEnd(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, "show_tab_home_end", value, false);
        updateExtraKeys();
    }
    public boolean isShowTabHomeEnd() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_tab_home_end", false);
    }

    public boolean isShowPasteKey() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_paste_key", true);
    }
    public boolean isShowCopyAllKey() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_copy_all_key", true);
    }
    public boolean isShowClearPasteKey() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, "show_clear_paste_key", true);
    }

    public boolean shouldKeepScreenOn() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_KEEP_SCREEN_ON, TERMUX_APP.DEFAULT_VALUE_KEEP_SCREEN_ON);
    }

    public void setKeepScreenOn(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_KEEP_SCREEN_ON, value, false);
    }



    public static int[] getDefaultFontSizes(Context context) {
        float dipInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics());

        int[] sizes = new int[3];

        // This is a bit arbitrary and sub-optimal. We want to give a sensible default for minimum font size
        // to prevent invisible text due to zoom be mistake:
        sizes[1] = (int) (4f * dipInPixels); // min

        // http://www.google.com/design/spec/style/typography.html#typography-line-height
        int defaultFontSize = Math.round(12 * dipInPixels);
        // Make it divisible by 2 since that is the minimal adjustment step:
        if (defaultFontSize % 2 == 1) defaultFontSize--;

        sizes[0] = defaultFontSize; // default

        sizes[2] = 256; // max

        return sizes;
    }

    public void setFontVariables(Context context) {
        int[] sizes = getDefaultFontSizes(context);

        DEFAULT_FONTSIZE = sizes[0];
        MIN_FONTSIZE = sizes[1];
        MAX_FONTSIZE = sizes[2];
    }

    public int getFontSize() {
        int fontSize = SharedPreferenceUtils.getIntStoredAsString(mSharedPreferences, TERMUX_APP.KEY_FONTSIZE, DEFAULT_FONTSIZE);
        return DataUtils.clamp(fontSize, MIN_FONTSIZE, MAX_FONTSIZE);
    }

    public void setFontSize(int value) {
        SharedPreferenceUtils.setIntStoredAsString(mSharedPreferences, TERMUX_APP.KEY_FONTSIZE, value, false);
    }

    public void changeFontSize(boolean increase) {
        int fontSize = getFontSize();

        fontSize += (increase ? 1 : -1) * 2;
        fontSize = Math.max(MIN_FONTSIZE, Math.min(fontSize, MAX_FONTSIZE));

        setFontSize(fontSize);
    }



    public String getCurrentSession() {
        return SharedPreferenceUtils.getString(mSharedPreferences, TERMUX_APP.KEY_CURRENT_SESSION, null, true);
    }

    public void setCurrentSession(String value) {
        SharedPreferenceUtils.setString(mSharedPreferences, TERMUX_APP.KEY_CURRENT_SESSION, value, false);
    }



    public int getLogLevel() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, TERMUX_APP.KEY_LOG_LEVEL, Logger.DEFAULT_LOG_LEVEL);
    }

    public void setLogLevel(Context context, int logLevel) {
        logLevel = Logger.setLogLevel(context, logLevel);
        SharedPreferenceUtils.setInt(mSharedPreferences, TERMUX_APP.KEY_LOG_LEVEL, logLevel, false);
    }



    public int getLastNotificationId() {
        return SharedPreferenceUtils.getInt(mSharedPreferences, TERMUX_APP.KEY_LAST_NOTIFICATION_ID, TERMUX_APP.DEFAULT_VALUE_KEY_LAST_NOTIFICATION_ID);
    }

    public void setLastNotificationId(int notificationId) {
        SharedPreferenceUtils.setInt(mSharedPreferences, TERMUX_APP.KEY_LAST_NOTIFICATION_ID, notificationId, false);
    }


    public synchronized int getAndIncrementAppShellNumberSinceBoot() {
        // Keep value at MAX_VALUE on integer overflow and not 0, since not first shell
        return SharedPreferenceUtils.getAndIncrementInt(mSharedPreferences, TERMUX_APP.KEY_APP_SHELL_NUMBER_SINCE_BOOT,
            TERMUX_APP.DEFAULT_VALUE_APP_SHELL_NUMBER_SINCE_BOOT, true, Integer.MAX_VALUE);
    }

    public synchronized void resetAppShellNumberSinceBoot() {
        SharedPreferenceUtils.setInt(mSharedPreferences, TERMUX_APP.KEY_APP_SHELL_NUMBER_SINCE_BOOT,
            TERMUX_APP.DEFAULT_VALUE_APP_SHELL_NUMBER_SINCE_BOOT, true);
    }

    public synchronized int getAndIncrementTerminalSessionNumberSinceBoot() {
        // Keep value at MAX_VALUE on integer overflow and not 0, since not first shell
        return SharedPreferenceUtils.getAndIncrementInt(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_SESSION_NUMBER_SINCE_BOOT,
            TERMUX_APP.DEFAULT_VALUE_TERMINAL_SESSION_NUMBER_SINCE_BOOT, true, Integer.MAX_VALUE);
    }

    public synchronized void resetTerminalSessionNumberSinceBoot() {
        SharedPreferenceUtils.setInt(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_SESSION_NUMBER_SINCE_BOOT,
            TERMUX_APP.DEFAULT_VALUE_TERMINAL_SESSION_NUMBER_SINCE_BOOT, true);
    }


    public boolean isTerminalViewKeyLoggingEnabled() {
        return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_VIEW_KEY_LOGGING_ENABLED, TERMUX_APP.DEFAULT_VALUE_TERMINAL_VIEW_KEY_LOGGING_ENABLED);
    }

    public void setTerminalViewKeyLoggingEnabled(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_TERMINAL_VIEW_KEY_LOGGING_ENABLED, value, false);
    }



    public boolean arePluginErrorNotificationsEnabled(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getBoolean(mMultiProcessSharedPreferences, TERMUX_APP.KEY_PLUGIN_ERROR_NOTIFICATIONS_ENABLED, TERMUX_APP.DEFAULT_VALUE_PLUGIN_ERROR_NOTIFICATIONS_ENABLED);
        else
            return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_PLUGIN_ERROR_NOTIFICATIONS_ENABLED, TERMUX_APP.DEFAULT_VALUE_PLUGIN_ERROR_NOTIFICATIONS_ENABLED);
    }

    public void setPluginErrorNotificationsEnabled(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_PLUGIN_ERROR_NOTIFICATIONS_ENABLED, value, false);
    }



    public boolean areCrashReportNotificationsEnabled(boolean readFromFile) {
        if (readFromFile)
            return SharedPreferenceUtils.getBoolean(mMultiProcessSharedPreferences, TERMUX_APP.KEY_CRASH_REPORT_NOTIFICATIONS_ENABLED, TERMUX_APP.DEFAULT_VALUE_CRASH_REPORT_NOTIFICATIONS_ENABLED);
       else
            return SharedPreferenceUtils.getBoolean(mSharedPreferences, TERMUX_APP.KEY_CRASH_REPORT_NOTIFICATIONS_ENABLED, TERMUX_APP.DEFAULT_VALUE_CRASH_REPORT_NOTIFICATIONS_ENABLED);
    }

    public void setCrashReportNotificationsEnabled(boolean value) {
        SharedPreferenceUtils.setBoolean(mSharedPreferences, TERMUX_APP.KEY_CRASH_REPORT_NOTIFICATIONS_ENABLED, value, false);
    }

}
