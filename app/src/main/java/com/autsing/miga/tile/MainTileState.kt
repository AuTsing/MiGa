package com.autsing.miga.tile

import com.autsing.miga.presentation.model.Scene
import com.autsing.miga.presentation.model.getMockScene

data class MainTileState(
    val scenes: List<Scene>,
)

fun getMockMainTileState(): MainTileState {
    return MainTileState(
        scenes = listOf(
            getMockScene(0),
            getMockScene(1),
            getMockScene(2),
            getMockScene(3),
        ),
    )
}
