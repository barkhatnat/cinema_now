package ru.barkhatnat.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.barkhatnat.cinema.domain.OccupiedSeats;
import ru.barkhatnat.cinema.domain.Seat;
import ru.barkhatnat.cinema.domain.Session;
import ru.barkhatnat.cinema.repository.OccupiedSeatsRepository;
import ru.barkhatnat.cinema.repository.SeatRepository;
import ru.barkhatnat.cinema.repository.SessionRepository;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OccupiedSeatsService {

    private final OccupiedSeatsRepository occupiedSeatsRepository;
    private final SeatRepository seatRepository;
    private final SessionRepository sessionRepository;

    public boolean isSeatOccupied(UUID sessionId, UUID seatId) {
        return occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId)
                .map(OccupiedSeats::isOccupied)
                .orElse(false);
    }

    public OccupiedSeats markSeatAsOccupied(UUID sessionId, UUID seatId) {
        return occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId)
                .map(occupiedSeat -> {
                    occupiedSeat.setOccupied(true);
                    return occupiedSeatsRepository.save(occupiedSeat);
                })
                .orElseGet(() -> {
                    OccupiedSeats newOccupiedSeat = new OccupiedSeats();
                    Optional<Seat> seat = seatRepository.findById(seatId);
                    Optional<Session> session = sessionRepository.findById(sessionId);
                    if (seat.isPresent() && session.isPresent()) {
                        newOccupiedSeat.setSeat(seat.get());
                        newOccupiedSeat.setSession(session.get());
                    } else {
                        new IllegalArgumentException("Seat or Session not found");
                    }
                    newOccupiedSeat.setOccupied(true);
                    return occupiedSeatsRepository.save(newOccupiedSeat);
                });
    }

    public OccupiedSeats markSeatAsFree(UUID sessionId, UUID seatId) {
        return occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId)
                .map(occupiedSeat -> {
                    occupiedSeat.setOccupied(false);
                    return occupiedSeatsRepository.save(occupiedSeat);
                })
                .orElseGet(() -> {
                    OccupiedSeats newOccupiedSeat = new OccupiedSeats();
                    Optional<Seat> seat = seatRepository.findById(seatId);
                    Optional<Session> session = sessionRepository.findById(sessionId);
                    if (seat.isPresent() && session.isPresent()) {
                        newOccupiedSeat.setSeat(seat.get());
                        newOccupiedSeat.setSession(session.get());
                    } else {
                        new IllegalArgumentException("Seat or Session not found");
                    }
                    newOccupiedSeat.setOccupied(false);
                    return occupiedSeatsRepository.save(newOccupiedSeat);
                });
    }
}