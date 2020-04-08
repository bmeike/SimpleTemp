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
package net.callmeike.android.simpletemp.vm

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import net.callmeike.android.simpletemp.crypto.Auth
import net.callmeike.android.simpletemp.db.AppDao
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject


@Module
interface LoginVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(LoginVM::class)
    fun bindViewModel(vm: LoginVM): ViewModel
}

class LoginVM @Inject constructor(
    private val auth: Auth,
    private val nav: Navigator,
    private val dao: AppDao
) : ViewModel() {
    companion object {
        private const val TAG = "LOGINVM"
    }

    val validated: MutableLiveData<Boolean?> = MutableLiveData()

    fun registered() = runBlocking { dao.getApp() != null }

    fun register(password: CharArray) {
        MainScope().launch {
            dao.registerApp(auth.init(password))
            validated.postValue(true)
        }
    }

    fun validate(password: CharArray) {
        MainScope().launch {
            val app = dao.getApp() ?: throw IllegalStateException("attempt to validate uninitialized app")
            validated.postValue(auth.validate(app, password))
        }
    }

    fun nextPage(ctxt: Context) = nav.peoplePage(ctxt)
}
