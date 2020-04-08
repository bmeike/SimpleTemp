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
package net.callmeike.android.simpletemp.sync

import android.util.Log
import net.callmeike.android.simpletemp.db.CouchbaseDb
import net.callmeike.android.simpletemp.db.CouchbaseReportsDao
import com.couchbase.lite.Document
import com.couchbase.lite.DocumentFlag
import com.couchbase.lite.ReplicationFilter
import com.couchbase.lite.Replicator
import com.couchbase.lite.URLEndpoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.URI
import java.util.EnumSet
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class CouchbaseDataSync @Inject constructor(
    @Named("net") private val dispatcher: CoroutineDispatcher,
    private val db: CouchbaseDb
) : DataSync {
    companion object {
        private const val TAG = "SYNC"

        private val TARGET_URI = URI.create("wss://localhost:4984/todo")
    }

    override suspend fun sync(): Unit = withContext(dispatcher) {
        val filter = ReplicationFilter { doc, flags ->
            ((doc.getString(CouchbaseDb.PROP_DOC_TYPE) == CouchbaseReportsDao.DOC_TYPE_REPORT)
                    && doc.contains(CouchbaseReportsDao.PROP_LOCATION))
        }

        val config = db.getSyncConfig(URLEndpoint(TARGET_URI))
        config.replicatorType = ReplicatorHelper.getReplicatorTypeFor(true, true)
        config.isContinuous = false
        config.pushFilter = filter
        config.pullFilter = filter

        Replicator(config).start()
        Log.d(TAG, "Replication started")
        Unit
    }
}
