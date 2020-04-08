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

import android.content.Context
import net.callmeike.android.simpletemp.model.App
import net.callmeike.android.simpletemp.model.People
import net.callmeike.android.simpletemp.model.Person
import net.callmeike.android.simpletemp.model.Report
import com.couchbase.lite.Document
import com.couchbase.lite.Endpoint
import com.couchbase.lite.From
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Query
import com.couchbase.lite.ReplicatorConfiguration
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult


interface AppDao {
    suspend fun getApp(): App?
    suspend fun registerApp(app: App)
}

interface ProfilesDao {
    suspend fun getPeople(): People
    suspend fun getPerson(id: String): Person?
    suspend fun saveProfile(person: Person)
}

interface ReportsDao {
    suspend fun postReport(report: Report)
    suspend fun getRecentReports(id: String): List<Pair<String, Float>>
}

// not for public use
interface Db {
    fun init(ctxt: Context)
    fun getQuery(vararg projection: SelectResult): From
    fun runQuery(query: Query): List<Result>
    fun getMetaIdsForDocsOfType(type: String): List<String>
    fun getUniqueDoc(type: String, idProp: String, id: String): Document?
    fun getDoc(metaId: String): Document?
    fun saveDoc(doc: MutableDocument)
    fun getSyncConfig(target: Endpoint): ReplicatorConfiguration
}
