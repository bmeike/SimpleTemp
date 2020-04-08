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
import net.callmeike.android.simpletemp.model.People
import net.callmeike.android.simpletemp.model.Person
import com.couchbase.lite.Array
import com.couchbase.lite.Expression
import com.couchbase.lite.From
import com.couchbase.lite.MutableArray
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Result
import com.couchbase.lite.SelectResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class CouchbaseProfilesDao @Inject constructor(
    @Named("db") private val dispatcher: CoroutineDispatcher,
    private val db: Db
) : ProfilesDao {

    companion object {
        private const val TAG = "PROFILE_DB"

        private const val DOC_TYPE_PROFILE = "type_profile"

        private const val PROP_NAME = "profile_name"
        private const val PROP_ID = "profile_id"
        private const val PROP_BIRTH_YEAR = "profile_birth_year"
        private const val PROP_STREET = "profile_street"
        private const val PROP_CITY = "profile_city"
        private const val PROP_STATE = "profile_state"
        private const val PROP_ZIP = "profile_zip"
        private const val PROP_CONDITIONS = "profile_conditions"
    }

    override suspend fun saveProfile(person: Person): Unit = withContext(dispatcher) {
        val doc = db.getUniqueDoc(DOC_TYPE_PROFILE, PROP_ID, person.id)

        val mDoc = doc?.toMutable() ?: MutableDocument()

        mDoc.setString(CouchbaseDb.PROP_DOC_TYPE, DOC_TYPE_PROFILE)
        mDoc.setString(PROP_ID, person.id)
        mDoc.setString(PROP_NAME, person.name)

        if (person.birthYear == null) {
            mDoc.remove(PROP_BIRTH_YEAR)
        } else {
            mDoc.setInt(PROP_BIRTH_YEAR, person.birthYear)
        }
        if (person.street == null) {
            mDoc.remove(PROP_STREET)
        } else {
            mDoc.setString(PROP_STREET, person.street)
        }
        if (person.city == null) {
            mDoc.remove(PROP_CITY)
        } else {
            mDoc.setString(PROP_CITY, person.city)
        }
        if (person.state == null) {
            mDoc.remove(PROP_STATE)
        } else {
            mDoc.setString(PROP_STATE, person.state)
        }
        if (person.zip == null) {
            mDoc.remove(PROP_ZIP)
        } else {
            mDoc.setString(PROP_ZIP, person.zip)
        }

        val newConditions = person.conditions
        if ((newConditions == null) || (newConditions.isEmpty())) {
            mDoc.remove(PROP_CONDITIONS)
        } else {
            val conditionsArray = MutableArray()
            newConditions.forEach { conditionsArray.addString(it) }
            mDoc.setArray(PROP_CONDITIONS, conditionsArray)
        }

        db.saveDoc(mDoc)
        Log.d(TAG, "Saved profile: ${person}")
        Unit
    }

    override suspend fun getPerson(id: String): Person? = withContext(dispatcher) {
        val qb = getPersonQuery()
        val people = db.runQuery(
            qb.where(
                Expression.property(CouchbaseDb.PROP_DOC_TYPE).`is`(Expression.string(DOC_TYPE_PROFILE))
                    .and(Expression.property(PROP_ID).`is`(Expression.string(id)))
            )
        )
            .map { r -> toPerson(r) }

        val n = people.size
        if (n == 0) {
            return@withContext null
        }
        if (n > 1) {
            Log.w(TAG, "Id is not unique: ${id}.  Using first")
        }

        return@withContext people[0]
    }

    override suspend fun getPeople(): People = withContext(dispatcher) {
        return@withContext db.runQuery(
            getPersonQuery().where(
                Expression.property(CouchbaseDb.PROP_DOC_TYPE).`is`(Expression.string(DOC_TYPE_PROFILE))
            )
        )
            .map { r -> toPerson(r) }
    }

    private fun getPersonQuery(): From {
        return db.getQuery(
            SelectResult.expression(Expression.property(PROP_ID)),
            SelectResult.expression(Expression.property(PROP_NAME)),
            SelectResult.expression(Expression.property(PROP_BIRTH_YEAR)),
            SelectResult.expression(Expression.property(PROP_STREET)),
            SelectResult.expression(Expression.property(PROP_CITY)),
            SelectResult.expression(Expression.property(PROP_STATE)),
            SelectResult.expression(Expression.property(PROP_ZIP)),
            SelectResult.expression(Expression.property(PROP_CONDITIONS))
        )
    }

    private fun toPerson(r: Result) = Person(
        r.getString(PROP_ID)!!,
        r.getString(PROP_NAME)!!,
        r.getInt(PROP_BIRTH_YEAR),
        r.getString(PROP_STREET),
        r.getString(PROP_CITY),
        r.getString(PROP_STATE),
        r.getString(PROP_ZIP),
        convertConditions(r.getArray(PROP_CONDITIONS))
    )

    private fun convertConditions(conditionsArray: Array?): List<String>? {
        if (conditionsArray == null) {
            return null
        }

        val conditions = mutableListOf<String>()
        for (condition in conditionsArray.iterator()) {
            conditions.add(condition.toString())
        }

        return conditions
    }
}