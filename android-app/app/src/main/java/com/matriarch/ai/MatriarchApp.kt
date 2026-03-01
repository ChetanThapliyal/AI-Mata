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
        val apiKey = "nnkwznyy3z47" 
        
        // Valid for 30 days, generated locally for testing to bypass SDK parser crash.
        val userToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiYW5kcm9pZC1tYXRyaWFyY2gtdXNlciIsImV4cCI6MTc3NDk0NTEyM30.cwlLNQmWwKRZf6CySWbcCFY87KnHwlyVz0sf0fukj6M" 
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
