package pt.unl.fct.di.pdf_html_rss_core.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.security.KeyPair
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