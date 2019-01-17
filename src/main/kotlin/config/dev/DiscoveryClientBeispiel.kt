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
package de.hska.bestellung.config.dev

import de.hska.bestellung.config.Settings.DEV
import de.hska.bestellung.config.logger
import de.hska.bestellung.service.BestellungService.Companion.kundeService
import org.springframework.boot.CommandLineRunner
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.context.annotation.Profile

/**
 * Registrierte Services fuer den aufzurufenden Microservice "kunde"
 * protokollieren.
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface DiscoveryClientBeispiel {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen,
     * damit _Service Discovery_ für den Service _kunde_ abgefragt wird.
     * @param discoveryClient DiscoveryClient für Service Discovery.
     * @return CommandLineRunner
     */
    @Bean
    @Description("Ausgabe der registrierten Services")
    @Profile(DEV)
    fun logDiscoveryClientBeispiel(discoveryClient: DiscoveryClient) = CommandLineRunner {
        val logger = logger()
        val services = discoveryClient.services
        val serviceInstanzen = discoveryClient.getInstances(kundeService)

        logger.warn("Registrierte Services: $services")

        if (serviceInstanzen.isEmpty()) {
            logger.error("Kein Service mit dem Namen {} registriert", kundeService)
        } else {
            logger.warn("Instanzen des Service $kundeService:")
            serviceInstanzen.forEach { serviceInstance ->
                logger.warn("   URI = {}", serviceInstance.uri)
            }
        }
    }
}
