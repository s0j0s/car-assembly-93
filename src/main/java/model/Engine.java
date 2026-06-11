package model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Engine implements Displayable {
    GM("GM"),
    TOYOTA("TOYOTA"),
    WIA("WIA"),
    BROKEN("고장난 엔진");

    private final String displayName;

    public boolean isBroken() {
        return this == BROKEN;
    }
}
