package com.savage.svc.config;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.Direction;
import com.savage.svc.dto.Floor;
import com.savage.svc.services.CarService;
import com.savage.svc.services.ElevatorService;
import com.savage.svc.services.RequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
public class SvcConfiguration {

   /**
    * Define the list of floors in the building.
    * Note although this is configurable, the UI is not responsive enough yet to change from the default.
    */
   @Bean
   public List<Floor> floors(@Value("${floorCount:4}") int floorCount,
                             @Value("${lobbyFloorIndex:0}") int lobbyIndex) {
      if (lobbyIndex < 0 || lobbyIndex > floorCount - 1) {
         throw new IllegalArgumentException("Lobby index is out of range.");
      }
      List<Floor> floors = new ArrayList<>();
      for (int i = 0; i < floorCount; i++) {
         floors.add(Floor.builder()
            .id(i)
            .name(i == lobbyIndex ? "L" : Integer.toString(i))
            .build());
      }
      return floors;
   }

   /**
    * Define the elevator cars in the building.
    * Note although this is configurable, the UI is not responsive enough yet to change from the default.
    */
   @Bean
   public List<Car> cars(@Value("${carCount:2}") int carCount) {
      List<Car> cars = new ArrayList<>();
      for (int i = 0; i < carCount; i++) {
         cars.add(Car.builder()
            .id(i)
            .enabled(true)
            .direction(Direction.UP)
            .build());
      }
      return cars;
   }

   /**
    * Define the elevator service.
    */
   @Bean
   public ElevatorService elevatorService(@Value("${lobbyFloorIndex:0}") int lobbyIndex,
                                          List<Car> cars,
                                          List<Floor> floors) {
      if (cars == null || cars.isEmpty()) {
         throw new IllegalArgumentException("Must have at least 1 car.");
      }
      if (floors == null || floors.size() < 2) {
         throw new IllegalArgumentException("Must have at least 2 floors.");
      }
      return ElevatorService.builder()
         .floors(floors)
         .cars(cars)
         .lobbyIndex(lobbyIndex)
         .build();
   }

   /**
    * Define the service that manages internal and external requests.
    * Internal: from inside the car.
    * External: requests from outside the elevator car.
    */
   @Bean
   public RequestService requestService(@Value("${floorCount:4}") int floorCount) {
      return RequestService.builder()
         .requests(new ArrayList<>())
         .minFloor(0)
         .maxFloor(floorCount - 1)
         .build();
   }

   @Bean
   public CarService carService(ElevatorService elevatorService,
                                RequestService requestService) {
      CarService carService = CarService.builder()
         .elevatorService(elevatorService)
         .requestService(requestService)
         .build();
      return carService;
   }

}
