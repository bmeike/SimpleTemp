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
package net.callmeike.android.simpletemp.bindings

import net.callmeike.android.simpletemp.crypto.Auth
import net.callmeike.android.simpletemp.crypto.SimpleAuth
import net.callmeike.android.simpletemp.db.AppDao
import net.callmeike.android.simpletemp.db.CouchbaseAppDao
import net.callmeike.android.simpletemp.db.CouchbaseDb
import net.callmeike.android.simpletemp.db.CouchbaseProfilesDao
import net.callmeike.android.simpletemp.db.CouchbaseReportsDao
import net.callmeike.android.simpletemp.db.Db
import net.callmeike.android.simpletemp.db.ProfilesDao
import net.callmeike.android.simpletemp.db.ReportsDao
import net.callmeike.android.simpletemp.loc.Location
import net.callmeike.android.simpletemp.loc.SimpleLocation
import net.callmeike.android.simpletemp.sync.CouchbaseDataSync
import net.callmeike.android.simpletemp.sync.DataSync
import dagger.Binds
import dagger.Module


@Module
interface DbModule {
    @Binds
    fun bindsDb(nav: CouchbaseDb): Db

    @Binds
    fun bindsAppDao(nav: CouchbaseAppDao): AppDao

    @Binds
    fun bindsProfilesDao(nav: CouchbaseProfilesDao): ProfilesDao

    @Binds
    fun bindsReportsDao(nav: CouchbaseReportsDao): ReportsDao
}


@Module
interface AuthModule {
    @Binds
    fun bindsAuth(auth: SimpleAuth): Auth
}


@Module
interface LocModule {
    @Binds
    fun bindsLoc(auth: SimpleLocation): Location
}


@Module
interface SyncModule {
    @Binds
    fun bindsSync(auth: CouchbaseDataSync): DataSync
}

