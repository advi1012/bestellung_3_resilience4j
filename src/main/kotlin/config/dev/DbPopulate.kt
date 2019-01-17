/*
 * Copyright (C) 2016 - 2018 Juergen Zimmermann, Hochschule Karlsruhe
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

import com.mongodb.reactivestreams.client.MongoCollection
import de.hska.bestellung.config.Settings.DEV
import de.hska.bestellung.config.logger
import de.hska.bestellung.entity.Bestellung
import de.hska.bestellung.entity.Bestellung.Companion.ID_PATTERN
import org.bson.Document
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Description
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.createCollection
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.array
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.date
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import reactor.core.publisher.Mono

/**
 * Interface, um im Profil _dev_ die (Test-) DB neu zu laden.
 *
 * @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@HS-Karlsruhe.de)
 */
interface DbPopulate {
    /**
     * Bean-Definition, um einen CommandLineRunner für das Profil "dev" bereitzustellen,
     * damit die (Test-) DB neu geladen wird.
     * @param mongoTemplate Template für MongoDB
     * @return CommandLineRunner
     */
    @Bean
    @Description("DB neu laden")
    @Profile(DEV)
    fun dbPopulate(mongoTemplate: ReactiveMongoTemplate) = CommandLineRunner {
        val logger = logger()
        logger.warn("Neuladen der Collection 'Bestellung'")

        mongoTemplate.dropCollection(Bestellung::class.java)
                // Mono<Void> -> Mono<...>
                .then(createSchema(mongoTemplate))
                // Mono -> Flux
                .thenMany(bestellungen)
                .flatMap { bestellung -> mongoTemplate.insert(bestellung) }
                .subscribe { bestellung -> logger.warn("{}", bestellung) }
    }

    private fun createSchema(mongoTemplate: ReactiveMongoTemplate): Mono<MongoCollection<Document>> {
        // https://docs.mongodb.com/manual/core/schema-validation/
        // https://www.mongodb.com/blog/post/mongodb-36-json-schema-validation-expressive-query-syntax
        val schema = MongoJsonSchema.builder()
                .required("id", "datum", "kundeId", "bestellpositionen")
                .properties(
                        string("id").matching(ID_PATTERN),
                        date("datum"),
                        string("kundeId").matching(ID_PATTERN),
                        array("bestellpositionen").uniqueItems(true))
                .build()

        val logger = logger()
        logger.info("JSON Schema fuer Bestellung: {}", schema.toDocument().toJson())
        return mongoTemplate.createCollection<Bestellung>(CollectionOptions.empty().schema(schema))
    }
}
