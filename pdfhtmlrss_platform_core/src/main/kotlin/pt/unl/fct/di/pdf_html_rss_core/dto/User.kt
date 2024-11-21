package pt.unl.fct.di.pdf_html_rss_core.dto

import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.KeyPair
import javax.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @Column(name = "userid")
    var userId : Long,

    @Column(name = "username")
    //TODO solve clash
    private var username : String,

    @Column(name = "passwordhash")
    var passwordHash : String,

    @Transient
    var passwordClear : String?,

) : UserDetails {
    constructor() : this(-1, "", "", "")

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        //TODO roles/authorities
        return arrayListOf(GrantedAuthority { "USER" })
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