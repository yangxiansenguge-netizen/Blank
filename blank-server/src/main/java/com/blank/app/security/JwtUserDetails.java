package com.blank.app.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JwtUserDetails implements UserDetails {
    private Integer id;
    private String uid;
    private String username;
    private String email;
    private String identity;

    public JwtUserDetails(Integer id, String uid, String username, String email, String identity) {
        this.id = id;
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.identity = identity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if ("admin".equals(identity)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Integer getId() { return id; }
    public String getUid() { return uid; }
    public String getEmail() { return email; }
    public String getIdentity() { return identity; }
}
