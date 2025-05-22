package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.LoginHelper
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FileHelper.instance = FileHelper(this)
        LoginHelper.instance = LoginHelper()
        ApiHelper.instance = ApiHelper()
        SceneRepository.instance = SceneRepository(
            fileHelper = FileHelper.instance,
            apiHelper = ApiHelper.instance,
        )
        DeviceRepository.instance = DeviceRepository(
            fileHelper = FileHelper.instance,
            apiHelper = ApiHelper.instance,
        )
    }

}
