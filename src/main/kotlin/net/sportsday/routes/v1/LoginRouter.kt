package net.sportsday.routes.v1

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import net.sportsday.services.AuthenticationService
import net.sportsday.utils.DataMessageResponse
import net.sportsday.utils.JwtConfig
import net.sportsday.utils.MessageResponse

/**
 * Created by testusuke on 2024/03/25
 * @author testusuke
 */

fun Route.loginRouter() {
    route("/login") {
        post {
            val code = call.receive<OpenIDConnectCode>()

            try {
                //  login
                val user = AuthenticationService.login(code.code)

                if (user == null) {
                    call.respond(HttpStatusCode.Unauthorized, MessageResponse("Unauthorized"))
                    return@post
                }

                //  issue JWT token
                val jwt = JwtConfig.makeToken(user.id.toString())

                //  set token as http only cookie
                call.response.cookies.append(
                    name = "access_token",
                    value = jwt,
                    httpOnly = false,
                    secure = AuthenticationService.getIsSecure(),
                    maxAge = JwtConfig.getExpirationDuration(),
                    domain = JwtConfig.getIssuer(),
                    path = "/",
                )

                call.respond(HttpStatusCode.OK, DataMessageResponse("Authorized", mapOf("token" to jwt)))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, MessageResponse("Internal Server Error"))
            }
        }
    }
}

@Serializable
data class OpenIDConnectCode(
    val code: String,
)
