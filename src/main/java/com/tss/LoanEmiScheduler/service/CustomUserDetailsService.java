package com.tss.LoanEmiScheduler.service;

import com.tss.LoanEmiScheduler.entity.User;
import com.tss.LoanEmiScheduler.entity.UserPrincipal;
import com.tss.LoanEmiScheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import static com.tss.LoanEmiScheduler.constant.GlobalConstant.SECURITY;

@RequiredArgsConstructor
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("{} Load: User by username {}", SECURITY, username);
        User user = userRepository.findByIdentifier(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        log.info("{} Loaded: User with id {}", SECURITY, user.getId());
        return new UserPrincipal(user);
    }
}
