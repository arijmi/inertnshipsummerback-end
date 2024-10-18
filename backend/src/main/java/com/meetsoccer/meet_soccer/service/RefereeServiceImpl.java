package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Referee;
import com.meetsoccer.meet_soccer.repository.RefereeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefereeServiceImpl implements RefereeService {

    @Autowired
    private RefereeRepository refereeRepository;

    @Override
    public Referee findById(Long id) {
        return refereeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Referee not found"));
    }

    @Override
    @Transactional
    public Referee save(Referee referee) {
        return refereeRepository.save(referee);
    }

    @Override
    @Transactional
    public Referee update(Referee referee) {
        if (refereeRepository.existsById(referee.getId())) {
            return refereeRepository.save(referee);
        } else {
            throw new RuntimeException("Referee not found");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (refereeRepository.existsById(id)) {
            refereeRepository.deleteById(id);
        } else {
            throw new RuntimeException("Referee not found");
        }
    }

    @Override
    public Referee findByEmail(String email) {
        return refereeRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Referee not found with email: " + email));
    }
}

