package dev.t7e.models

import dev.t7e.utils.Cache
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Created by testusuke on 2023/02/25
 * @author testusuke
 */
object Classes : IntIdTable("classes") {
    val name = varchar("name", 64)
    val description = varchar("description", 128).nullable()
    val group = reference("group", Groups)
    val createdAt = datetime("created_at")
}

class ClassEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ClassEntity>(Classes) {
        val getAllClasses = Cache.memoizeOneObject {
            transaction {
                ClassEntity.all().toList().map {
                    it to it.serializableModel()
                }
            }
        }

        val getClass: (id: Int) -> Pair<ClassEntity, ClassModel>? = Cache.memoize { id ->
            transaction {
                ClassEntity
                    .findById(id)
                    ?.let {
                        it to it.serializableModel()
                    }
            }
        }

        val getClassUsers: (id: Int) -> List<Pair<UserEntity, User>>? = Cache.memoize { id ->
            getClass(id)
                ?.let {
                    transaction {
                        it.first.users.toList().map { user ->
                            user to user.serializableModel()
                        }
                    }
                }
        }
    }

    var name by Classes.name
    var description by Classes.description
    var group by GroupEntity referencedOn Classes.group
    var createdAt by Classes.createdAt
    val users by UserEntity referrersOn Users.classEntity

    fun serializableModel(): ClassModel {
        return ClassModel(
            id.value,
            name,
            description,
            group.id.value,
            createdAt.toString()
        )
    }
}

@Serializable
data class ClassModel(
    val id: Int,
    val name: String,
    val description: String?,
    val groupId: Int,
    val createdAt: String
)

@Serializable
data class OmittedClassModel(
    val name: String,
    val description: String?,
    val groupId: Int
)