package com.example.bkkinfoplus;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

import javax.inject.Inject;

/**
 * Subclassing Application in order to build the Dagger injector.
 */
public class BkkInfoApplication extends Application {

    public static AppComponent injector;

    @Inject SharedPreferences mSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        injector = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .apiModule(new ApiModule())
                .build();

        injector.inject(this); // Oh the irony...

        setLanguage();
    }

    /**
     * Sets the application language (and locale) to the value saved in preferences.
     * If nothing is set, or set to "auto", it won't update the configuration.
     */
    private void setLanguage() {
        String languagePreference = mSharedPreferences.getString(
                getString(R.string.pref_key_language),
                getString(R.string.pref_key_language_auto)
        );

        if (languagePreference.equals(getString(R.string.pref_key_language_auto))) {
            // Language is "auto". This is either because the preference is missing,
            // or because it has been set to "auto"
            return;
        }

        Locale newLocale = new Locale(languagePreference);
        Locale.setDefault(newLocale);
        Configuration config = new Configuration();
        config.locale = newLocale;

        getResources().updateConfiguration(config, null);
    }
}
