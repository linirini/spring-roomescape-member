package roomescape.domain;

import roomescape.exception.InvalidReservationException;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;

public class ReservationTime {
    private final static long NO_ID = 0;

    private final long id;
    private final LocalTime startAt;

    public ReservationTime(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(long id, ReservationTime reservationTime) {
        this(id, reservationTime.startAt);
    }

    public ReservationTime(long id, String time) {
        validate(time);
        this.id = id;
        this.startAt = LocalTime.parse(time);
    }

    public ReservationTime(String time) {
        validate(time);
        this.id = NO_ID;
        this.startAt = LocalTime.parse(time);
    }

    private void validate(String time) {
        try {
            LocalTime.parse(time);
        } catch (DateTimeParseException e) {
            throw new InvalidReservationException("올바르지 않은 시간입니다.");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ReservationTime that = (ReservationTime) o;
        return id == that.id && Objects.equals(startAt, that.startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}
