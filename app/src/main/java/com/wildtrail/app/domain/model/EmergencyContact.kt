package com.wildtrail.app.domain.model

/**
 * Emergency contacts that the app can dial automatically if fall-detection
 * triggers. A user may have many; [isPrimary] selects who is dialled first
 * and [notifyOnFall] gates whether the contact is involved at all.
 */
data class EmergencyContact(
    val contactId: String,
    val userUid: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String?,
    val isPrimary: Boolean,
    val notifyOnFall: Boolean,
)
