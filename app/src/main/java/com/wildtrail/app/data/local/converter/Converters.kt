package com.wildtrail.app.data.local.converter

import androidx.room.TypeConverter
import com.wildtrail.app.domain.model.AchievementCategory
import com.wildtrail.app.domain.model.GeoPoint
import com.wildtrail.app.domain.model.Sex
import com.wildtrail.app.domain.model.SurfaceType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Room only supports a small set of column types out-of-the-box (primitives,
 * String, ByteArray). Anything richer needs a [TypeConverter] that serialises
 * it to one of those types on the way down and back on the way up.
 *
 * For collections we use Kotlinx Serialization to encode JSON into a TEXT
 * column. JSON keeps the data human-readable in Database Inspector, which is
 * very handy when debugging hike routes.
 *
 * For enums we just store the string name — enough for our purposes and
 * survives reordering of enum constants without DB migrations.
 */
object Converters {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- List<GeoPoint> <-> JSON ----------------------------------------

    @TypeConverter
    @JvmStatic
    fun geoPointListToJson(value: List<GeoPoint>): String =
        json.encodeToString(ListSerializer(GeoPoint.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun jsonToGeoPointList(value: String): List<GeoPoint> =
        json.decodeFromString(ListSerializer(GeoPoint.serializer()), value)

    // ---- List<String> <-> JSON ------------------------------------------

    @TypeConverter
    @JvmStatic
    fun stringListToJson(value: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), value)

    @TypeConverter
    @JvmStatic
    fun jsonToStringList(value: String): List<String> =
        json.decodeFromString(ListSerializer(String.serializer()), value)

    // ---- Enums <-> String -----------------------------------------------

    @TypeConverter
    @JvmStatic
    fun surfaceTypeToString(value: SurfaceType): String = value.name

    @TypeConverter
    @JvmStatic
    fun stringToSurfaceType(value: String): SurfaceType =
        runCatching { SurfaceType.valueOf(value) }.getOrDefault(SurfaceType.OTHER)

    @TypeConverter
    @JvmStatic
    fun achievementCategoryToString(value: AchievementCategory): String = value.name

    @TypeConverter
    @JvmStatic
    fun stringToAchievementCategory(value: String): AchievementCategory =
        runCatching { AchievementCategory.valueOf(value) }.getOrDefault(AchievementCategory.OTHER)

    @TypeConverter
    @JvmStatic
    fun sexToString(value: Sex?): String? = value?.name

    @TypeConverter
    @JvmStatic
    fun stringToSex(value: String?): Sex? = value?.let {
        runCatching { Sex.valueOf(it) }.getOrNull()
    }
}
