package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Car {
    private CarType        carType;
    private Engine         engine;
    private BrakeSystem    brakeSystem;
    private SteeringSystem steeringSystem;

    public boolean isFullyConfigured() {
        return carType != null && engine != null
            && brakeSystem != null && steeringSystem != null;
    }

    public void reset() {
        carType        = null;
        engine         = null;
        brakeSystem    = null;
        steeringSystem = null;
    }
}
