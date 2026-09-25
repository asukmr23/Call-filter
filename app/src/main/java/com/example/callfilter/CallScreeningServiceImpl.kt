package com.example.callfilter

import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * Android calls onScreenCall() for every incoming call BEFORE the phone rings,
 * as long as this app is set as the system's "Call Screening" app
 * (Settings > Apps > Default apps > Caller ID & spam apps on Samsung/One UI).
 *
 * Logic:
 *  - Number IS in contacts  -> allow the call through normally, it rings as usual.
 *  - Number NOT in contacts -> silently decline (no ring), but keep it in the call
 *                              log and post our own low-key notification so the
 *                              user can see who called and tap to call back.
 */
class CallScreeningServiceImpl : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart

        val isKnownContact = when {
            phoneNumber.isNullOrBlank() -> true // unknown/withheld number: don't block, let normal handling occur
            else -> ContactUtils.isNumberInContacts(applicationContext, phoneNumber)
        }

        val response = CallResponseBuilderCompat.build(isKnownContact)
        respondToCall(callDetails, response)

        if (!isKnownContact && !phoneNumber.isNullOrBlank()) {
            NotificationHelper.showMissedCallNotification(
                context = applicationContext,
                phoneNumber = phoneNumber,
                callerLabel = callDetails.callerDisplayName
            )
        }
    }
}
