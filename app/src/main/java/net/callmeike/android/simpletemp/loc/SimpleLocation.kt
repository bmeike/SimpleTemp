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
package net.callmeike.android.simpletemp.loc

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import net.callmeike.android.simpletemp.app.SimpleTemp
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.round


@Singleton
class SimpleLocation @Inject constructor(
    private val app: SimpleTemp
) : Location {
    companion object {
        private const val TAG = "LOC"
    }

    override fun enabledProviders(): MutableList<String> =
        (app.getSystemService(LOCATION_SERVICE) as LocationManager).getProviders(true)

    override fun neededPermissions(): List<String> {
        return listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
            .filter { p -> ActivityCompat.checkSelfPermission(app, p) != PackageManager.PERMISSION_GRANTED }
    }

    override suspend fun getMostRecentLocation(): Pair<Float, Float>? {
        if (neededPermissions().isNotEmpty()) {
            return null
        }

        val providers = enabledProviders()
        if (providers.isEmpty()) {
            return null
        }

        val locMgr = app.getSystemService(LOCATION_SERVICE) as LocationManager
        var bestLocation: android.location.Location? = null
        for (provider in providers) {
            val l = try {
                locMgr.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                null
            } ?: continue

            if ((bestLocation == null) || (l.accuracy < bestLocation.accuracy)) {
                bestLocation = l
            }
        }

        bestLocation ?: return null

        Log.d(TAG, "Got good location: ${bestLocation.latitude}/${bestLocation.longitude}")
        return Pair(roundTo2Decimals(bestLocation.latitude), roundTo2Decimals(bestLocation.longitude))
    }

    // Extension function to reduce the accuracy of lat/long to just over a kilometer
    private fun roundTo2Decimals(v: Double) = (round(v * 100.0) / 100.0).toFloat()
}