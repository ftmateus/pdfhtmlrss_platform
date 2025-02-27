package pt.unl.fct.di.pdf_html_rss_core.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import pt.unl.fct.di.pdf_html_rss_core.data.User
import java.util.*

@Repository
interface UsersRepository : CrudRepository<User, Long> {

//    @Query(
//        value = "SELECT username FROM User u WHERE u.username == :username"
//    )
    fun findByUsername(
        @Param("username")
        username : String
    ) : Optional<User>;

//    @Query(
//        value = "SELECT " +
//                "case when count(c)> 0 then true else false end " +
//                "FROM User u WHERE u.username == :username"
//    )
    fun existsByUsername(
        @Param("username")
        username : String
    ) : Boolean;
}