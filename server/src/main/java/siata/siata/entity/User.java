package siata.siata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Data
@Entity
@Table(name = "user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User implements UserDetails {

    @Id
    @Column(name = "username", length = 50)
    @NotBlank(message = "Username tidak boleh kosong")
    @Size(min = 3, max = 50, message = "Username harus 3-50 karakter")
    private String username;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nip_pegawai", referencedColumnName = "nip", unique = true)
    private Pegawai pegawai;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Password tidak boleh kosong")
    private String password;

    @Column(name = "role", nullable = false, length = 50)
    @NotBlank(message = "Role tidak boleh kosong")
    @Pattern(regexp = "^(TIM_MANAJEMEN_ASET|PPK|PPBJ|DIREKTUR|DEV)$", 
             message = "Role harus salah satu dari: TIM_MANAJEMEN_ASET, PPK, PPBJ, DIREKTUR, DEV")
    private String role;

    // Field untuk mode simulasi (hanya untuk DEV)
    @Column(name = "simulation_role", length = 50)
    private String simulationRole;

    public String getEffectiveRole() {
        if (simulationRole != null && !simulationRole.isEmpty()) {
            return simulationRole;
        }
        return role;
    }

    // Implementasi UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}