package dev.t7e.routes.v1

import dev.t7e.plugins.Role
import dev.t7e.plugins.withRole
import dev.t7e.services.MicrosoftAccountsService
import dev.t7e.utils.DataMessageResponse
import dev.t7e.utils.DataResponse
import dev.t7e.utils.MessageResponse
import dev.t7e.utils.respondOrInternalError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

/**
 * Created by testusuke on 2023/03/06
 * @author testusuke
 */

fun Route.microsoftAccountsRouter() {
    route("/microsoft-accounts") {
        withRole(Role.ADMIN) {
            /**
             * Get all microsoft accounts
             */
            get {
                val accounts = MicrosoftAccountsService.getAll()

                call.respond(HttpStatusCode.OK, DataResponse(accounts.getOrDefault(listOf())))
            }

            route("/{id?}") {

                /**
                 * Get specific microsoft account
                 */
                get {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                    MicrosoftAccountsService.getById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, DataResponse(it))
                        }
                }

                /**
                 * Delete specific microsoft account
                 */
                delete {
                    val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                    MicrosoftAccountsService.deleteById(id)
                        .respondOrInternalError {
                            call.respond(HttpStatusCode.OK, MessageResponse("deleted microsoft account"))
                        }
                }

                route("/role") {
                    /**
                     * Update specific microsoft account role
                     */
                    put {
                        val id = call.parameters["id"]?.toIntOrNull() ?: throw BadRequestException("invalid id parameter")
                        val role = call.receive<AccountRoleRequest>()

                        MicrosoftAccountsService
                            .setAccountRole(id, role.role)
                            .respondOrInternalError {
                                call.respond(
                                    HttpStatusCode.OK,
                                    DataMessageResponse(
                                        "updated account role",
                                        it
                                    )
                                )
                            }
                    }
                }
            }
        }
    }
}

@Serializable
data class AccountRoleRequest(val role: String)