package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SteeringSystem implements Displayable {
    BOSCH("Bosch"),
    MOBIS("Mobis");

    private final String displayName;
}
