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

import android.util.Base64
import android.util.Log
import net.callmeike.android.simpletemp.model.App
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class CouchbaseAppDao @Inject constructor(
    @Named("db") private val dispatcher: CoroutineDispatcher,
    private val db: Db
) : AppDao {
    companion object {
        private const val TAG = "APP_DB"

        private const val DOC_TYPE_APP = "type_app"

        private const val PROP_SALT = "app_salt"
        private const val PROP_INIT_VEC = "app_init_vec"
        private const val PROP_APP_ID = "app_id"
        private const val PROP_PASSWORD = "app_password"
    }

    // Cached app record
    private var app: App? = null

    override suspend fun registerApp(app: App): Unit = withContext(dispatcher) {
        if (getApp() != null) {
            throw IllegalStateException("attempt to re-register the app")
        }

        val newAppDoc = MutableDocument()
        newAppDoc.setString(CouchbaseDb.PROP_DOC_TYPE, DOC_TYPE_APP)
        newAppDoc.setString(PROP_SALT, Base64.encodeToString(app.salt, Base64.DEFAULT))
        newAppDoc.setString(PROP_INIT_VEC, Base64.encodeToString(app.iv, Base64.DEFAULT))
        newAppDoc.setString(PROP_APP_ID, app.id)
        newAppDoc.setString(PROP_PASSWORD, app.password)

        db.saveDoc(newAppDoc)
    }

    override suspend fun getApp(): App? = withContext(dispatcher) {
        if (app != null) {
            return@withContext app
        }

        val qb = db.getQuery(
            SelectResult.expression(Expression.property(PROP_SALT)),
            SelectResult.expression(Expression.property(PROP_APP_ID)),
            SelectResult.expression(Expression.property(PROP_INIT_VEC)),
            SelectResult.expression(Expression.property(PROP_PASSWORD))
        )

        val apps =
            db.runQuery(qb.where(Expression.property(CouchbaseDb.PROP_DOC_TYPE).`is`(Expression.string(DOC_TYPE_APP))))
        val n = apps.size
        if (n == 0) {
            return@withContext null
        }
        if (n > 1) {
            Log.w(TAG, "Too many App docs.  Using first")
        }

        // cache the app
        app = toApp(apps[0])
        Log.d(TAG, "Fetched application: ${app}")

        return@withContext app
    }

    private fun toApp(result: Result): App = App(
        Base64.decode(result.getString(PROP_SALT), Base64.DEFAULT),
        Base64.decode(result.getString(PROP_INIT_VEC), Base64.DEFAULT),
        result.getString(PROP_APP_ID)!!,
        result.getString(PROP_PASSWORD)!!
    )
}