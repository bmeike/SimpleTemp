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
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Document
import com.couchbase.lite.Endpoint
import com.couchbase.lite.Expression
import com.couchbase.lite.LogLevel
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Query
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.ReplicatorConfiguration
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CouchbaseDb @Inject constructor() : Db {
    lateinit var db: Database

    companion object {
        const val DB_NAME = "SimpleTemp"

        const val PROP_DOC_TYPE = "document_type"

        private const val PROP_DOC_META_ID = "meta_id"
    }

    override fun init(ctxt: Context) {
        CouchbaseLite.init(ctxt)
        Database.log.console.level = LogLevel.DEBUG
        db = Database(DB_NAME)
    }

    override fun getDoc(metaId: String): Document? = db.getDocument(metaId)

    override fun saveDoc(doc: MutableDocument) = db.save(doc)

    override fun getSyncConfig(target: Endpoint) = ReplicatorConfiguration(db, target)

    override fun getQuery(vararg projection: SelectResult) =
        QueryBuilder.select(*projection).from(DataSource.database(db))

    override fun runQuery(query: Query): List<Result> = query.execute().allResults()

    override fun getMetaIdsForDocsOfType(type: String): List<String> {
        return QueryBuilder.select(SelectResult.expression(Meta.id).`as`(PROP_DOC_META_ID))
            .from(DataSource.database(db))
            .where(Expression.property(PROP_DOC_TYPE).`is`(Expression.string(type)))
            .execute()
            .allResults()
            .map { r -> r.getString(PROP_DOC_META_ID)!! }
    }

    override fun getUniqueDoc(type: String, idProp: String, id: String): Document? {
        val ids = QueryBuilder.select(SelectResult.expression(Meta.id).`as`(PROP_DOC_META_ID))
            .from(DataSource.database(db))
            .where(
                Expression.property(PROP_DOC_TYPE).`is`(Expression.string(type))
                    .and(Expression.property(idProp).`is`(Expression.string(id)))
            )
            .execute()
            .allResults()

        if (ids.size < 1) {
            return null
        }

        if (ids.size > 1) {
            throw IllegalStateException("id/type is not unique")
        }

        return getDoc(ids[0].getString(PROP_DOC_META_ID)!!)
    }
}
