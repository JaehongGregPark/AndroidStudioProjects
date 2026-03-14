package com.example.scriptaudio.engine.scroll

/**
 * AutoScrollEngine
 *
 * TTS 진행에 맞춰
 * Reader 자동 스크롤
 */

import androidx.compose.foundation.lazy.LazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AutoScrollEngine {

    /**
     * 특정 위치로 스크롤
     */
    fun scrollTo(

        scope: CoroutineScope,
        listState: LazyListState,
        index: Int

    ) {

        scope.launch {

            listState.animateScrollToItem(index)

        }

    }

}