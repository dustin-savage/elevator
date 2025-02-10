package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.services.api.CarService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Manages access to elevator cars.
 */
@Builder
public class DefaultCarService implements CarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCarService.class);

    private final List<Car> cars;

    @Override
    public List<Car> getCars() {
        return cars;
    }

    @Override
    public Car getCarById(int id) {
        return cars.stream()
           .filter(c -> c.getId() == id)
           .findFirst().orElse(null);
    }

}
