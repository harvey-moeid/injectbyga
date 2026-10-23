package com.byga.flowmanager.data

import android.os.Build
import com.byga.flowmanager.BuildConfig
import com.byga.flowmanager.domain.Trial
import org.json.JSONArray
import org.json.JSONObject

/** No emails, tokens, free-form notes or private browser data in the exported evidence. */
object EvidenceExport {
    fun json(trials: List<Trial>): String = JSONObject().apply {
        put("schema", 1)
        put("gate", "UNVERIFIED")
        put("verificationSource", "TESTER_MANUAL_ONLY")
        put("appVersion", BuildConfig.VERSION_NAME)
        put("androidApi", Build.VERSION.SDK_INT)
        put("androidRelease", Build.VERSION.RELEASE)
        put("manufacturer", Build.MANUFACTURER)
        put("model", Build.MODEL)
        put("exportedAt", System.currentTimeMillis())
        put("warning", "Hasil historis manual; bukan bukti otomatis isolasi atau sesi saat ini. Lampirkan bukti perangkat terpisah.")
        put("trials", JSONArray().apply {
            trials.reversed().forEach { trial -> put(JSONObject().apply {
                put("id", trial.id); put("slot", trial.slot)
                put("method", trial.method.name); put("provider", trial.provider)
                put("providerVersion", trial.providerVersion); put("profile", trial.profile)
                put("scenario", trial.scenario); put("run", trial.run)
                put("startedAt", trial.startedAt); put("outcome", trial.outcome.name)
                put("reviewedAt", trial.reviewedAt ?: JSONObject.NULL)
                put("errorCode", trial.errorCode ?: JSONObject.NULL)
            }) }
        })
    }.toString(2)
}
