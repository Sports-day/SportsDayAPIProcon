package net.sportsday.services

import io.ktor.server.plugins.*
import net.sportsday.models.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2024/03/25
 * @author testusuke
 */
object AuthenticationService {
    private val isSecure = System.getenv("COOKIE_SECURE")?.toBoolean() ?: false

    fun getIsSecure(): Boolean = isSecure

    fun login(): User {
        //  find user by email
        val userModel = transaction {
            val user = UserEntity.find {
                Users.email eq "demo1@toyama.kosen-ac.jp"
            }.firstOrNull()

            //  create user if not found
            if (user == null) {
                throw BadRequestException("demo user not found")
            }

            user.serializableModel()
        }


        return userModel
    }
}
