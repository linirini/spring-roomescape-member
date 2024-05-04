package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ReservationTimeRepository;
import roomescape.dao.ThemeRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.InvalidReservationException;
import roomescape.service.dto.AllReservationTimeResponse;
import roomescape.service.dto.AvailableReservationTimeResponse;
import roomescape.service.dto.ReservationTimeCreateRequest;
import roomescape.service.dto.ReservationTimeReadRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@Sql(scripts = {"classpath:truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationTimeServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("새로운 예약 시간을 저장한다.")
    @Test
    void create() {
        //given
        String startAt = "10:00";
        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(startAt);

        //when
        AllReservationTimeResponse result = reservationTimeService.create(reservationTimeCreateRequest);

        //then
        assertAll(
                () -> assertThat(result.id()).isNotZero(),
                () -> assertThat(result.startAt()).isEqualTo(startAt)
        );
    }

    @DisplayName("모든 예약 시간 내역을 조회한다.")
    @Test
    void findAll() {
        //given
        reservationTimeRepository.save(new ReservationTime("10:00"));

        //when
        List<AllReservationTimeResponse> reservationTimes = reservationTimeService.findAll();

        //then
        assertThat(reservationTimes).hasSize(1);
    }

    @DisplayName("시간이 이미 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedTime() {
        //given
        ReservationTime target = reservationTimeRepository.save(new ReservationTime("10:00"));

        ReservationTimeCreateRequest reservationTimeCreateRequest = new ReservationTimeCreateRequest(target.getStartAt());

        //when&then
        assertThatThrownBy(() -> reservationTimeService.create(reservationTimeCreateRequest))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이미 같은 시간이 존재합니다.");
    }

    @DisplayName("예약이 존재하는 시간으로 삭제를 시도하면 예외를 발생시킨다.")
    @Test
    void cannotDeleteTime() {
        //given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime("10:00"));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        reservationRepository.save(new Reservation("lily", new ReservationDate("2222-10-04"), reservationTime, theme));

        //when&then
        assertThatThrownBy(() -> reservationTimeService.deleteById(reservationTime.getId()))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("해당 시간에 예약이 존재해서 삭제할 수 없습니다.");
    }

    @DisplayName("해당 테마와 날짜에 예약이 가능한 시간 목록을 조회한다.")
    @Test
    void findAvailableTimes() {
        //given
        ReservationTime bookedReservationTime = reservationTimeRepository.save(new ReservationTime("10:00"));
        ReservationTime notBookedReservationTime = reservationTimeRepository.save(new ReservationTime("11:00"));
        Theme theme = themeRepository.save(new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
        Reservation reservation = reservationRepository.save(new Reservation("lily", new ReservationDate("2222-10-04"), bookedReservationTime, theme));

        //when
        List<AvailableReservationTimeResponse> result = reservationTimeService.findAvailableTimes(new ReservationTimeReadRequest(reservation.getDate(), theme.getId()));

        //then
        boolean isBookedOfBookedTime = result.stream().filter(time -> time.id() == bookedReservationTime.getId()).findFirst().get().isBooked();
        boolean isBookedOfUnBookedTime = result.stream().filter(time -> time.id() == notBookedReservationTime.getId()).findFirst().get().isBooked();
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(isBookedOfUnBookedTime).isFalse(),
                () -> assertThat(isBookedOfBookedTime).isTrue()
        );
    }
}
