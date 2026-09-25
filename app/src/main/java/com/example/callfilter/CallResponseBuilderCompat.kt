package com.example.callfilter

import android.telecom.CallScreeningService.CallResponse

object CallResponseBuilderCompat {

    fun build(isKnownContact: Boolean): CallResponse {
        val builder = CallResponse.Builder()

        return if (isKnownContact) {
            // Let it ring and behave 100% normally.
            builder
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        } else {
            // Decline before it rings, but keep it in the call log
            // (setSkipCallLog = false) so it still shows as a missed/declined
            // call in the native Phone app. We skip the SYSTEM's missed-call
            // notification because we post our own custom one instead.
            builder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()
        }
    }
}
