package com.example.androidlatihan15_firebasedb

import android.content.Context
import android.content.SharedPreferences

class PrefsHelper {
    val USER_ID = "uidx"
    val COUNTER_ID = "counter"

    var mContext: Context
    var shareSet: SharedPreferences

    constructor(ctx: Context) {
        mContext = ctx
        shareSet = mContext.getSharedPreferences(
            "APLIKASITESTDB",
            Context.MODE_PRIVATE
        )
    }

    fun saveUID(uid: String) {
        val edit = shareSet.edit()
        edit.putString(USER_ID, uid)
        edit.apply()
    }

    fun getUID(): String? {
        return shareSet.getString(USER_ID, " ")
    }

    fun saveCounterId(counter: Int) {
        val edit = shareSet.edit()
        edit.putInt(COUNTER_ID, counter)
        edit.apply()
    }

    fun getCounterId(): Int {
        return shareSet.getInt(COUNTER_ID, 1)
    }
}