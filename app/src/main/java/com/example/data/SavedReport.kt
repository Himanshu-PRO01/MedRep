package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types

@Entity(tableName = "reports")
@TypeConverters(ReportConverters::class)
data class SavedReport(
    @PrimaryKey val id: String,
    val title: String,
    val date: String,
    val urgency: String,
    val urgencyTitle: String,
    val category: String = "Other",
    val patientSummary: String,
    val language: String,
    val testItems: List<TestItem>,
    val practicalAdvice: List<String>,
    val doctorQuestions: List<String>
)

@JsonClass(generateAdapter = true)
data class TestItem(
    val testName: String,
    val simpleName: String,
    val resultValue: String,
    val normalRange: String,
    val status: String,
    val explanation: String
)

class ReportConverters {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    @TypeConverter
    fun fromTestItemList(list: List<TestItem>): String {
        val type = Types.newParameterizedType(List::class.java, TestItem::class.java)
        return moshi.adapter<List<TestItem>>(type).toJson(list)
    }

    @TypeConverter
    fun toTestItemList(json: String): List<TestItem> {
        val type = Types.newParameterizedType(List::class.java, TestItem::class.java)
        return moshi.adapter<List<TestItem>>(type).fromJson(json) ?: emptyList()
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String): List<String> {
        val type = Types.newParameterizedType(List::class.java, String::class.java)
        return moshi.adapter<List<String>>(type).fromJson(json) ?: emptyList()
    }
}
