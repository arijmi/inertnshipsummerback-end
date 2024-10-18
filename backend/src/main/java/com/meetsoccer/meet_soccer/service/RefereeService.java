package com.meetsoccer.meet_soccer.service;

import com.meetsoccer.meet_soccer.model.Referee;

public interface RefereeService {
    Referee findById(Long id);
    Referee save(Referee referee);
    Referee update(Referee referee);
    void delete(Long id);

    Referee findByEmail(String email);
}
