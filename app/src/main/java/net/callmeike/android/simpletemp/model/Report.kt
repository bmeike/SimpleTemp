/*
   Copyright 2020, G. Blake Meike
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.simpletemp.model

import net.callmeike.android.simpletemp.app.APP_NAME
import org.threeten.bp.Instant
import org.threeten.bp.format.DateTimeFormatter

data class Report(
    val appId: String,
    val ownerId: String,
    val loc: Pair<Float, Float>?,
    val temperature: Float = 0.0F,
    val symptoms: List<String>? = null,
    val appName: String = APP_NAME,
    val time: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
) {
    companion object {
        const val SYMPTOM_COUGH = "COUGH"
        const val SYMPTOM_TIRED = "TIRED"
        const val SYMPTOM_BREATH = "BREATH"
    }
}