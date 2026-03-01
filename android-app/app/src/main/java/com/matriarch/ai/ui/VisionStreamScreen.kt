package com.matriarch.ai.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.matriarch.ai.MatriarchApp
import com.matriarch.ai.ui.overlay.OverlayHud
import io.getstream.video.android.compose.permission.LaunchCallPermissions
import io.getstream.video.android.compose.theme.VideoTheme
import io.getstream.video.android.compose.ui.components.call.renderer.ParticipantVideo

@Composable
fun VisionStreamScreen(
    viewModel: MatriarchViewModel = viewModel()
) {
    val context = LocalContext.current
    val client = remember { MatriarchApp.client() }
    val call = remember { client.call("default", "matriarch-session-1") }

    var permissionsGranted by remember { mutableStateOf(false) }

    LaunchCallPermissions(
        call = call,
        onAllPermissionsGranted = {
            permissionsGranted = true
        }
    )

    LaunchedEffect(permissionsGranted) {
        if (permissionsGranted) {
            val result = call.join(create = true)
            result.onError {
                Toast.makeText(context, "Error joining: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Observe connection & participants
    val participants by call.state.participants.collectAsState()
    val connection by call.state.connection.collectAsState()
    
    // For HUD
    val currentThought by viewModel.currentThought.collectAsState()
    val isConnected = connection == io.getstream.video.android.core.RealtimeConnection.Connected
    
    // Wait for connection to trigger backend join
    LaunchedEffect(isConnected) {
        if (isConnected) {
            viewModel.startSession(call.id)
        }
    }

    VideoTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            
            // Fullscreen local video (we render our own feed natively using ParticipantVideo)
            val me by call.state.me.collectAsState()
            if (me != null) {
                ParticipantVideo(
                    modifier = Modifier.fillMaxSize(),
                    call = call,
                    participant = me!!
                )
            }
            
            // HUD Overlay
            val isMayaSpeaking by viewModel.isMayaSpeaking.collectAsState()
            OverlayHud(
                currentThought = currentThought,
                isConnected = isConnected,
                isMayaSpeaking = isMayaSpeaking
            )
        }
    }
}
