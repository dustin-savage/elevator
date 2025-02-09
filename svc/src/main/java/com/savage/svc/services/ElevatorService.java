package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.Floor;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Builder
public class ElevatorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElevatorService.class);

    private final List<Floor> floors;
    private final List<Car> cars;
    private final int lobbyIndex;

    public List<Car> getCars() {
        return cars;
    }

    public Car getCarById(int id) {
        return cars.stream()
           .filter(c -> c.getId() == id)
           .findFirst().orElse(null);
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public Floor getFloorById(int id) {
        return floors.stream()
           .filter(f -> f.getId() == id)
           .findFirst().orElse(null);
    }

   public int getLobbyFloor() {
        return this.getFloors().get(this.lobbyIndex).getId();
   }

}
