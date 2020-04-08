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
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Button
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import net.callmeike.android.simpletemp.app.APP
import net.callmeike.android.simpletemp.view.afterTextChanged
import net.callmeike.android.simpletemp.vm.LoginVM
import net.callmeike.android.simpletemp.vm.VMFactory
import kotlinx.android.synthetic.main.activity_login.toolbar
import kotlinx.android.synthetic.main.content_login.instructions
import kotlinx.android.synthetic.main.content_login.login
import kotlinx.android.synthetic.main.content_login.password
import javax.inject.Inject


class LoginActivity : AppCompatActivity() {
    @Inject
    lateinit var vmFactory: VMFactory

    private lateinit var viewModel: LoginVM

    companion object {
        // must match instruction text
        private const val PWD_MIN_LEN = 6
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_login, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_about -> {
                showAbout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        APP?.vmFactory()?.inject(this)

        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this, vmFactory).get(LoginVM::class.java)

        viewModel.validated.observe(
            this,
            Observer { valid ->
                if (valid != null) {
                    validated(valid)
                    viewModel.validated.postValue(null)
                }
            })

        val registered = viewModel.registered()
        if (!registered) {
            login.text = getText(R.string.action_register)
            instructions.text = getText(R.string.register_ins)
        }

        password.afterTextChanged { s -> login.isEnabled = s.length >= PWD_MIN_LEN }

        login.setOnClickListener { login(registered) }
    }

    private fun login(registered: Boolean) {
        val pwd = password.text.toString().toCharArray()
        password.setText("")
        if (registered) {
            viewModel.validate(pwd)
        } else {
            viewModel.register(pwd)
        }
    }

    private fun validated(valid: Boolean) {
        if (!valid) {
            Toast.makeText(this, R.string.retry, Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.nextPage(this)
        finish()
    }

    private fun showAbout() {
        val popUpView = layoutInflater.inflate(R.layout.popup_about_login, null)
        val popup = PopupWindow(
            popUpView,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )

        val dismissButton: Button = popUpView.findViewById(R.id.button_dismiss)
        dismissButton.setOnClickListener { popup.dismiss() }

        val webView: WebView = popUpView.findViewById(R.id.about_text)
        webView.loadDataWithBaseURL(null, getString(R.string.about_login), "text/html", "utf-8", null)

        popup.animationStyle = android.R.style.Animation_Dialog
        popup.showAtLocation(popUpView, Gravity.CENTER, 0, 0)
    }
}
