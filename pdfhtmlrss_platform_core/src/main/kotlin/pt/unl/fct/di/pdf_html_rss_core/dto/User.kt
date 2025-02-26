package pt.unl.fct.di.pdf_html_rss_core.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.KeyPair
import java.util.stream.Collectors
import javax.persistence.*

@JsonDeserialize
@Entity
@Table(name = "users")
class User(
    @Id @Column(name = "userid")
    @JsonProperty
    var userId : Long,

    //TODO solve clash
    @Column(name = "username")
    @JsonProperty
    private var username : String,

    @Column(name = "passwordhash")
    @JsonIgnore
    var passwordHash : String,

    @Transient
    @JsonIgnore
    var passwordClear : String?,

    @Column(name = "role")
    @JsonIgnore
    var role : String

) : UserDetails {
    constructor() : this(-1, "", "", "", "ROLE_USER")

    val isAdmin get() : Boolean = role == "ROLE_ADMIN"

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        if(isAdmin)
            return arrayListOf(
                SimpleGrantedAuthority("ROLE_USER"),
                SimpleGrantedAuthority("ROLE_ADMIN")
            )

        return arrayListOf(SimpleGrantedAuthority(role ))
    }

    override fun getPassword(): String {
        return passwordHash;
    }

    override fun getUsername(): String {
        return username;
    }

    override fun isAccountNonExpired(): Boolean {
        return true;
    }

    override fun isAccountNonLocked(): Boolean {
        return true;
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true;
    }

    override fun isEnabled(): Boolean {
        return true;
    }

//    @OneToOne(optional = true, fetch = FetchType.LAZY)
//    var rssKeyPair: RSSKeyPairEntity? = null;
}