package roomescape.domain;

import java.util.Objects;
import roomescape.exception.InvalidReservationException;

public class MemberName {

    private static final int MINIMUM_NAME_LENGTH = 1;
    private static final int MAXIMUM_NAME_LENGTH = 5;
    private static final String INVALID_NAME_LENGTH = String.format("이름은 %d자 이상, %d자 이하여야 합니다.", MINIMUM_NAME_LENGTH,
            MAXIMUM_NAME_LENGTH);

    private final String value;

    public MemberName(final String value) {
        validateName(value);
        this.value = value;
    }

    private void validateName(final String name) {
        if (name.length() < MINIMUM_NAME_LENGTH || name.length() > MAXIMUM_NAME_LENGTH) {
            throw new InvalidReservationException(INVALID_NAME_LENGTH);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MemberName memberName = (MemberName) o;
        return Objects.equals(value, memberName.value);
    }

    public String getValue() {
        return value;
    }
}
