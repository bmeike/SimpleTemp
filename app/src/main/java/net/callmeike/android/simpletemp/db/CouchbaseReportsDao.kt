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
package net.callmeike.android.simpletemp.db

import android.util.Log
import com.couchbase.lite.Expression
import com.couchbase.lite.From
import com.couchbase.lite.MutableArray
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.callmeike.android.simpletemp.model.Report
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class CouchbaseReportsDao @Inject constructor(
    @Named("db") private val dispatcher: CoroutineDispatcher,
    private val db: Db
) : ReportsDao {
    companion object {
        private const val TAG = "REPORT_DB"

        const val DOC_TYPE_REPORT = "type_report"

        const val PROP_LOCATION = "report_location"

        private const val PROP_APP_NAME = "report_app_name"
        private const val PROP_APP_ID = "report_app_id"
        private const val PROP_TIMESTAMP = "report_timestamp"
        private const val PROP_PERSON_ID = "report_person_id"
        private const val PROP_TEMP = "report_temperature"
        private const val PROP_SYMPTOMS = "report_symptoms"
    }

    override suspend fun postReport(report: Report): Unit = withContext(dispatcher) {
        val mDoc = MutableDocument()
        mDoc.setString(CouchbaseDb.PROP_DOC_TYPE, DOC_TYPE_REPORT)
        mDoc.setString(PROP_APP_NAME, report.appName)
        mDoc.setString(PROP_APP_ID, report.appId)
        mDoc.setString(PROP_TIMESTAMP, report.time)
        mDoc.setString(PROP_PERSON_ID, report.ownerId)
        mDoc.setFloat(PROP_TEMP, report.temperature)

        val loc = report.loc
        if (loc != null) {
            mDoc.setArray(PROP_LOCATION, convertLoc(loc))
        }

        val symptoms = report.symptoms
        if ((symptoms != null) && symptoms.isNotEmpty()) {
            val symptomsArray = MutableArray()
            symptoms.forEach { symptomsArray.addString(it) }
            mDoc.setArray(PROP_SYMPTOMS, symptomsArray)
        }

        db.saveDoc(mDoc)
        Log.d(TAG, "Saved report: ${report}")
        Unit
    }

    override suspend fun getRecentReports(id: String): List<Pair<String, Float>> = withContext(dispatcher) {
        return@withContext db.runQuery(
            getTemperatureQuery().where(
                Expression.property(CouchbaseDb.PROP_DOC_TYPE).`is`(Expression.string(DOC_TYPE_REPORT))
                    .and(Expression.property(PROP_PERSON_ID).`is`(Expression.string(id)))
            )
        )
            .map { r -> toSample(r) }
    }

    private fun getTemperatureQuery(): From {
        return db.getQuery(
            SelectResult.expression(Expression.property(PROP_TIMESTAMP)),
            SelectResult.expression(Expression.property(PROP_TEMP))
        )
    }

    private fun toSample(r: Result): Pair<String, Float> {
        return Pair(
            r.getString(PROP_TIMESTAMP)!!,
            r.getFloat(PROP_TEMP)
        )
    }

    private fun convertLoc(loc: Pair<Float, Float>): MutableArray {
        val location = MutableArray()
        location.addFloat(loc.first)
        location.addFloat(loc.second)
        return location
    }
}