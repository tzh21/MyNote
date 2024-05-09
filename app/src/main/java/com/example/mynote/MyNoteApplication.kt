//Application 作用参考
//https://developer.android.com/courses/pathways/android-basics-compose-unit-5-pathway-2

package com.example.mynote

import android.app.Application
import androidx.media3.exoplayer.ExoPlayer
import com.example.mynote.data.AppContainer
import com.example.mynote.data.AppDataContainer

class MyNoteApplication : Application() {
    /**
     * AppContainer instance used by the rest of classes to obtain dependencies
     */
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer()
    }
}