/*
 * Copyright (C) 2016 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.hska.bestellung.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDate
import java.time.LocalDate.now
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import org.springframework.data.annotation.Transient
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * Unveränderliche Daten einer Bestellung. In DDD ist Bestellung ist ein _Aggregate Root_.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @property id ID einer Bestellung als UUID [ID_PATTERN]].
 * @property version Versionsnummber in der DB.
 * @property datum Bestelldatum.
 * @property kundeId ID des zugehörigen Kunden.
 * @property bestellpositionen Liste von [Bestellposition]
 * @property _links HATEOAS-Links, wenn genau 1 JSON-Objekt in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 * @property links HATEOAS-Links, wenn ein JSON-Array in einem Response
 *      zurückgeliefert wird. Die Links werden nicht in der DB gespeichert.
 * @property kundeNachname Nachname des Kunden. Der Nachname wird nicht in der DB gespeichert.
 */
@Document
@TypeAlias("Bestellung")
@JsonPropertyOrder(
        "datum", "kundeId", "kundeNachname", "bestellpositionen")
data class Bestellung(
    @JsonIgnore
    val id: String? = null,

    @Version
    @JsonIgnore
    val version: Int? = null,

    val datum: LocalDate = now(),

    @get:NotNull(message = "{bestellung.kundeId.notNull}")
    @get:Pattern(
            regexp = ID_PATTERN,
            message = "{bestellung.kundeId.pattern}")
    @Indexed(name = "kundeId_idx")
    val kundeId: String,

    @get:NotEmpty(message = "{bestellung.bestellpositionen.notEmpty}")
    @get:Valid
    val bestellpositionen: List<Bestellposition> = emptyList(),

    @CreatedDate
    @JsonIgnore
    private val erzeugt: LocalDateTime? = null,

    @LastModifiedDate
    @JsonIgnore
    private val aktualisiert: LocalDateTime? = null
) {

    @Suppress("PropertyName", "VariableNaming")
    @Transient
    var _links: Map<String, Map<String, String>>? = null

    @Transient
    var links: List<Map<String, String>>? = null

    @Transient
    var kundeNachname: String? = null

    /**
     * Vergleich mit einem anderen Objekt oder null.
     * @param other Das zu vergleichende Objekt oder null
     * @return True, falls das zu vergleichende (Kunde-) Objekt die gleiche
     *      ID und die gleiche Kunde-ID hat.
     */
    @Suppress("ReturnCount")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Bestellung
        return id == other.id && kundeId == other.kundeId
    }

    /**
     * Hashwert aufgrund der Emailadresse.
     * @return Der Hashwert.
     */
    override fun hashCode(): Int {
        val result = id?.hashCode() ?: 0
        @Suppress("MagicNumber")
        return 31 * result + kundeId.hashCode()
    }

    companion object {
        private const val HEX_PATTERN = "[\\dA-Fa-f]"

        /**
         * Muster bzw. regulärer Ausdruck für eine UUID.
         */
        const val ID_PATTERN = "$HEX_PATTERN{8}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{4}-$HEX_PATTERN{12}"
    }
}
