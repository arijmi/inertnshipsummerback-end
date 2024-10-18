package com.meetsoccer.meet_soccer.security.services;

import com.meetsoccer.meet_soccer.model.ERole;
import com.meetsoccer.meet_soccer.model.User;
import com.meetsoccer.meet_soccer.repository.PlayerRepository;
import com.meetsoccer.meet_soccer.repository.RefereeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    RefereeRepository refereeRepository;

    @Autowired
    PlayerRepository playerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Check if the user is a player
        User user = playerRepository.findByEmail(email);
        ERole role = ERole.ROLE_PLAYER;
        if (user == null) {
            // Check if the user is a referee else throw an exception
            user = refereeRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException("Referee not found with email: " + email));
            role = ERole.ROLE_REFEREE;
            if (user == null) {
                throw new UsernameNotFoundException("User Not Found with email: " + email);
            }
        }

        return UserDetailsImpl.build(user,role);
    }
}
