package com.termux.app.fragments.settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.termux.R;
import com.termux.shared.termux.settings.preferences.TermuxAppSharedPreferences;
import android.app.AlertDialog;
import androidx.preference.SwitchPreference;

@Keep
public class TermuxPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context == null) return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setPreferenceDataStore(TermuxPreferencesDataStore.getInstance(context));

        setPreferencesFromResource(R.xml.termux_preferences, rootKey);

        // Диалог перезагрузки для volume_keys_behaviour
        SwitchPreference volumePref = findPreference("volume_keys_behaviour");
        if (volumePref != null) {
            volumePref.setOnPreferenceChangeListener((preference, newValue) -> {
                Context ctx = getContext();
                if (ctx != null) {
                    new AlertDialog.Builder(ctx)
                        .setTitle("Применить настройки?")
                        .setMessage("Изменения вступят в силу после перезагрузки конфигурации Termux. Перезагрузить сейчас?")
                        .setPositiveButton("Перезагрузить", (d, w) -> {
                            try {
                                Runtime.getRuntime().exec(new String[]{"sh", "-c", "termux-reload-settings"});
                            } catch (Exception e) {
                                // ignore
                            }
                        })
                        .setNegativeButton("Позже", null)
                        .show();
                }
                return true;
            });
        }
    }

}

class TermuxPreferencesDataStore extends PreferenceDataStore {

    private final Context mContext;
    private final TermuxAppSharedPreferences mPreferences;

    private static TermuxPreferencesDataStore mInstance;

    private TermuxPreferencesDataStore(Context context) {
        mContext = context;
        mPreferences = TermuxAppSharedPreferences.build(context, true);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        if (mPreferences == null) return;
        if (key == null) return;
        switch (key) {
            case "volume_keys_behaviour":
                mPreferences.setVirtualVolumeKeysDisabled(!value);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        if (mPreferences == null) return false;
        switch (key) {
            case "volume_keys_behaviour":
                return !mPreferences.areVirtualVolumeKeysDisabled();
            default:
                return false;
        }
    }

    public static synchronized TermuxPreferencesDataStore getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new TermuxPreferencesDataStore(context);
        }
        return mInstance;
    }

}
