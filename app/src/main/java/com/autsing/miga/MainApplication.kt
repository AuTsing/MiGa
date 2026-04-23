package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.helper.SerdeHelper
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SerdeHelper.instance = SerdeHelper()
        AuthRepository.instance = AuthRepository(
            context = this,
        )
        SceneRepository.instance = SceneRepository(
            context = this,
        )
        DeviceRepository.instance = DeviceRepository(
            context = this,
        )
    }
}
