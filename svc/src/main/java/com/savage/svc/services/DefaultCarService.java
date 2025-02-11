package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.services.api.CarService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * Manages access to elevator cars.
 */
@Builder
public class DefaultCarService implements CarService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCarService.class);

    private final List<Car> cars;
    @Builder.Default
    private final StampedLock lock = new StampedLock();

    @Override
    public List<Car> getCars() {
        long stamp = lock.readLock();
        try {
            return Collections.unmodifiableList(cars);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Car getCarById(int id) {
        long stamp = lock.readLock();
        try {
            return cars.stream()
               .filter(c -> c.getId() == id)
               .findFirst().orElse(null);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Car save(Car car) {
        long stamp = lock.writeLock();
        try {
            Car update = cars.stream()
               .filter(c -> c.getId() == car.getId())
               .findFirst().orElse(null);
            if (update != null) {
                int i = cars.indexOf(update);
                cars.remove(update);
                cars.add(i, car);
                return car;
            }
        } finally {
            lock.unlockWrite(stamp);
        }
        return null;
    }

}
