package com.epam.gym.security;

import com.epam.gym.repository.TraineeRepository;
import com.epam.gym.repository.TrainerRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final TraineeRepository traineeRepository;
  private final TrainerRepository trainerRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Try to find as Trainee first
    return traineeRepository
        .findByUsername(username)
        .map(this::createUserDetails)
        .orElseGet(
            () ->
                trainerRepository
                    .findByUsername(username)
                    .map(this::createUserDetails)
                    .orElseThrow(
                        () -> new UsernameNotFoundException("User not found: " + username)));
  }

  private UserDetails createUserDetails(com.epam.gym.model.User user) {
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(!user.getIsActive())
        .build();
  }
}
