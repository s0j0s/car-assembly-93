package model;

public enum SteeringSystem {
    BOSCH("Bosch"),
    MOBIS("Mobis");

    public final String displayName;

    SteeringSystem(String displayName) {
        this.displayName = displayName;
    }
}
