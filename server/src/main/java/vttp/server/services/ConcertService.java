package vttp.server.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.server.models.angularDto.Concert;
import vttp.server.repositories.ConcertRepository;

@Service
public class ConcertService {

    @Autowired
    private ConcertRepository concertRepo;

    public List<Concert> getUpcomingConcerts(LocalDate today) {
        return concertRepo.getAllUpcomingConcerts(today);
    }

    public Optional<Concert> getConcertById(Long id) {
        return concertRepo.getConcertById(id);
    }
    
    public boolean validateConcertDate(Long concertId, LocalDate date) {
        return concertRepo.validateConcertDate(concertId, date);
    }
    
}
