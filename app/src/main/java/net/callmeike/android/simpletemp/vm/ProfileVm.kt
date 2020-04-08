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
import net.callmeike.android.simpletemp.db.ProfilesDao
import net.callmeike.android.simpletemp.model.Person
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@Module
interface ProfileVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(ProfileVM::class)
    fun bindViewModel(vm: ProfileVM): ViewModel
}

class ProfileVM @Inject constructor(
    private val nav: Navigator,
    private val dao: ProfilesDao
) : ViewModel() {
    val profile: MutableLiveData<Person> = MutableLiveData()

    fun createProfile() = profile.postValue(Person())

    fun lookupProfile(id: String) = MainScope().launch {
        val person = dao.getPerson(id)
        profile.postValue(person ?: Person())
    }

    fun updateProfile(
        name: String,
        age: Int?,
        street: String,
        city: String,
        state: String,
        zip: String,
        condHeart: Boolean,
        condResp: Boolean,
        condDiabetes: Boolean,
        condDepression: Boolean
    ) {
        val person = profile.value!!

        val conditions = mutableListOf<String>()
        if (condHeart) {
            conditions.add(Person.CONDITION_HEART)
        }
        if (condResp) {
            conditions.add(Person.CONDITION_RESP)
        }
        if (condDiabetes) {
            conditions.add(Person.CONDITION_DIABETES)
        }
        if (condDepression) {
            conditions.add(Person.CONDITION_DEPRESSION)
        }

        val updatedProfile = person.update(
            name,
            age,
            if (street.isEmpty()) null else street,
            if (city.isEmpty()) null else city,
            if (state.isEmpty()) null else state,
            if (zip.isEmpty()) null else zip,
            conditions
        )

        MainScope().launch {
            dao.saveProfile(updatedProfile)
        }
    }

    fun back(ctxt: Context) = nav.peoplePage(ctxt)
}
