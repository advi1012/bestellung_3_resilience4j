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
package de.hska.bestellung.rest

import de.hska.bestellung.entity.Bestellung
import de.hska.bestellung.Router.Companion.idPathVar
import de.hska.bestellung.rest.util.ifNoneMatch
import de.hska.bestellung.rest.util.itemLinks
import de.hska.bestellung.rest.util.singleLinks
import de.hska.bestellung.service.BestellungService
import org.springframework.http.HttpStatus.NOT_MODIFIED
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.notFound
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.net.URI

/**
 * Eine Handler-Function wird von der Router-Function [de.hska.bestellung.Router.router]
 * aufgerufen, nimmt einen Request entgegen und erstellt den Response.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 *
 * @constructor Einen BestellungHandler mit einem injizierten [BestellungService]
 *      erzeugen.
 */
@Component
class BestellungHandler(private val service: BestellungService) {
    /**
     * Suche anhand der Bestellung-ID
     * @param request Der eingehende Request
     * @return Ein Mono-Objekt mit dem Statuscode 200 und der gefundenen
     *      Bestellung einschließlich HATEOAS-Links, oder aber Statuscode 204.
     */
    fun findById(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable(idPathVar)

        return service.findById(id)
                .flatMap { bestellungToOK(it, request) }
                .switchIfEmpty(ServerResponse.notFound().build())
    }

    private fun bestellungToOK(bestellung: Bestellung, request: ServerRequest): Mono<ServerResponse> {
        val version = bestellung.version
        val versionHeader = request.ifNoneMatch()
        return if (versionHeader != null &&
                compareVersion(version, versionHeader)) {
            status(NOT_MODIFIED).build()
        } else {
            bestellung._links = request.uri().singleLinks()

            // Entity Tag, um Aenderungen an der angeforderten
            // Ressource erkennen zu koennen.
            // Client: GET-Requests mit Header "If-None-Match"
            //         ggf. Response mit Statuscode NOT MODIFIED (s.o.)
            ok().eTag("\"$version\"").body(bestellung.toMono())
        }
    }

    @Suppress("ReturnCount")
    private fun compareVersion(version: Int?, versionHeader: String?): Boolean {
        if (versionHeader == null || version == null) {
            return false
        }
        val versionHeaderInt = try {
            versionHeader.toInt()
        } catch (e: NumberFormatException) {
            false
        }
        return versionHeaderInt == version
    }

    /**
     * Suche mit diversen Suchkriterien als Query-Parameter. Es wird
     * `Mono<List<Bestellung>>` statt `Flux<Bestellung>` zurückgeliefert, damit
     * auch der Statuscode 204 möglich ist.
     * @param request Der eingehende Request mit den Query-Parametern.
     * @return Ein Mono-Objekt mit dem Statuscode 200 und einer Liste mit den
     *      gefundenen Bestellungen einschließlich HATEOAS-Links, oder aber
     *      Statuscode 204.
     */
    @Suppress("ReturnCount")
    fun find(request: ServerRequest): Mono<ServerResponse> {
        val queryParams = request.queryParams()
        if (queryParams.size > 1) {
            return notFound().build()
        }

        val bestellungen = if (queryParams.isEmpty()) {
            service.findAll()
        } else {
            val kundeId = request.queryParam("kundeId")
            if (!kundeId.isPresent) {
                return notFound().build()
            }
            service.findByKundeId(kundeId.get())
        }

        val bestelllungenHateoas = bestellungen.map {
            if (it.id != null) {
                it.links = request.uri().itemLinks(it.id)
            }
            it
        }.collectList()

        return bestelllungenHateoas.flatMap {
            if (it.isEmpty()) notFound().build() else ok().body(it.toMono())
        }
    }

    /**
     * Einen neuen Bestellung-Datensatz anlegen.
     * @param request Der eingehende Request mit dem Bestellung-Datensatz im
     *      Body.
     * @return Response mit Statuscode 201 einschließlich Location-Header oder
     *      Statuscode 400 falls Constraints verletzt sind oder der
     *      JSON-Datensatz syntaktisch nicht korrekt ist.
     */
    fun create(request: ServerRequest) = request.bodyToMono<Bestellung>()
            .flatMap(service::create)
            .flatMap {
                val uri = request.uri()
                val location = URI("$uri/${it.id}")
                created(location).build()
            }
}
