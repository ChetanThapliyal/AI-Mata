package com.matriarch.ai.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matriarch.ai.network.MatriarchNetwork
import com.matriarch.ai.network.SessionStartRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatriarchViewModel : ViewModel() {

    private val _currentThought = MutableStateFlow("Initializing Maya...")
    val currentThought: StateFlow<String> = _currentThought
    
    // We can update this later based on call events if needed
    private val _isMayaSpeaking = MutableStateFlow(false)
    val isMayaSpeaking: StateFlow<Boolean> = _isMayaSpeaking

    fun startSession(callId: String) {
        _currentThought.value = "Calling Maya..."
        viewModelScope.launch {
            try {
                val request = SessionStartRequest(call_id = callId)
                val response = MatriarchNetwork.api.startSession(request)
                
                if (response.session_id.isNotEmpty()) {
                    _currentThought.value = "Maya is watching..."
                } else {
                    _currentThought.value = "Maya is busy"
                }
            } catch (e: Exception) {
                Log.e("MatriarchViewModel", "Session start failed", e)
                _currentThought.value = "Connection error"
            }
        }
    }
}
