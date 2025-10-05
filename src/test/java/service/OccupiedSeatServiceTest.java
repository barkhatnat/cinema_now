package service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.barkhatnat.cinema.domain.OccupiedSeat;
import ru.barkhatnat.cinema.domain.Seat;
import ru.barkhatnat.cinema.domain.Session;
import ru.barkhatnat.cinema.repository.OccupiedSeatsRepository;
import ru.barkhatnat.cinema.repository.SeatRepository;
import ru.barkhatnat.cinema.repository.SessionRepository;
import ru.barkhatnat.cinema.service.OccupiedSeatsService;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class OccupiedSeatServiceTest {
    private OccupiedSeatsRepository occupiedSeatsRepository;
    private OccupiedSeatsService service;
    private SeatRepository seatRepository;
    private SessionRepository sessionRepository;

    private UUID sessionId;
    private UUID seatId;

    @BeforeEach
    void setUp() {
        occupiedSeatsRepository = mock(OccupiedSeatsRepository.class);
        seatRepository = mock(SeatRepository.class);
        sessionRepository = mock(SessionRepository.class);
        service = new OccupiedSeatsService(occupiedSeatsRepository, seatRepository, sessionRepository);

        sessionId = UUID.randomUUID();
        seatId = UUID.randomUUID();
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false"
    })
    void checkIsSeatOccupied(Boolean isOccupied, boolean expected) {
        if (isOccupied != null) {
            OccupiedSeat occupiedSeat = new OccupiedSeat();
            occupiedSeat.setOccupied(isOccupied);
            when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                    .thenReturn(Optional.of(occupiedSeat));
        } else {
            when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                    .thenReturn(Optional.empty());
        }

        boolean actual = service.isSeatOccupied(sessionId, seatId);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void checkIsSeatOccupied_shouldHandleNullIdsSafely() {
        when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                .thenReturn(Optional.empty());

        boolean result = service.isSeatOccupied(null, null);

        assertThat(result).isFalse();
    }

    @Test
    void markSeatAsFree_shouldThrowException_whenSeatOrSessionNotFound() {
        when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                .thenReturn(Optional.empty());
        when(seatRepository.findById(seatId)).thenReturn(Optional.empty());
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> service.markSeatAsFree(sessionId, seatId));
    }


    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false"
    })
    void markSeatAsOccupied_paramTest(boolean existingSeat, boolean initialOccupied) {
        if (existingSeat) {
            stubFindExistedSeat(initialOccupied);
        } else {
            stubCreateNewOccupiedSeat();
        }

        OccupiedSeat returned = service.markSeatAsOccupied(sessionId, seatId);

        assertThat(returned.isOccupied()).isTrue();
        if (!existingSeat) {
            assertThat(returned.getSeat()).isNotNull();
            assertThat(returned.getSession()).isNotNull();
        }
        verify(occupiedSeatsRepository, times(1)).save(any(OccupiedSeat.class));
    }

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false, false"
    })
    void markSeatAsFree_paramTest(boolean existingSeat, boolean initialOccupied) {
        if (existingSeat) {
            stubFindExistedSeat(initialOccupied);
        } else {
            stubCreateNewOccupiedSeat();
        }

        OccupiedSeat returned = service.markSeatAsFree(sessionId, seatId);

        assertThat(returned.isOccupied()).isFalse();
        if (!existingSeat) {
            assertThat(returned.getSeat()).isNotNull();
            assertThat(returned.getSession()).isNotNull();
        }
        verify(occupiedSeatsRepository, times(1)).save(any(OccupiedSeat.class));
    }

    private void stubCreateNewOccupiedSeat() {
        when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                .thenReturn(Optional.empty());
        Seat seat = new Seat();
        Session session = new Session();
        when(seatRepository.findById(seatId)).thenReturn(Optional.of(seat));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(occupiedSeatsRepository.save(any(OccupiedSeat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }


    private void stubFindExistedSeat(boolean isOccupied) {
        OccupiedSeat occupiedSeat = new OccupiedSeat();
        occupiedSeat.setOccupied(isOccupied);
        occupiedSeat.setId(UUID.randomUUID());
        when(occupiedSeatsRepository.findBySessionIdAndSeatId(sessionId, seatId))
                .thenReturn(Optional.of(occupiedSeat));
        when(occupiedSeatsRepository.save(any(OccupiedSeat.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
