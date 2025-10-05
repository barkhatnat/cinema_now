package ru.barkhatnat.cinema.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.barkhatnat.cinema.domain.OccupiedSeat;

import java.util.Optional;
import java.util.UUID;

public interface OccupiedSeatsRepository extends JpaRepository<OccupiedSeat, UUID>, JpaSpecificationExecutor<OccupiedSeat> {
    Optional<OccupiedSeat> findBySessionIdAndSeatId(UUID sessionId, UUID seatId);
}