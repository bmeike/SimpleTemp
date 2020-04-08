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
package net.callmeike.android.simpletemp.app

import android.app.Application
import net.callmeike.android.simpletemp.bindings.AuthModule
import net.callmeike.android.simpletemp.bindings.DbModule
import net.callmeike.android.simpletemp.bindings.LocModule
import net.callmeike.android.simpletemp.bindings.SyncModule
import net.callmeike.android.simpletemp.db.AppDao
import net.callmeike.android.simpletemp.db.Db
import net.callmeike.android.simpletemp.vm.ViewModelFactory
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton


@Singleton
@Component(modules = [DispatchersModule::class, DbModule::class, AuthModule::class, LocModule::class, SyncModule::class])
interface AppFactory {
    @Component.Builder
    interface Builder {
        fun build(): AppFactory

        @BindsInstance
        fun app(app: SimpleTemp): Builder
    }

    fun db(): Db
    fun appDao(): AppDao
    fun vmFactory(): ViewModelFactory
}


const val APP_NAME = "net.callmeike.android.simpletemp"


var APP: AppFactory? = null
    private set(appFactory) {
        field = appFactory
    }

class SimpleTemp : Application() {
    override fun onCreate() {
        super.onCreate()

        val appFactory = DaggerAppFactory.builder()
            .app(this)
            .build()

        appFactory.db().init(this)

        // warm up the cache
        runBlocking { appFactory.appDao().getApp() }

        APP = appFactory
    }
}
