package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.dto.User
import java.util.*

@Repository
class UsersRepository() : CrudRepository<User, Long> {

    private final val users = mutableMapOf<Long, User>()

    override fun <S : User?> save(user: S & Any): S & Any {
        check(user.userId != null);
        users[user.userId] = user;
        return user;
    }

    override fun findById(id: Long): Optional<User> {
        return Optional.ofNullable(
            users[id]
        )
    }

    override fun existsById(id: Long): Boolean {
        return users[id] != null;
    }

    fun existsByUsername(username : String) : Boolean {
        return users.any {
            it.value.username == username
        }
    }

    fun findByUsername(username : String) : Optional<User> {
        return Optional.ofNullable(
            users.values.find {
                it.username == username
            }
        )
    }

    override fun <S : User?> saveAll(entities: MutableIterable<S>): MutableIterable<S> {
        TODO("Not yet implemented")
    }

    override fun findAll(): MutableIterable<User> {
        TODO("Not yet implemented")
    }

    override fun count(): Long {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun deleteAll(entities: MutableIterable<User>) {
        TODO("Not yet implemented")
    }

    override fun deleteAllById(ids: MutableIterable<Long>) {
        TODO("Not yet implemented")
    }

    override fun delete(entity: User) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: Long) {
        TODO("Not yet implemented")
    }

    override fun findAllById(ids: MutableIterable<Long>): MutableIterable<User> {
        TODO("Not yet implemented")
    }

}