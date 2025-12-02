package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.helper.LoginHelper
import com.autsing.miga.presentation.helper.SerdeHelper
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FileHelper.instance = FileHelper(this)
        SerdeHelper.instance = SerdeHelper()
        LoginHelper.instance = LoginHelper(
            serdeHelper = SerdeHelper.instance,
        )
        ApiHelper.instance = ApiHelper(
            serdeHelper = SerdeHelper.instance,
        )
        SceneRepository.instance = SceneRepository(
            serdeHelper = SerdeHelper.instance,
            fileHelper = FileHelper.instance,
            apiHelper = ApiHelper.instance,
        )
        DeviceRepository.instance = DeviceRepository(
            serdeHelper = SerdeHelper.instance,
            fileHelper = FileHelper.instance,
            apiHelper = ApiHelper.instance,
        )
    }
}
