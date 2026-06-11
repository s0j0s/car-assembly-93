package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BrakeSystem implements Displayable {
    MANDO("Mando"),
    CONTINENTAL("Continental"),
    BOSCH("Bosch");

    private final String displayName;
}
