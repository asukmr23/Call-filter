package com.example.callfilter

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

object ContactUtils {

    /**
     * Returns true if [rawNumber] matches a number saved in the user's contacts.
     * Uses ContactsContract.PhoneLookup, which handles formatting differences
     * (spaces, dashes, country codes) the same way the Phone app does.
     */
    fun isNumberInContacts(context: Context, rawNumber: String): Boolean {
        if (rawNumber.isBlank()) return false

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(rawNumber)
        )
        val projection = arrayOf(ContactsContract.PhoneLookup._ID)

        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                cursor.moveToFirst()
            } ?: false
        } catch (e: SecurityException) {
            // No READ_CONTACTS permission yet -> treat as unknown-safe default is "known"
            // so we never silently block calls before the user has granted permission.
            true
        }
    }
}
