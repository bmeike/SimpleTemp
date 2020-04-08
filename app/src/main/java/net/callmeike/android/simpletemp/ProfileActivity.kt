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
package net.callmeike.android.simpletemp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.callmeike.android.simpletemp.app.APP
import net.callmeike.android.simpletemp.model.Person
import net.callmeike.android.simpletemp.view.afterTextChanged
import net.callmeike.android.simpletemp.vm.ProfileVM
import net.callmeike.android.simpletemp.vm.VMFactory
import kotlinx.android.synthetic.main.activity_profile.toolbar
import kotlinx.android.synthetic.main.content_profile.add
import kotlinx.android.synthetic.main.content_profile.birth_year
import kotlinx.android.synthetic.main.content_profile.city
import kotlinx.android.synthetic.main.content_profile.condition_depression
import kotlinx.android.synthetic.main.content_profile.condition_diabetes
import kotlinx.android.synthetic.main.content_profile.condition_heart
import kotlinx.android.synthetic.main.content_profile.condition_respiratory
import kotlinx.android.synthetic.main.content_profile.name
import kotlinx.android.synthetic.main.content_profile.state
import kotlinx.android.synthetic.main.content_profile.street
import kotlinx.android.synthetic.main.content_profile.zip
import javax.inject.Inject


fun String.toIntOrDefault(i: Int) = try {
    this.toInt()
} catch (_: NumberFormatException) {
    i
}

class ProfileActivity : AppCompatActivity() {
    companion object {
        const val PARAM_PERSON_ID = "simpletemp.PERSON_ID"
        private const val NAME_MIN_LEN = 2
    }

    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: ProfileVM

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_person_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(ProfileVM::class.java)

        viewModel.profile.observe(this, Observer { person -> populate(person) })

        val id = intent.getStringExtra(PARAM_PERSON_ID)
        if (id == null) {
            viewModel.createProfile()
            add.isEnabled = false
        } else {
            viewModel.lookupProfile(id)
            add.setText(R.string.action_update)
        }

        name.afterTextChanged { s -> add.isEnabled = s.length >= NAME_MIN_LEN }

        add.setOnClickListener {
            updateProfile()
            viewModel.back(this)
        }
    }

    private fun populate(person: Person?) {
        person ?: return

        name.setText(person.name)
        birth_year.setText(person.birthYear?.toString() ?: "")

        street.setText(person.street)
        city.setText(person.city)
        state.setText(person.state)
        zip.setText(person.zip)

        condition_heart.isChecked = person.conditions?.contains(Person.CONDITION_HEART) ?: false
        condition_respiratory.isChecked = person.conditions?.contains(Person.CONDITION_RESP) ?: false
        condition_diabetes.isChecked = person.conditions?.contains(Person.CONDITION_DIABETES) ?: false
        condition_depression.isChecked = person.conditions?.contains(Person.CONDITION_DEPRESSION) ?: false
    }

    private fun updateProfile() {
        val profileName = name.text.toString()
        if (profileName.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_name, Toast.LENGTH_SHORT).show()
            return
        }

        val sBirthYear = birth_year.text.toString()
        var iBirthYear = 0
        if (sBirthYear.isNotEmpty()) {
            iBirthYear = sBirthYear.toIntOrDefault(-1)
            if ((iBirthYear < 1900) or (iBirthYear > 2022)) {
                Toast.makeText(this, R.string.error_birth_year, Toast.LENGTH_SHORT).show()
                return
            }
        }

        val zipcode = zip.text.toString().trim()
        if ((zipcode.isNotEmpty()) and ((zipcode.length != 5) or (zipcode.toIntOrDefault(0) == 0))) {
            Toast.makeText(this, R.string.error_zip, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfile(
            profileName,
            iBirthYear,
            street.text.toString(),
            city.text.toString(),
            state.text.toString(),
            zipcode,
            condition_heart.isChecked,
            condition_respiratory.isChecked,
            condition_diabetes.isChecked,
            condition_depression.isChecked
        )
    }
}
