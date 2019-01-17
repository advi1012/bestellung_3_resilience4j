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
package de.hska.bestellung.config

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerExchangeFilterFunction
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions.basicAuthentication

/**
 * Konfiguration für WebClient.
 */
interface WebClientConfig {
    // https://cloud.spring.io/spring-cloud-static/Finchley.RELEASE/single/spring-cloud.html#loadbalanced-webclient
    /**
     * Bean-Function für Lastbalanzierung einschließlich BASIC-Authentifizierung.
     * @param lb Injizierte Funktion für Lastbalanzierung
     */
    @Bean
    fun webClientBuilder(lb: LoadBalancerExchangeFilterFunction) =
        WebClient.builder().filter(lb).filter(basicAuthentication(username, password))

    @Suppress("MayBeConstant")
    companion object {
        /**
         * Benutzername für BASIC-Authentifizierung
         */
        val username = "admin"

        /**
         * Passwort für BASIC-Authentifizierung
         */
        val password = "p"
    }
}
