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
import net.callmeike.android.simpletemp.db.ReportsDao
import net.callmeike.android.simpletemp.loc.Location
import net.callmeike.android.simpletemp.model.Report
import net.callmeike.android.simpletemp.sync.DataSync
import net.callmeike.android.simpletemp.view.fahrenheitToCentigrade
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject


@Module
interface TempVMModule {
    @Binds
    @IntoMap
    @ViewModelKey(TempVM::class)
    fun bindViewModel(vm: TempVM): ViewModel
}

class TempVM @Inject constructor(
    private val auth: Auth,
    private val loc: Location,
    private val nav: Navigator,
    private val dao: ReportsDao,
    private val net: DataSync
) : ViewModel() {
    val temps: MutableLiveData<List<Pair<String, Float>>> = MutableLiveData()

    fun checkLocationPermission() = loc.neededPermissions()

    fun checkLocationEnabled() = loc.enabledProviders().isNotEmpty()

    fun recordTemp(
        id: String,
        temperature: Float,
        symptomCough: Boolean,
        symptomTired: Boolean,
        symptomBreathing: Boolean
    ) {
        val currentTemp = temperature.fahrenheitToCentigrade()

        val symptoms = mutableListOf<String>()
        if (symptomCough) {
            symptoms.add(Report.SYMPTOM_COUGH)
        }
        if (symptomTired) {
            symptoms.add(Report.SYMPTOM_TIRED)
        }
        if (symptomBreathing) {
            symptoms.add(Report.SYMPTOM_BREATH)
        }

        MainScope().launch {
            val currentLoc = loc.getMostRecentLocation()

            dao.postReport(
                Report(
                    auth.getHashedAppId(),
                    auth.hash(id),
                    currentLoc,
                    currentTemp,
                    symptoms
                )
            )
            if (currentLoc != null) {
                net.sync()
            }
        }
    }

    fun getRecentReports(id: String) {
        MainScope().launch {
            temps.postValue(dao.getRecentReports(auth.hash(id)))
        }
    }

    fun back(ctxt: Context) = nav.peoplePage(ctxt)
}
