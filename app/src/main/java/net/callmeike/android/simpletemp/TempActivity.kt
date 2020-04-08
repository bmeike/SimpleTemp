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

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.callmeike.android.simpletemp.app.APP
import net.callmeike.android.simpletemp.view.afterTextChanged
import net.callmeike.android.simpletemp.view.centigradeToFahrenheit
import net.callmeike.android.simpletemp.vm.TempVM
import net.callmeike.android.simpletemp.vm.VMFactory
import kotlinx.android.synthetic.main.activity_profile.toolbar
import kotlinx.android.synthetic.main.content_profile.add
import kotlinx.android.synthetic.main.content_temp.symptom_breathing
import kotlinx.android.synthetic.main.content_temp.symptom_cough
import kotlinx.android.synthetic.main.content_temp.symptom_tired
import kotlinx.android.synthetic.main.content_temp.temp
import javax.inject.Inject


class TempActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "ACT_TEMP"

        const val PARAM_PERSON_ID = "simpletemp.PERSON_ID"

        private const val CODE_PERM_REQ = 777
    }

    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: TempVM

    private lateinit var profileId: String

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

        setContentView(R.layout.activity_temp)
        setSupportActionBar(toolbar)

        profileId = intent.getStringExtra(ProfileActivity.PARAM_PERSON_ID)!!

        viewModel = ViewModelProviders.of(this, vmFactory).get(TempVM::class.java)

        viewModel.temps.observe(this, Observer { temps -> graphTemps(temps) })

        viewModel.getRecentReports(profileId)

        val neededPermissions = viewModel.checkLocationPermission()
        if (neededPermissions.isEmpty()) {
            if (!viewModel.checkLocationEnabled()) {
                requestLocationServices()
            }
        } else {
            val needRational = neededPermissions
                .map { p -> ActivityCompat.shouldShowRequestPermissionRationale(this, p) }
                .reduce { x, y -> x || y }
            if (needRational) {
                showPermissionsRationalDialog(neededPermissions)
            } else {
                requestPermissions(neededPermissions)
            }
        }

        // !!! Fahrenheit coded in, for now...
        temp.afterTextChanged { s ->
            val temp = s.toFloatOrNull()
            add.isEnabled = temp?.let { (it > 80) && (it < 110) } ?: false
        }

        add.setOnClickListener { recordTemp() }
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<String>, grants: IntArray) {
        when (code) {
            CODE_PERM_REQ -> {
                if ((grants.contains(PackageManager.PERMISSION_GRANTED))) {
                    requestLocationServices()
                }
            }
        }
    }

    private fun recordTemp() {
        viewModel.recordTemp(
            profileId,
            temp.text.toString().toFloat(),
            symptom_cough.isChecked,
            symptom_tired.isChecked,
            symptom_breathing.isChecked
        )
        viewModel.back(this)
    }


    private fun graphTemps(temps: List<Pair<String, Float>>?) {
        temps ?: return

        for (temp in temps) {
            Log.d(TAG, "@${temp.first}: ${temp.second.centigradeToFahrenheit()}")
        }
    }

    private fun showPermissionsRationalDialog(perms: List<String>) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.location_rational)
            .setCancelable(false)
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.cancel()
                requestPermissions(perms)
            }
        builder.create().show()
    }

    private fun requestLocationServices() {
        if (viewModel.checkLocationEnabled()) {
            return
        }
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.location_request)
            .setCancelable(false)
            .setPositiveButton(R.string.yes) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.no) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    private fun requestPermissions(perms: List<String>) =
        ActivityCompat.requestPermissions(this, perms.toTypedArray(), CODE_PERM_REQ)
}

