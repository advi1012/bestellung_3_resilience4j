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

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails
        .MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User

/**
 * Security-Konfiguration.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface SecurityConfig {
    /**
     * Bean-Definition, um den Zugriffsschutz an der REST-Schnittstelle zu
     * konfigurieren.
     *
     * @param http Injiziertes Objekt von `ServerHttpSecurity` als
     *      Ausgangspunkt für die Konfiguration.
     * @return Objekt von `SecurityWebFilterChain`.
     */
    @Suppress("HasPlatformType")
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity) =
        http.authorizeExchange()
                .matchers(EndpointRequest.to("health")).permitAll()
                .matchers(EndpointRequest.toAnyEndpoint()).hasRole(endpointAdmin)
                .anyExchange().authenticated()

                .and()
                .httpBasic()

                .and()
                // keine generierte HTML-Seite fuer Login
                .formLogin().disable()
                .csrf().disable()
                // TODO Disable FrameOptions: Clickjacking
                .build()

    /**
     * Bean-Definition, um den Administrations-User im Hauptspeicher
     * bereitzustellen.
     *
     * @return Objekt von `MapReactiveUserDetailsService`
     */
    @Bean
    fun reactiveUserDetailsService(): MapReactiveUserDetailsService {
        @Suppress("DEPRECATION")
        val admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("p")
                .roles(admin, endpointAdmin)
                .build()
        return MapReactiveUserDetailsService(admin)
    }

    @Suppress("MayBeConstant")
    companion object {
        private val admin = "ADMIN"
        private val endpointAdmin = "ACTUATOR"
    }
}
