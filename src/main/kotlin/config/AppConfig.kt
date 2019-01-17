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
package de.hska.bestellung.config

import de.hska.bestellung.Router
import de.hska.bestellung.config.dev.DbPopulate
import de.hska.bestellung.config.dev.DiscoveryClientBeispiel
import de.hska.bestellung.config.dev.LogRequest
import de.hska.bestellung.config.dev.MongoMappingEventsListener
import de.hska.bestellung.config.dev.WebClientBeispiel
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.security.config.annotation.web.reactive
        .EnableWebFluxSecurity

/**
 * Konfigurationsklasse für die Anwendung bzw. den Microservice.
 * Konfigurationsklassen werden mit _CGLIB_ verarbeitet.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Configuration
@EnableMongoAuditing
@EnableWebFluxSecurity
@EnableCaching
class AppConfig :
        Router,
        DbConfig,
        DbPopulate,
        DiscoveryClientBeispiel,
        LogRequest,
        MongoMappingEventsListener,
        SecurityConfig,
        WebClientBeispiel,
        WebClientConfig
