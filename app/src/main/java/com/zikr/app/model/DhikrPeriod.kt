package com.zikr.app.model

enum class DhikrPeriod(
    val label: String,
    val shortLabel: String,
    val requestCode: Int,
    val notificationId: Int
) {
    MORNING(
        label = "الذكر الصباحي",
        shortLabel = "الصباحي",
        requestCode = 1001,
        notificationId = 2001
    ),
    EVENING(
        label = "الذكر المسائي",
        shortLabel = "المسائي",
        requestCode = 1002,
        notificationId = 2002
    );

    companion object {
        fun fromString(value: String?): DhikrPeriod {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MORNING
        }
    }
}
