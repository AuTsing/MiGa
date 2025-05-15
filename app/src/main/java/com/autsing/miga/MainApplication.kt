package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.util.FileUtil
import com.autsing.miga.presentation.util.LoginUtil

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FileUtil.instance = FileUtil(this)
        LoginUtil.instance = LoginUtil()
    }

}
