package com.autsing.miga.presentation.activity

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.ActionBuilders.AndroidStringExtra
import androidx.wear.protolayout.ActionBuilders.LaunchAction
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
        private const val EXTRA_SCENE_ID: String = "extra_scene_id"

        fun startActivity(context: Context, sceneId: String) {
            val intent = Intent(context, RunSceneActivity::class.java)
            intent.putExtra(EXTRA_SCENE_ID, sceneId)
            context.startActivity(intent)
        }

        fun createLaunchAction(context: Context, sceneId: String): LaunchAction {
            val extra = AndroidStringExtra.Builder()
                .setValue(sceneId)
                .build()
            val activity = AndroidActivity.Builder()
                .setPackageName(context.packageName)
                .setClassName(RunSceneActivity::class.java.name)
                .addKeyToExtraMapping(EXTRA_SCENE_ID, extra)
                .build()
            val action = LaunchAction.Builder()
                .setAndroidActivity(activity)
                .build()
            return action
        }

        fun createPendingIntent(
            context: Context,
            requestCode: Int,
            sceneId: String,
        ): PendingIntent {
            val intent = Intent(context, RunSceneActivity::class.java)
            intent.putExtra(EXTRA_SCENE_ID, sceneId)
            val pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            return pendingIntent
        }
    }

    private val fileHelper: FileHelper = FileHelper.instance
    private val apiHelper: ApiHelper = ApiHelper.instance
    private val sceneRepository: SceneRepository = SceneRepository.instance

    private val loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val success: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val exception: MutableStateFlow<String> = MutableStateFlow("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val loading = loading.collectAsState()
            val success = success.collectAsState()
            val exception = exception.collectAsState()

            RunSceneScreen(loading.value, success.value, exception.value)
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
                val scenes = sceneRepository.loadScenesLocal().getOrThrow()
                scenes.first { it.scene_id == sceneId }
            }.getOrElse { throw Exception("场景信息无效") }

            val result = apiHelper.runScene(auth, scene).getOrThrow()

            throw Exception("场景信息无效")
            if (result == "ok") {
                success.value = true
                loading.value = false
                delay(1500)
                finish()
            }
        }.onFailure {
            success.value = false
            exception.value = it.stackTraceToString()
            loading.value = false
        }
    }
}
