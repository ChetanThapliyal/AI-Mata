package com.matriarch.ai

import android.app.Application
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User

class MatriarchApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // This is the static API key from the Python backend's .env.
        // In a real production app, never hardcode the user token on the client.
        val apiKey = "t53u7eu48q4h" 
        
        // Use the dynamically generated API_TOKEN (acts as both backend auth and Stream SDK user token)
        val userToken = BuildConfig.API_TOKEN
        val userId = "android-matriarch-user"

        val user = User(
            id = userId,
            name = "Matriarch Device",
        )

        StreamVideoBuilder(
            context = applicationContext,
            apiKey = apiKey,
            geo = GEO.GlobalEdgeNetwork,
            user = user,
            token = userToken,
        ).build()
        // The builder automatically sets the singleton instance of StreamVideo.
    }

    companion object {
        fun client(): StreamVideo {
            return StreamVideo.instance()
        }
    }
}
