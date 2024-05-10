package roomescape.reservation.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(
        long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm") LocalTime startAt) {
    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}