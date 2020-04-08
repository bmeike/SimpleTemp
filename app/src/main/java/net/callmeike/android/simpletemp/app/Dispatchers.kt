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

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import javax.inject.Named
import javax.inject.Singleton


@Module
object DispatchersModule {
    private val dbDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val netDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Provides
    @JvmStatic
    @Singleton
    @Named("db")
    fun dbDispatcher(): CoroutineDispatcher =
        dbDispatcher

    @Provides
    @JvmStatic
    @Singleton
    @Named("net")
    fun netDispatcher(): CoroutineDispatcher =
        netDispatcher
}
