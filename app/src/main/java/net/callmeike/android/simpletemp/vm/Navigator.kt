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
import android.content.Intent
import net.callmeike.android.simpletemp.LoginActivity
import net.callmeike.android.simpletemp.PeopleActivity
import net.callmeike.android.simpletemp.ProfileActivity
import net.callmeike.android.simpletemp.TempActivity
import dagger.Binds
import dagger.Module
import javax.inject.Inject
import javax.inject.Singleton


@Module
interface NavModule {
    @Binds
    fun bindsNavigator(nav: SimpleTempNavigator): Navigator
}

@Singleton
class SimpleTempNavigator @Inject constructor() : Navigator {
    override fun loginPage(ctxt: Context) {
        val intent = Intent(ctxt, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        ctxt.startActivity(intent)
    }

    override fun peoplePage(ctxt: Context) {
        val intent = Intent(ctxt, PeopleActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        ctxt.startActivity(intent)
    }

    override fun createProfile(ctxt: Context) {
        val intent = Intent(ctxt, ProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        ctxt.startActivity(intent)
    }


    override fun updateProfile(ctxt: Context, id: String) {
        val intent = Intent(ctxt, ProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        intent.putExtra(ProfileActivity.PARAM_PERSON_ID, id)
        ctxt.startActivity(intent)
    }

    override fun getTemp(ctxt: Context, id: String) {
        val intent = Intent(ctxt, TempActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        intent.putExtra(TempActivity.PARAM_PERSON_ID, id)
        ctxt.startActivity(intent)

    }
}
