package com.autsing.miga

import android.app.Application
import com.autsing.miga.presentation.repository.AuthRepository
import com.autsing.miga.presentation.repository.DeviceRepository
import com.autsing.miga.presentation.repository.SceneRepository

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        AuthRepository.instance = AuthRepository(this)
        SceneRepository.instance = SceneRepository(this)
        DeviceRepository.instance = DeviceRepository(this)
    }
}
