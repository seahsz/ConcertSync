package vttp.server.services;

import java.time.LocalDate;
import java.util.List;

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
    
}
