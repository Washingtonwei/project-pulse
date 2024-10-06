package team.projectpulse.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

public class MyUserPrincipal implements UserDetails {

    private final PeerEvaluationUser peerEvaluationUser;


    public MyUserPrincipal(PeerEvaluationUser peerEvaluationUser) {
        this.peerEvaluationUser = peerEvaluationUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert a user's roles from space-delimited string to a list of SimpleGrantedAuthority objects.
        // E.g., john's roles are stored in a string like "admin user moderator", we need to convert it to a list of GrantedAuthority.
        // Before conversion, we need to add this "ROLE_" prefix to each role rubricName.
        return Arrays.stream(StringUtils.tokenizeToStringArray(this.peerEvaluationUser.getRoles(), " "))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public String getPassword() {
        return this.peerEvaluationUser.getPassword();
    }

    @Override
    public String getUsername() {
        return this.peerEvaluationUser.getUsername();
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
        return this.peerEvaluationUser.isEnabled();
    }

    public PeerEvaluationUser getPeerEvaluationUser() {
        return peerEvaluationUser;
    }

}
