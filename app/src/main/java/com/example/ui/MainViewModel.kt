package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ReportRepository
import com.example.data.SavedReport
import com.example.data.TestItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class ThemePreference { SYSTEM, LIGHT, DARK }

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val report: SavedReport) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ReportRepository
    private val sharedPrefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    private val _themePreference = MutableStateFlow(
        ThemePreference.valueOf(sharedPrefs.getString("theme_pref", ThemePreference.SYSTEM.name) ?: ThemePreference.SYSTEM.name)
    )
    val themePreference: StateFlow<ThemePreference> = _themePreference.asStateFlow()

    private val _isLowPowerMode = MutableStateFlow(
        sharedPrefs.getBoolean("low_power_mode", false)
    )
    val isLowPowerMode: StateFlow<Boolean> = _isLowPowerMode.asStateFlow()

    private val _savedReports = MutableStateFlow<List<SavedReport>>(emptyList())
    val savedReports: StateFlow<List<SavedReport>> = _savedReports.asStateFlow()

    private val _analysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val analysisState: StateFlow<AnalysisState> = _analysisState.asStateFlow()
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        val dao = AppDatabase.getDatabase(application).reportDao()
        repository = ReportRepository(dao)
        
        viewModelScope.launch {
            repository.allReports.collectLatest { reports ->
                _savedReports.value = reports
            }
        }
    }

    fun analyzeText(text: String, apiKey: String) {
        if (text.isBlank() || apiKey.isBlank()) {
            _analysisState.value = AnalysisState.Error("Text and API Key are required.")
            return
        }
        _analysisState.value = AnalysisState.Loading
        viewModelScope.launch {
            try {
                val jsonString = repository.analyzeReport(text, apiKey)
                val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                
                val adapter = moshi.adapter(SavedReport::class.java)
                val parsedReport = adapter.fromJson(cleanJson)
                
                if (parsedReport != null) {
                    val reportWithId = parsedReport.copy(id = "rep-${System.currentTimeMillis()}")
                    _analysisState.value = AnalysisState.Success(reportWithId)
                } else {
                    _analysisState.value = AnalysisState.Error("Failed to parse report.")
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun analyzeImage(base64Image: String, apiKey: String) {
        if (base64Image.isBlank() || apiKey.isBlank()) {
            _analysisState.value = AnalysisState.Error("Image and API Key are required.")
            return
        }
        _analysisState.value = AnalysisState.Loading
        viewModelScope.launch {
            try {
                val jsonString = repository.analyzeImage(base64Image, apiKey)
                val cleanJson = jsonString.replace("```json", "").replace("```", "").trim()
                
                val adapter = moshi.adapter(SavedReport::class.java)
                val parsedReport = adapter.fromJson(cleanJson)
                
                if (parsedReport != null) {
                    val reportWithId = parsedReport.copy(id = "rep-${System.currentTimeMillis()}")
                    _analysisState.value = AnalysisState.Success(reportWithId)
                } else {
                    _analysisState.value = AnalysisState.Error("Failed to parse report.")
                }
            } catch (e: Exception) {
                _analysisState.value = AnalysisState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveReport(report: SavedReport) {
        viewModelScope.launch {
            repository.saveReport(report)
        }
    }
    
    fun deleteReport(id: String) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    fun clearAnalysis() {
        _analysisState.value = AnalysisState.Idle
    }
    
    fun setThemePreference(preference: ThemePreference) {
        sharedPrefs.edit().putString("theme_pref", preference.name).apply()
        _themePreference.value = preference
    }

    fun setLowPowerMode(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("low_power_mode", enabled).apply()
        _isLowPowerMode.value = enabled
    }
}
