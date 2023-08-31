package dev.t7e.routes.v1

import dev.t7e.models.LogEvents
import dev.t7e.models.OmittedGame
import dev.t7e.plugins.Role
import dev.t7e.plugins.UserPrincipal
import dev.t7e.plugins.withRole
import dev.t7e.services.GamesService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.logger.Logger
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2023/05/09
 * @author testusuke
 */
fun Route.gamesRouter() {
    route("/games") {
        withRole(Role.USER) {
            /**
             * Get all games
             */
            get {
                val games = GamesService.getAll()

                call.respond(
                    HttpStatusCode.OK,
                    DataResponse(games.getOrDefault(listOf())),
                )
            }

            withRole(Role.ADMIN) {
                /**
                 * Create new game
                 */
                post {
                    val requestBody = call.receive<OmittedGame>()

                    GamesService
                        .create(requestBody)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataMessageResponse(
                                    "created game",
                                    it,
                                ),
                            )
                            //  Logger
                            Logger.commit(
                                "[GamesRouter] created game: ${it.name}",
                                LogEvents.CREATE,
                                call.principal<UserPrincipal>()?.microsoftAccount,
                            )
                        }
                }
            }

            route("/{id?}") {
                /**
                 * Get game by id
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                    GamesService
                        .getById(id)
                        .respondOrInternalError {
                            call.respond(
                                HttpStatusCode.OK,
                                DataResponse(it),
                            )
                        }
                }

                withRole(Role.ADMIN) {
                    /**
                     * Update game
                     */
                    put {
                        val id = call.parameters["id"]?.toIntOrNull()
                            ?: throw BadRequestException("invalid id parameter")
                        val requestBody = call.receive<OmittedGame>()

                        GamesService
                            .update(id, requestBody)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "updated game",
                                        it,
                                    ),
                                )
                                //  Logger
                                Logger.commit(
                                    "[GamesRouter] updated game: ${it.name}",
                                    LogEvents.UPDATE,
                                    call.principal<UserPrincipal>()?.microsoftAccount,
                                )
                            }
                    }

                    /**
                     * Delete game
                     */
                    delete {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        GamesService
                            .deleteById(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    MessageResponse(
                                        "deleted game",
                                    ),
                                )
                                //  Logger
                                Logger.commit(
                                    "[GamesRouter] deleted game: $id",
                                    LogEvents.DELETE,
                                    call.principal<UserPrincipal>()?.microsoftAccount,
                                )
                            }
                    }
                }

                route("/matches") {
                    /**
                     * Get matches
                     */
                    get {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        GamesService
                            .getMatches(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(it),
                                )
                            }
                    }

                    withRole(Role.ADMIN) {
                        /**
                         * Delete all matches
                         */
                        delete {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("invalid id parameter")

                            GamesService
                                .deleteAllMatches(id)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        MessageResponse(
                                            "deleted matches",
                                        ),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] deleted all matches. game: $id",
                                        LogEvents.DELETE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }
                    }
                }

                route("/entries") {
                    /**
                     * Get entries
                     */
                    get {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        GamesService
                            .getEntries(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataResponse(it),
                                )
                            }
                    }

                    withRole(Role.ADMIN) {
                        /**
                         * enter game
                         */
                        post {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("invalid id parameter")
                            val requestBody = call.receive<EntryRequest>()

                            GamesService
                                .enterGame(id, requestBody.teamIds)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        DataMessageResponse(
                                            "entered game",
                                            it,
                                        ),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] entered game: $id",
                                        LogEvents.UPDATE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }

                        /**
                         * leave game
                         */
                        delete("/{teamId}") {
                            val id = call.parameters["id"]?.toIntOrNull()
                                ?: throw BadRequestException("invalid id parameter")
                            val teamId = call.parameters["teamId"]?.toIntOrNull()
                                ?: throw BadRequestException("invalid teamId parameter")

                            GamesService
                                .cancelEntry(id, teamId)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        DataMessageResponse(
                                            "left game",
                                            it,
                                        ),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] left game: $id",
                                        LogEvents.UPDATE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }
                    }
                }

                route("/tournament") {
                    withRole(Role.ADMIN) {
                        /**
                         * Make tournament tree
                         */
                        post {
                            val id =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")
                            val parent = call.receive<TournamentTreeCreateRequest>()

                            GamesService
                                .makeTournamentTree(id, parent.parentId)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        DataMessageResponse(
                                            "made tournament tree",
                                            it,
                                        ),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] made new tournament tree. game: $id match: ${it.id}",
                                        LogEvents.CREATE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }

                        /**
                         * Update tournament tree recursively
                         */
                        post("/update-tree") {
                            val id =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")

                            GamesService
                                .updateTournamentTree(id)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        MessageResponse("updated tournament tree"),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] updated tournament tree. game: $id",
                                        LogEvents.UPDATE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }
                    }

                    /**
                     * Get tournament result
                     */
                    get("/result") {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        GamesService
                            .getTournamentResult(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "calculated tournament result",
                                        it,
                                    ),
                                )
                            }
                    }
                }

                route("/league") {
                    withRole(Role.ADMIN) {
                        /**
                         * Make league matches
                         */
                        post {
                            val id =
                                call.parameters["id"]?.toIntOrNull()
                                    ?: throw BadRequestException("invalid id parameter")
                            val location = call.receive<LocationRequest>()

                            GamesService
                                .makeLeagueMatches(id, location.locationId)
                                .respondOrInternalError {
                                    call.respond(
                                        HttpStatusCode.OK,
                                        DataMessageResponse(
                                            "made league matches",
                                            it,
                                        ),
                                    )
                                    //  Logger
                                    Logger.commit(
                                        "[GamesRouter] made new league matches. game: $id",
                                        LogEvents.CREATE,
                                        call.principal<UserPrincipal>()?.microsoftAccount,
                                    )
                                }
                        }
                    }

                    /**
                     * Get league result
                     */
                    get("/result") {
                        val id =
                            call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")

                        GamesService
                            .calculateLeagueResults(id)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "calculated league results",
                                        it,
                                    ),
                                )
                            }
                    }
                }
            }
        }
    }
}

@Serializable
data class EntryRequest(
    val teamIds: List<Int>,
)

@Serializable
data class LocationRequest(
    val locationId: Int?,
)

@Serializable
data class TournamentTreeCreateRequest(
    val parentId: Int?,
)
