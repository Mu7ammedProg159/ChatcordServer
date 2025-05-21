package com.mdev.chatcord.server.authentication.configuration;

import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//@Component
public class AccountAccessAuthenticationProvider extends DaoAuthenticationProvider {


    public AccountAccessAuthenticationProvider(){
        //super();

        this.setPreAuthenticationChecks(new AccountPreAuthenticationChecks());
    }

    private static class AccountPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails toCheck) {}
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        super.additionalAuthenticationChecks(userDetails, authentication); // password checked here

        if (!userDetails.isEnabled()) {
            throw new DisabledException("CUSTOM: Failed to authenticate since user account is blocked.");
        }

        if (!userDetails.isAccountNonLocked()) {
            throw new LockedException("CUSTOM: Failed to authenticate since user account is locked.");
        }

        if (!userDetails.isAccountNonExpired())
            throw new AccountExpiredException("CUSTOM: Failed to authenticate since user account is expired");

        if (!userDetails.isCredentialsNonExpired())
            throw new CredentialsExpiredException("CUSTOM: Failed to authenticate since user account credentials are expired.");

    }
}
