package io.github.ikafire.stronger.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements")
data class BodyMeasurementEntity(
    @PrimaryKey
    val id: String,
    val date: String,
    val type: String,
    val value: Double,
    val unit: String
)
