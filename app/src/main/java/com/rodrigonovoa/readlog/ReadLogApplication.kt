package com.rodrigonovoa.readlog

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReadLogApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setApplicationLocales(AppCompatDelegate.getApplicationLocales())
    }
}
