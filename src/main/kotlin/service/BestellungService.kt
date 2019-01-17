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
package de.hska.bestellung.service

import de.hska.bestellung.config.logger
import de.hska.bestellung.entity.Bestellung
import de.hska.bestellung.entity.Kunde
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.onErrorResume
import reactor.core.publisher.toMono
import java.util.UUID.randomUUID

/**
 * Anwendungslogik für Bestellungen.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Service
class BestellungService(
    private val mongoTemplate: ReactiveMongoTemplate,
    private val clientBuilder: WebClient.Builder,
    circuitBreakerRegistry: CircuitBreakerRegistry,
    circuitBreakerProperties: CircuitBreakerProperties
) {
    // Circuit Breaker mit Namen "kunde" und Konfigurationsdaten aus application.yml / Config-Server mit Namen "kunde"
    private val circuitBreaker =
        circuitBreakerRegistry.circuitBreaker("kunde") { circuitBreakerProperties.createCircuitBreakerConfig("kunde") }

    /**
     * Alle Bestellungen ermitteln.
     * @return Alle Bestellungen.
     */
    fun findAll(): Flux<Bestellung> =
        mongoTemplate.findAll(Bestellung::class.java)
            .flatMap { bestellung ->
                findKundeById(bestellung.kundeId)
                    .map { (nachname) ->
                        bestellung.kundeNachname = nachname
                        bestellung
                    }
            }

    /**
     * Eine Bestellung anhand der ID suchen.
     * @param id Die Id der gesuchten Bestellung.
     * @return Die gefundene Bestellung oder ein leeres Mono-Objekt.
     */
    fun findById(id: String) =
        mongoTemplate.findById(id, Bestellung::class.java)
            .flatMap { bestellung ->
                findKundeById(bestellung.kundeId).map { kunde ->
                    logger.trace("Kunde: {}", kunde)
                    bestellung.kundeNachname = kunde?.nachname
                    bestellung
                }
            }

    private fun findKundeById(kundeId: String) =
        clientBuilder.build()
            .get()
            .uri("http://$kundeService/$kundeId")
            .retrieve()
            .bodyToMono<Kunde>()
            // https://stackoverflow.com/questions/49276946/resilience4j-how-to-handle-errors-when-using-a-circuit-breaker-in-a-spring-webfl#answer-49291583
            .transform(CircuitBreakerOperator.of(circuitBreaker))
            .onErrorResume(WebClientResponseException.NotFound::class) {
                logger.warn("onErrorResume: ${it.message}")
                Kunde(nachname = "Dummy", email = "dummy@test.de").toMono()
            }

    /**
     * Bestellungen zur Kunde-ID suchen.
     * @param kundeId Die Id des gegebenen Kunden.
     * @return Die gefundenen Bestellungen oder ein leeres Flux-Objekt.
     */
    fun findByKundeId(kundeId: String) =
        findKundeById(kundeId)
            .flatMapMany { (nachname) ->
                val criteria = Criteria.where("kundeId").regex("\\.*$kundeId\\.*", "i")
                val query = Query(criteria)
                mongoTemplate.find(query, Bestellung::class.java)
                    .map { bestellung ->
                        bestellung.kundeNachname = nachname
                        bestellung
                    }
            }

    /**
     * Eine neue Bestellung anlegen.
     * @param bestellung Das Objekt der neu anzulegenden Bestellung.
     * @return Die neu angelegte Bestellung mit generierter ID.
     */
    fun create(bestellung: Bestellung): Mono<Bestellung> {
        val neueBestellung = bestellung.copy(id = randomUUID().toString())
        return mongoTemplate.save(neueBestellung)
    }

    companion object {
        /**
         * Name des Kunde-Service beim Server für _Service Diccovery_.
         */
        @Suppress("MayBeConstant")
        val kundeService = "kunde"

        private val logger = logger()
    }
}
