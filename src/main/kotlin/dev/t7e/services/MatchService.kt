package dev.t7e.services

import dev.t7e.models.*
import dev.t7e.utils.safeValueOf
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

/**
 * Created by testusuke on 2023/05/07
 * @author testusuke
 */
object MatchService: StandardService<MatchEntity, Match>(
    objectName = "match",
    _getAllObjectFunction = { MatchEntity.getAll() },
    _getObjectByIdFunction = { MatchEntity.getById(it) },
    fetchFunction = { MatchEntity.fetch(it) }
) {

    fun update(id: Int, omittedMatch: OmittedMatch): Result<Match> = transaction {
        val match = MatchEntity.getById(id)?.first ?: throw NotFoundException("invalid match id")

        val location = omittedMatch.locationId?.let {
            LocationEntity.getById(it)?.first ?: throw NotFoundException("invalid location id")
        }
        val game = GameEntity.getById(omittedMatch.gameId)?.first ?: throw NotFoundException("invalid game id")
        val sport = SportEntity.getById(omittedMatch.sportId)?.first ?: throw NotFoundException("invalid sport id")
        val leftTeam = omittedMatch.leftTeamId?.let {
            TeamEntity.getById(it)?.first ?: throw NotFoundException("invalid left team id")
        }
        val rightTeam = omittedMatch.rightTeamId?.let {
            TeamEntity.getById(it)?.first ?: throw NotFoundException("invalid right team id")
        }
        val winnerTeam = omittedMatch.winnerId?.let {
            TeamEntity.getById(it)?.first ?: throw NotFoundException("invalid winner team id")
        }

        //  update
        match.location = location
        match.game = game
        match.sport = sport
        match.startAt = LocalDateTime.parse(omittedMatch.startAt)
        match.leftTeam = leftTeam
        match.rightTeam = rightTeam
        match.leftScore = omittedMatch.leftScore
        match.rightScore = omittedMatch.rightScore
        match.winner = winnerTeam
        match.status = omittedMatch.status

        match.updatedAt = LocalDateTime.now()

        Result.success(
            match.serializableModel().apply {
                fetchFunction(this.id)
            }
        )
    }
}