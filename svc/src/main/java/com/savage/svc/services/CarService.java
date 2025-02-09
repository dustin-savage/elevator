package com.savage.svc.services;

import com.savage.svc.dto.*;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Builder
public class CarService {

   private static final Logger LOGGER = LoggerFactory.getLogger(CarService.class);

   private ElevatorService elevatorService;
   private RequestService requestService;

   @Scheduled(fixedDelay = 3000)
   public void start() {
      List<Car> cars = new ArrayList<>(elevatorService.getCars());
      // Shuffle so one car is not favored for most requests.
      Collections.shuffle(cars);
      for (Car car : cars) {
         if (car.isEnabled()) {
            if (car.getRequest() == null) {
               // Car is doing nothing. Find work.
               CarRequest req = this.requestService.getRequestCandidate(car);
               if (req != null) {
                  if (this.requestService.assignRequest(req.getId(), car)) {
                     this.moveCar(car, req);
                  }
               } else {
                  // Return to lobby
                  if (car.getCurrentFloor() != elevatorService.getLobbyFloor()) {
                     CarRequest lobbyRequest = CarRequest.builder()
                        .assignedCarId(car.getId())
                        .floor(elevatorService.getLobbyFloor())
                        .build();
                     lobbyRequest = this.requestService.addRequest(lobbyRequest);
                     this.moveCar(car, lobbyRequest);
                  }
               }
            } else {
               // Car is working on tasking. Nothing to do.
            }
         }
      }
   }

   private void moveCar(Car car, CarRequest request) {
      car.setDirection(car.getCurrentFloor() <= request.getFloor() ? Direction.UP : Direction.DOWN);
      car.setRequest(request);
   }

}
