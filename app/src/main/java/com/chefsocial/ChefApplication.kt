package com.chefsocial

import android.app.Application
import com.chefsocial.data.AppDatabase

class ChefApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.warmUp(this)
    }
}
