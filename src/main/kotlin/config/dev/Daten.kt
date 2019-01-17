/*
 * Copyright (C) 2018 - present Juergen Zimmermann, Hochschule Karlsruhe
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
@file:Suppress("MayBeConstant")

package de.hska.bestellung.config.dev

import de.hska.bestellung.entity.Bestellposition
import de.hska.bestellung.entity.Bestellung
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDate

private val kundeId1 = "00000000-0000-0000-0000-000000000001"
private val kundeId2 = "00000000-0000-0000-0000-000000000002"
private val kundeId4 = "00000000-0000-0000-0000-000000000004"

private val artikelId1 = "20000000-0000-0000-0000-000000000001"
private val artikelId2 = "20000000-0000-0000-0000-000000000002"
private val artikelId3 = "20000000-0000-0000-0000-000000000003"
private val artikelId4 = "20000000-0000-0000-0000-000000000004"
private val artikelId5 = "20000000-0000-0000-0000-000000000005"
private val artikelId6 = "20000000-0000-0000-0000-000000000006"

/**
 * Testdaten für Bestellungen
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@SuppressWarnings("MagicNumber")
val bestellungen = Flux.just(
        Bestellung(
                id = "10000000-0000-0000-0000-000000000001",
                datum = LocalDate.of(2018, 1, 1),
                kundeId = kundeId1,
                bestellpositionen = listOf(
                        Bestellposition(
                                artikelId = artikelId1,
                                einzelpreis = BigDecimal("10"),
                                anzahl = 1),
                        Bestellposition(
                                artikelId = artikelId2,
                                einzelpreis = BigDecimal("20"),
                                anzahl = 1)
                )),
        Bestellung(
                id = "10000000-0000-0000-0000-000000000002",
                datum = LocalDate.of(2018, 1, 2),
                kundeId = kundeId1,
                bestellpositionen = listOf(
                        Bestellposition(
                                artikelId = artikelId3,
                                einzelpreis = BigDecimal("30"),
                                anzahl = 3),
                        Bestellposition(
                                artikelId = artikelId4,
                                einzelpreis = BigDecimal("40"),
                                anzahl = 4)
                )),
        Bestellung(
                id = "10000000-0000-0000-0000-000000000003",
                datum = LocalDate.of(2018, 1, 3),
                kundeId = kundeId1,
                bestellpositionen = listOf(
                        Bestellposition(
                                artikelId = artikelId5,
                                einzelpreis = BigDecimal("50"),
                                anzahl = 5),
                        Bestellposition(
                                artikelId = artikelId6,
                                einzelpreis = BigDecimal("60"),
                                anzahl = 6)
                )),
        Bestellung(
                id = "10000000-0000-0000-0000-000000000004",
                datum = LocalDate.of(2018, 1, 4),
                kundeId = kundeId2,
                bestellpositionen = listOf(Bestellposition(
                        artikelId = artikelId1,
                        einzelpreis = BigDecimal("10"),
                        anzahl = 1))),
        Bestellung(
                id = "10000000-0000-0000-0000-000000000005",
                datum = LocalDate.of(2018, 1, 5),
                kundeId = kundeId4,
                bestellpositionen = listOf(Bestellposition(
                        artikelId = artikelId1,
                        einzelpreis = BigDecimal("10"),
                        anzahl = 1))),
        Bestellung(
                id = "10000000-0000-0000-0000-000000000010",
                datum = LocalDate.of(2018, 1, 31),
                kundeId = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF",
                bestellpositionen = listOf(Bestellposition(
                        artikelId = artikelId2,
                        einzelpreis = BigDecimal("20"),
                        anzahl = 2)))
)
