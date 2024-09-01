package com.nextgen.indoorplanting;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private static final String USER_SHARED_PREFS = "USER_SHARED_PREFS";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_TYPE = "user_type";

    private static UserSessionManager instance;
    private Context context;

    private String userEmail;
    private String userName;
    private String userType;

    private UserSessionManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context);
        }
        return instance;
    }

    public String getUserEmail() {
        if (userEmail == null || userEmail.isEmpty()) {
            loadUserEmailFromPrefs();
        }
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        saveUserEmailToPrefs(userEmail);
    }

    public String getUserName() {
        if (userName == null || userName.isEmpty()) {
            loadUserNameFromPrefs();
        }
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        saveUserNameToPrefs(userName);
    }

    public String getUserType() {
        if (userType == null || userType.isEmpty()) {
            loadUserTypeFromPrefs();
        }
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
        saveUserTypeToPrefs(userType);
    }

    private void saveUserEmailToPrefs(String userEmail) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.apply();
    }

    private void loadUserEmailFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    private void saveUserNameToPrefs(String userName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.apply();
    }

    private void loadUserNameFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userName = sharedPreferences.getString(KEY_USER_NAME, "");
    }

    private void saveUserTypeToPrefs(String userType) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_TYPE, userType);
        editor.apply();
    }

    private void loadUserTypeFromPrefs() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        userType = sharedPreferences.getString(KEY_USER_TYPE, "");
    }

    public void clearSharedPreferences() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(USER_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        userEmail = null;
        userName = null;
        userType = null;
    }
}
