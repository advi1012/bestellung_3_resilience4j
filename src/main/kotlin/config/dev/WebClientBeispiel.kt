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
package de.hska.bestellung.config.dev

import de.hska.bestellung.config.Settings.DEV
import de.hska.bestellung.config.logger
import de.hska.bestellung.entity.Kunde
import de.hska.bestellung.service.BestellungService.Companion.kundeService
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.toMono

/**
 * Den Microservice _kunde_ mit WebClient aufrufen.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface WebClientBeispiel {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen,
     * damit der Microservice _kunde_ mit WebClient aufgerufen wird.
     * @param clientBuilder Injizierter WebClient Builder
     * @return CommandLineRunner
     */
    @Bean
    @Profile(DEV)
    fun webClientBeispiel(
        clientBuilder: WebClient.Builder,
        circuitBreakerRegistry: CircuitBreakerRegistry,
        circuitBreakerProperties: CircuitBreakerProperties
    ) = CommandLineRunner {
        val kundeIdVorhanden = "00000000-0000-0000-0000-000000000001"
        val kundeIdNichtVorhanden = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
        val circuitBreaker =
            circuitBreakerRegistry.circuitBreaker("kunde") {
                circuitBreakerProperties.createCircuitBreakerConfig("kunde")
            }
        val logger = logger()

        val client = clientBuilder.build()
        client.get()
            .uri("http://$kundeService/$kundeIdVorhanden")
            .retrieve()
            .bodyToMono<Kunde>()
            .subscribe { kunde ->
                logger.warn("WebClient fuer \"{}\" mit vorhandener ID: {}", kundeService, kunde)
            }

        client.get()
            .uri("http://$kundeService/$kundeIdNichtVorhanden")
            .retrieve()
            .bodyToMono<Kunde>()
            .transform(CircuitBreakerOperator.of(circuitBreaker))
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                logger.warn("onErrorResume: ${it.message}")
                Kunde(nachname = "Dummy", email = "dummy@test.de").toMono()
            }
            .subscribe { kunde ->
                logger.warn("WebClient fuer \"{}\" mit nicht-vorhandener ID: {}", kundeService, kunde)
            }
    }

    // Fuer OAuth siehe
    // https://github.com/bclozel/spring-reactive-university/blob/master/src/...
    //         ...main/java/com/example/integration/gitter/GitterClient.java
}
