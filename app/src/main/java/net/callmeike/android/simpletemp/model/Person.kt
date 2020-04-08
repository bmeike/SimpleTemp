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
package net.callmeike.android.simpletemp.model

import net.callmeike.android.simpletemp.crypto.AuthUtil

typealias People = List<Person>

data class Person(
    val id: String = AuthUtil.randomString(48),
    val name: String = "",
    val birthYear: Int? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val conditions: List<String>? = null
) {
    companion object {
        const val CONDITION_HEART = "HEART"
        const val CONDITION_RESP = "RESP"
        const val CONDITION_DIABETES = "DIABETES"
        const val CONDITION_DEPRESSION = "DEPRESSION"
    }

    fun update(
        name: String,
        birthYear: Int?,
        street: String?,
        city: String?,
        state: String?,
        zip: String?,
        conditions: List<String>?
    ) =
        Person(
            this.id,
            name,
            birthYear ?: this.birthYear,
            street ?: this.street,
            city ?: this.city,
            state ?: this.state,
            zip ?: this.zip,
            conditions ?: this.conditions
        )
}