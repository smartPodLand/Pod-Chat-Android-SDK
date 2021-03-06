package com.fanap.podchat.chat;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.securepreferences.SecurePreferences;
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.security.GeneralSecurityException;


/**
 * Sample app
 */
public

class App extends Application {


    private static final String TAG = "secureprefsample";
    protected static App instance;
    private SecurePreferences mSecurePrefs;
    private SecurePreferences mUserPrefs;

    public App() {
        super();
        instance = this;
    }

    public static App get() {
        return instance;
    }

    /**
     * Single point for the app to get the secure prefs object
     *
     * @return
     */
    public SharedPreferences getSharedPreferences() {
        if (mSecurePrefs == null) {
            mSecurePrefs = new SecurePreferences(this, "", "chat_prefs.xml");
            SecurePreferences.setLoggingEnabled(true);
        }
        return mSecurePrefs;
    }


    /**
     * This is just an example of how you might want to create your own key with less iterations 1,000 rather than default 10,000
     * . This makes it quicker but less secure.
     *
     * @return
     */
    public SharedPreferences getLesSecureSharedPreferences() {
        try {
            AesCbcWithIntegrity.SecretKeys myKey = AesCbcWithIntegrity.generateKeyFromPassword(Build.SERIAL, AesCbcWithIntegrity.generateSalt(), 1000);
            return new SecurePreferences(this, myKey, "chat_prefs.xml");
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "Failed to create custom key for SecurePreferences", e);
        }
        return null;
    }

    public SharedPreferences getDefaultSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }


    public SecurePreferences getUserPinBasedSharedPreferences(String password) {
        if (mUserPrefs == null) {
            mUserPrefs = new SecurePreferences(this, password, "user_prefs.xml");
        }
        return mUserPrefs;
    }

    public boolean changeUserPrefPassword(String newPassword) {
        if (mUserPrefs != null) {
            try {
                mUserPrefs.handlePasswordChange(newPassword, this);
                return true;
            } catch (GeneralSecurityException e) {
                Log.e(TAG, "Error during password change", e);
            }
        }
        return false;
    }
}
