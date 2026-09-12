package com.example.data

import com.example.network.Content
import com.example.network.GeminiRequest
import com.example.network.NetworkModule
import com.example.network.Part
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val dao: ReportDao) {
    val allReports: Flow<List<SavedReport>> = dao.getAllReports()

    suspend fun saveReport(report: SavedReport) {
        dao.insertReport(report)
    }
    
    suspend fun deleteReport(id: String) {
        dao.deleteReport(id)
    }

    suspend fun analyzeReport(text: String, apiKey: String): String {
        val prompt = """
            You are a medical assistant analyzing a lab report.
            Please read the following text and summarize it into a structured format.
            Return ONLY a JSON object that matches this structure EXACTLY:
            {
              "title": "...",
              "date": "...",
              "urgency": "NORMAL" | "ATTENTION" | "URGENT",
              "urgencyTitle": "...",
              "category": "Blood Test" | "Imaging" | "Prescription" | "Other",
              "patientSummary": "...",
              "language": "English",
              "testItems": [
                {
                  "testName": "...",
                  "simpleName": "...",
                  "resultValue": "...",
                  "normalRange": "...",
                  "status": "NORMAL" | "LOW" | "HIGH" | "ATTENTION",
                  "explanation": "..."
                }
              ],
              "practicalAdvice": ["..."],
              "doctorQuestions": ["..."]
            }
            
            Text to analyze:
            $text
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )
        val response = NetworkModule.geminiService.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Empty response from AI")
    }

    suspend fun analyzeImage(base64Image: String, apiKey: String): String {
        val prompt = """
            You are a medical assistant analyzing a lab report image.
            Please extract all relevant text and summarize it into a structured format.
            Return ONLY a JSON object that matches this structure EXACTLY:
            {
              "title": "...",
              "date": "...",
              "urgency": "NORMAL" | "ATTENTION" | "URGENT",
              "urgencyTitle": "...",
              "category": "Blood Test" | "Imaging" | "Prescription" | "Other",
              "patientSummary": "...",
              "language": "English",
              "testItems": [
                {
                  "testName": "...",
                  "simpleName": "...",
                  "resultValue": "...",
                  "normalRange": "...",
                  "status": "NORMAL" | "LOW" | "HIGH" | "ATTENTION",
                  "explanation": "..."
                }
              ],
              "practicalAdvice": ["..."],
              "doctorQuestions": ["..."]
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = com.example.network.InlineData("image/jpeg", base64Image))
                    )
                )
            )
        )
        val response = NetworkModule.geminiService.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: throw Exception("Empty response from AI")
    }
}
