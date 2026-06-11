package rule;

import model.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CompatibilityChecker {

    private static final List<CompatibilityRule> RULES = List.of(
        car -> car.getCarType() == CarType.SEDAN && car.getBrakeSystem() == BrakeSystem.CONTINENTAL
            ? Optional.of("Sedan에는 Continental제동장치 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.SUV && car.getEngine() == Engine.TOYOTA
            ? Optional.of("SUV에는 TOYOTA엔진 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.TRUCK && car.getEngine() == Engine.WIA
            ? Optional.of("Truck에는 WIA엔진 사용 불가") : Optional.empty(),

        car -> car.getCarType() == CarType.TRUCK && car.getBrakeSystem() == BrakeSystem.MANDO
            ? Optional.of("Truck에는 Mando제동장치 사용 불가") : Optional.empty(),

        car -> car.getBrakeSystem() == BrakeSystem.BOSCH && car.getSteeringSystem() != SteeringSystem.BOSCH
            ? Optional.of("Bosch제동장치에는 Bosch조향장치 이외 사용 불가") : Optional.empty()
    );

    public List<String> validate(Car car) {
        return RULES.stream()
            .map(rule -> rule.check(car))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    public boolean isValid(Car car) {
        return RULES.stream().allMatch(rule -> rule.check(car).isEmpty());
    }
}
