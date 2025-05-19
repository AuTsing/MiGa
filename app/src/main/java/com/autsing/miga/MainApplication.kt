package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.LoginHelper

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FileHelper.instance = FileHelper(this)
        LoginHelper.instance = LoginHelper()
        ApiHelper.instance = ApiHelper()
    }

}
