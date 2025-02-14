package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import com.savage.svc.dto.Direction;
import com.savage.svc.services.api.CarScheduler;
import com.savage.svc.services.api.CarService;
import com.savage.svc.services.api.RequestService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The DefaultCarScheduler.serviceRequests() method runs on a fixed schedule. Each run it will:
 * 1. Shuffle the cars so no one car is favored.
 * 2. Iterate over each car, and get a request for it to fulfill.
 * 3. If a request is found for the car, assign the request to the car so that it knows to move.
 */
@Builder
public class DefaultCarScheduler implements CarScheduler {

   private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCarScheduler.class);

   private CarService carService;
   private RequestService requestService;

   @Scheduled(fixedDelay = 1000)
   @Override
   public void serviceRequests() {
      List<Car> cars = new ArrayList<>(carService.getCars());
      // Shuffle so one car is not favored for most requests.
      Collections.shuffle(cars);
      for (Car car : cars) {
         if (car.isEnabled()) {
            if (car.getRequest() == null) {
               // Car is doing nothing. Assign work.
               this.requestService.assignRequest(car);
            } else {
               // Car is working on tasking. Nothing to do.
            }
         }
      }
   }

}
