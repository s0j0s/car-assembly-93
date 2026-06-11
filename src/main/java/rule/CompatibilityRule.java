package rule;

import model.Car;
import java.util.Optional;

@FunctionalInterface
public interface CompatibilityRule {
    Optional<String> check(Car car);
}
