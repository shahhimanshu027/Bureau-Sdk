package com.bureau.utils

import android.content.SharedPreferences

/**
 * Created by Abhin.
 */
class PreferenceManager(private val mSharedPreferences: SharedPreferences) {

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: String) {
        val editor = mSharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Int) {
        val editor = mSharedPreferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Double) {
        setValue(key, value.toString())
    }

    /**
     * Set a value for the key
     */
    fun setValue(key: String, value: Long) {
        val editor = mSharedPreferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun setValue(key: String, value: Set<String>) {
        val editor = mSharedPreferences.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    /**
     * Gets the value from the settings stored natively on the device.
     *
     * @param defaultValue Default value for the key, if one is not found.
     */
    fun getValue(key: String, defaultValue: String): String? = mSharedPreferences.getString(key, defaultValue)

    fun getIntValue(key: String, defaultValue: Int): Int = mSharedPreferences.getInt(key, defaultValue)

    fun getLongValue(key: String, defaultValue: Long): Long = mSharedPreferences.getLong(key, defaultValue)

    fun getBooleanValue(key: String, defaultValue: Boolean): Boolean = mSharedPreferences.getBoolean(key, defaultValue)

    fun getFloatValue(key: String, defaultValue: Float): Float = mSharedPreferences.getFloat(key, defaultValue)

    fun getStringSet(key: String, defaultValue: Set<String>): Set<String>? = mSharedPreferences.getStringSet(key, defaultValue)

    /**
     * Gets the value from the preferences stored natively on the device.
     *
     * @param defValue Default value for the key, if one is not found.
     */
    fun getValue(key: String, defValue: Boolean): Boolean = mSharedPreferences.getBoolean(key, defValue)

    fun setValue(key: String, value: Boolean) {
        val editor = mSharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    /**
     * Clear all the preferences store in this [SharedPreferences.Editor]
     */
    fun clear() {
        mSharedPreferences.edit().clear().apply()
    }

    /**
     * Removes preference entry for the given key.
     *
     * @param key Value for the key
     */
    fun removeValue(key: String) {
        mSharedPreferences.edit().remove(key).apply()
    }
}
