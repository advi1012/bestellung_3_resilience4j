/*
 * Copyright (C) 2017 - present Juergen Zimmermann, Hochschule Karlsruhe
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

import org.springframework.boot.Banner
import org.springframework.boot.SpringBootVersion
import org.springframework.core.SpringVersion
import org.springframework.security.core.SpringSecurityCoreVersion
import java.net.InetAddress

/**
 * Singleton-Klasse, um sinnvolle Konfigurationswerte für den Microservice vorzugeben.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
@Suppress("MagicNumber", "MayBeConstant")
object Settings {
    /**
     * Konstante für das Spring-Profile "dev".
     */
    const val DEV = "dev"

    private val version = "1.0"

    private val eurekaPort = 8761

    /**
     * Banner für den Start des Microservice in der Konsole.
     */
    val BANNER = Banner { _, _, out ->
        out.println("""
            |       __                                    _____
            |      / /_  _____  _________ ____  ____     /__  /
            | __  / / / / / _ \/ ___/ __ `/ _ \/ __ \      / /
            |/ /_/ / /_/ /  __/ /  / /_/ /  __/ / / /     / /___
            |\____/\__,_/\___/_/   \__, /\___/_/ /_/     /____(_)
            |                     /____/
            |
            |(C) Juergen Zimmermann, Hochschule Karlsruhe
            |Version          $version
            |Spring Boot      ${SpringBootVersion.getVersion()}
            |Spring Security  ${SpringSecurityCoreVersion.getVersion()}
            |Spring Framework ${SpringVersion.getVersion()}
            |Kotlin           ${KotlinVersion.CURRENT}
            |OpenJDK          ${System.getProperty("java.runtime.version")}
            |Betriebssystem   ${System.getProperty("os.name")}
            |Rechnername      ${InetAddress.getLocalHost().hostName}
            |""".trimMargin("|"))
    }

    private val parentPkgName by lazy {
        val pkgName = Settings::class.java.`package`.name
        pkgName.substringBeforeLast('.')
    }

    private val appName = parentPkgName.substringAfterLast('.')

    /**
     * Properties, die berücksichtigt werden, wenn der Microservice in der
     * Konsole gestartet wird.
     */
    val PROPS = mapOf(
            "spring.application.name" to appName,
            "spring.devtools.livereload.enabled" to false,
            "spring.profiles.default" to "prod",
            "spring.jackson.serialization.indent_output" to true,
            "spring.jackson.default-property-inclusion" to "non_null",
            "spring.reactor.stacktrace-mode.enabled" to true,

            "management.endpoints.web.exposure.include" to "*",
            "management.endpoint.mappings.enabled" to true,
            "management.endpoint.shutdown.enabled" to true,
            // "management.endpoint.health.enabled" to true,
            // "management.endpoint.health.show-details" to true,

            // "eureka.client.securePortEnabled" to true,
            "eureka.client.serviceUrl.defaultZone" to
                    "http://localhost:$eurekaPort/eureka/",

            "eureka.instance.preferIpAddress" to true,
            "spring.sleuth.sampler.probability" to 1.0,

            "spring.thymeleaf.cache" to false,
            "spring.thymeleaf.enabled" to true)
}
