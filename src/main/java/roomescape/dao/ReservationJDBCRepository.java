package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.util.List;
import java.util.Map;

@Repository
public class ReservationJDBCRepository implements ReservationRepository {
    private static final String TABLE_NAME = "reservation";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
        ReservationTime reservationTime = new ReservationTime(resultSet.getLong("time_id"),
                resultSet.getString("start_at"));
        Theme theme = new Theme(resultSet.getLong("theme_id"), resultSet.getString("theme_name"),
                resultSet.getString("description"), resultSet.getString("thumbnail"));
        Reservation reservation = new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getString("reservation_name"),
                resultSet.getString("date"),
                reservationTime,
                theme);
        return reservation;
    };

    public ReservationJDBCRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(TABLE_NAME)
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT r.id as reservation_id, r.name as reservation_name, r.date, "
                + "rt.id as time_id, rt.start_at, "
                + "t.id as theme_id, t.name as theme_name, t.description, t.thumbnail FROM reservation as r "
                + "inner join reservation_time as rt on r.time_id = rt.id "
                + "inner join theme as t on r.theme_id = t.id";
        List<Reservation> reservations = jdbcTemplate.query(sql, rowMapper);
        return reservations;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Map<String, ?> params = Map.of(
                "name", reservation.getName(),
                "date", reservation.getDate(),
                "time_id", reservation.getReservationTime().getId(),
                "theme_id", reservation.getTheme().getId());
        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, reservation);
    }

    @Override
    public void deleteById(final long id) {
        String sql = "DELETE FROM Reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(String date, long timeId, long themeId) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE date = ? AND time_id = ? AND theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, date, timeId, themeId) > 0;
    }

    @Override
    public boolean existsByTimeId(long id) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE time_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    @Override
    public boolean existsByThemeId(long id) {
        String sql = "SELECT COUNT(*) FROM Reservation WHERE theme_id = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }
}
