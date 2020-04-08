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
package net.callmeike.android.simpletemp.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.callmeike.android.simpletemp.R
import net.callmeike.android.simpletemp.model.People
import net.callmeike.android.simpletemp.model.Person
import net.callmeike.android.simpletemp.vm.PeopleVM

class PeopleAdapter(private val act: Activity, private val vm: PeopleVM) : RecyclerView.Adapter<PeopleViewHolder>() {
    private var people: People? = null
    override fun getItemCount() = people?.size ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        return PeopleViewHolder(
            act,
            vm,
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.row_person, parent, false)
        )
    }

    override fun onBindViewHolder(vh: PeopleViewHolder, pos: Int) = vh.setPerson(people?.getOrNull(pos))

    fun populate(people: People?) {
        this.people = people
        notifyDataSetChanged()
    }
}

class PeopleViewHolder(private val act: Activity, private val vm: PeopleVM, peopleView: View) :
    RecyclerView.ViewHolder(peopleView) {
    private val nameView: TextView = peopleView.findViewById(R.id.name)
    private val tempButton: ImageButton = peopleView.findViewById(R.id.temp)
    private var person: Person? = null

    fun setPerson(person: Person?) {
        this.person = person

        nameView.text = person?.name ?: ""

        nameView.setOnClickListener { vm.updateProfile(act, person) }

        tempButton.setOnClickListener { vm.getTemp(act, person) }
    }
}
