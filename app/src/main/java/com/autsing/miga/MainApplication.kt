package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.util.FileUtil

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FileUtil.instance = FileUtil(this)
    }

}
