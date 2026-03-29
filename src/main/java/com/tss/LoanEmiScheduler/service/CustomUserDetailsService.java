package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.User;
import com.tss.LoanEmiScheduler.entity.UserPrincipal;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByIdentifier(username)
                .orElseThrow(() -> new UsernameNotFoundException(username + "not found."));
        return new UserPrincipal(user);
    }
}
