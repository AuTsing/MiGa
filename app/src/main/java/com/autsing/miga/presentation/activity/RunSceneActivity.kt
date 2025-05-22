package com.autsing.miga.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import com.autsing.miga.presentation.helper.ApiHelper
import com.autsing.miga.presentation.helper.FileHelper
import com.autsing.miga.presentation.model.Auth
import com.autsing.miga.presentation.repository.SceneRepository
import com.autsing.miga.presentation.screen.RunSceneScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class RunSceneActivity : ComponentActivity() {

    companion object {
        const val EXTRA_SCENE_ID: String = "extra_scene_id"
    }

    private val fileHelper: FileHelper = FileHelper.instance
    private val apiHelper: ApiHelper = ApiHelper.instance
    private val sceneRepository: SceneRepository = SceneRepository.instance

    private val loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val message: MutableStateFlow<String> = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            val loading = loading.collectAsState()
            val message = message.collectAsState()

            RunSceneScreen(loading.value, message.value)
        }

        handleRunScene()
    }

    private fun handleRunScene() = lifecycleScope.launch(Dispatchers.IO) {
        runCatching {
            val auth = runCatching {
                val authJson = fileHelper.readJson("auth.json").getOrThrow()
                Json.decodeFromString<Auth>(authJson)
            }.getOrElse { throw Exception("用户信息无效") }
            val scene = runCatching {
                val sceneId = intent.getStringExtra(EXTRA_SCENE_ID) ?: ""
                val scenes = sceneRepository.loadScenesLocal(emptySet()).getOrThrow()
                scenes.first { it.scene_id == sceneId }
            }.getOrElse { throw Exception("场景信息无效") }

            message.value = apiHelper.runScene(auth, scene).getOrThrow()
            loading.value = false

            if (message.value == "ok") {
                delay(1000)
                finish()
            }
        }.onFailure {
            message.value = it.message ?: it.stackTraceToString()
            loading.value = false
        }
    }
}
