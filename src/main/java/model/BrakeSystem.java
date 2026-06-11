package model;

public enum BrakeSystem {
    MANDO("Mando"),
    CONTINENTAL("Continental"),
    BOSCH("Bosch");

    public final String displayName;

    BrakeSystem(String displayName) {
        this.displayName = displayName;
    }
}
