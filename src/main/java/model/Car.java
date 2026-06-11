package model;

public class Car {
    private CarType        carType;
    private Engine         engine;
    private BrakeSystem    brakeSystem;
    private SteeringSystem steeringSystem;

    public CarType        getCarType()        { return carType; }
    public Engine         getEngine()         { return engine; }
    public BrakeSystem    getBrakeSystem()    { return brakeSystem; }
    public SteeringSystem getSteeringSystem() { return steeringSystem; }

    public void setCarType(CarType carType)             { this.carType = carType; }
    public void setEngine(Engine engine)                { this.engine = engine; }
    public void setBrakeSystem(BrakeSystem brakeSystem) { this.brakeSystem = brakeSystem; }
    public void setSteeringSystem(SteeringSystem s)     { this.steeringSystem = s; }

    public boolean isFullyConfigured() {
        return carType != null && engine != null
            && brakeSystem != null && steeringSystem != null;
    }

    public void reset() {
        carType = null;
        engine = null;
        brakeSystem = null;
        steeringSystem = null;
    }
}
