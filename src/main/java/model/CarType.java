package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CarType implements Displayable {
    SEDAN("Sedan"),
    SUV("SUV"),
    TRUCK("Truck");

    private final String displayName;
}
