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
package de.hska.bestellung.rest

import de.hska.bestellung.config.Settings.DEV
import de.hska.bestellung.config.logger
import de.hska.bestellung.entity.Bestellposition
import de.hska.bestellung.entity.Bestellung
import de.hska.bestellung.entity.Bestellung.Companion.ID_PATTERN
import java.math.BigDecimal.TEN
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
        .RANDOM_PORT
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.body
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.toMono

@Tag("rest")
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@TestPropertySource(locations = ["/rest-test.properties"])
@DisplayName("REST-Schnittstelle fuer Bestellungen testen")
@Suppress("ClassName")
class BestellungRestTest(@LocalServerPort private val port: Int) {
    private lateinit var client: WebClient

    private lateinit var baseUrl: String

    @BeforeAll
    @Suppress("unused")
    fun beforeAll() {
        val schema = "http"
        baseUrl = "$schema://$HOST:$port"
        LOGGER.info("baseUrl = {}", baseUrl)
        client = WebClient.builder()
                .filter(basicAuthentication(USERNAME, PASSWORD))
                .baseUrl(baseUrl)
                .build()
    }

    // -------------------------------------------------------------------------
    // L E S E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Lesen {
        @ParameterizedTest
        @ValueSource(strings = [ID_VORHANDEN])
        fun `Suche mit vorhandener ID`(id: String) {
            // act
            val bestellung = client.get()
                    .uri(ID_PATH, id)
                    .retrieve()
                    .bodyToMono<Bestellung>()
                    .block()!!

            // assert
            assertAll(
                    { assertTrue(bestellung.kundeNachname!!.isNotEmpty()) },
                    { assertNotEquals("Dummy", bestellung.kundeNachname) },
                    {
                        assertTrue(bestellung._links!!["self"]!!["href"]
                        !!.endsWith("/$id"))
                    }
            )
        }

        @ParameterizedTest
        @ValueSource(strings = [ID_INVALID, ID_NICHT_VORHANDEN])
        fun `Suche mit syntaktisch ungueltiger oder nicht-vorhandener ID`() {
            // arrange
            val id = ID_INVALID

            // act
            val response = client.get()
                    .uri(ID_PATH, id)
                    .exchange()
                    .block()!!

            // assert
            assertEquals(NOT_FOUND, response.statusCode())
        }

        @Test
        fun `Suche nach allen Bestellungen`() {
            // act
            val bestellungen = client.get()
                    .retrieve()
                    .bodyToFlux<Bestellung>()
                    .collectList()
                    .block()!!

            // assert
            assertTrue(bestellungen.isNotEmpty())
        }

        @ParameterizedTest
        @ValueSource(strings = [KUNDE_ID])
        fun `Suche mit vorhandener Kunde-ID`(kundeId: String) {
            // act
            val bestellungen = client.get()
                    .uri {
                        it.path(BESTELLUNG_PATH)
                                .queryParam(KUNDE_ID_PARAM, kundeId)
                                .build()
                    }
                    .retrieve()
                    .bodyToFlux<Bestellung>()
                    .collectList()
                    .block()!!

            // assert
            assertAll(
                    { assertTrue(bestellungen.isNotEmpty()) },
                    {
                        bestellungen.map {
                            assertEquals(kundeId.toLowerCase(),
                                    it.kundeId.toLowerCase())
                        }
                    }
            )
        }
    }

    // -------------------------------------------------------------------------
    // S C H R E I B E N
    // -------------------------------------------------------------------------
    @Nested
    inner class Schreiben {
        @ParameterizedTest
        @CsvSource("$KUNDE_ID, $ARTIKEL_ID")
        fun `Abspeichern einer neuen Bestellung`(kundeId: String, artikelId: String) {
            // arrange
            val bestellposition = Bestellposition(
                    artikelId = artikelId,
                    anzahl = 1,
                    einzelpreis = TEN)
            val bestellpositionen = listOf(bestellposition)
            val neueBestellung = Bestellung(
                    kundeId = kundeId,
                    bestellpositionen = bestellpositionen)

            // act
            val response = client.post()
                    .body(neueBestellung.toMono())
                    .exchange()
                    .block()!!

            // assert
            with(response) {
                assertAll(
                        { assertEquals(CREATED, statusCode()) },
                        { assertNotNull(headers()) },
                        {
                            val location = headers().asHttpHeaders().location
                            assertNotNull(location)
                            val locationStr = location.toString()
                            assertTrue(locationStr.isNotEmpty())
                            val indexLastSlash = locationStr.lastIndexOf('/')
                            assertTrue(indexLastSlash > 0)
                            val idStr = locationStr.substring(indexLastSlash + 1)
                            assertTrue(idStr.matches(Regex(ID_PATTERN)))
                        }
                )
            }
        }
    }

    @Suppress("MayBeConstant")
    private companion object {
        val HOST = "localhost"
        val BESTELLUNG_PATH = "/"
        val ID_PATH = "/{id}"
        val KUNDE_ID_PARAM = "kundeId"

        const val USERNAME = "admin"
        const val PASSWORD = "p"

        const val ID_VORHANDEN = "10000000-0000-0000-0000-000000000001"
        const val ID_INVALID = "YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY"
        const val ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999"
        const val KUNDE_ID = "00000000-0000-0000-0000-000000000001"
        const val ARTIKEL_ID = "20000000-0000-0000-0000-000000000001"

        val LOGGER = logger()
    }
}
