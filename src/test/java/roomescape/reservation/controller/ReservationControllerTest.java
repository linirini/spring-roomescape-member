package roomescape.reservation.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.service.dto.SignUpRequest;
import roomescape.reservation.service.dto.ReservationRequest;
import roomescape.reservation.service.dto.ReservationTimeCreateRequest;
import roomescape.reservation.service.dto.ThemeRequest;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = {"classpath:truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ReservationControllerTest {
    @LocalServerPort
    private int port;

    private String date;
    private long timeId;
    private long themeId;
    private long memberId;

    @BeforeEach
    void init() {
        RestAssured.port = port;

        date = "2222-05-01";
        timeId = (int) RestAssured.given()
                .contentType(ContentType.JSON)
                .body(new ReservationTimeCreateRequest("17:46"))
                .when().post("/times")
                .then().extract().response().jsonPath().get("id");

        ThemeRequest themeRequest = new ThemeRequest("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        themeId = (int) RestAssured.given().contentType(ContentType.JSON).body(themeRequest)
                .when().post("/themes")
                .then().extract().response().jsonPath().get("id");

        SignUpRequest signUpRequest = new SignUpRequest("lini", "lini@email.com", "lini123");
        RestAssured.given().log().all().contentType(ContentType.JSON).body(signUpRequest)
                .when().post("/signup")
                .then().log().all();
        memberId = 1;
    }

    @DisplayName("예약 추가 성공 테스트")
    @Test
    void createReservation() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(201).body("id", is(greaterThan(0)));
    }

    @DisplayName("예약 추가 실패 테스트 - 중복 일정 오류")
    @Test
    void createDuplicatedReservation() {
        //given
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations");

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400)
                .body("message", is("선택하신 테마와 일정은 이미 예약이 존재합니다."));
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 오류")
    @Test
    void createInvalidScheduleReservation() {
        //given
        String invalidDate = "2023-10-04";

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest(invalidDate, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("현재보다 이전으로 일정을 설정할 수 없습니다."));
    }

    @DisplayName("예약 추가 실패 테스트 - 일정 날짜 오류")
    @Test
    void createInvalidScheduleDateReservation() {
        //given
        String invalidDate = "03-04";

        //when&then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new ReservationRequest(invalidDate, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().log().all()
                .assertThat().statusCode(400).body("message", is("올바르지 않은 날짜입니다."));
    }

    @DisplayName("모든 예약 내역 조회 테스트")
    @Test
    void findAllReservations() {
        //given
        RestAssured.given().contentType(ContentType.JSON)
                .body(new ReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations");

        //when & then
        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .assertThat().statusCode(200).body("size()", is(1));
    }

    @DisplayName("예약 취소 성공 테스트")
    @Test
    void deleteReservationSuccess() {
        //given
        var id = RestAssured.given().contentType(ContentType.JSON)
                .body(new ReservationRequest(date, memberId, timeId, themeId))
                .when().post("/reservations")
                .then().extract().body().jsonPath().get("id");

        //when
        RestAssured.given().log().all()
                .when().delete("/reservations/" + id)
                .then().log().all()
                .assertThat().statusCode(204);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .assertThat().body("size()", is(0));
    }
}