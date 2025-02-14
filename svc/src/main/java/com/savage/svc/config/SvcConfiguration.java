package com.savage.svc.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.savage.svc.dto.Car;
import com.savage.svc.dto.Direction;
import com.savage.svc.dto.Floor;
import com.savage.svc.services.DefaultCarScheduler;
import com.savage.svc.services.DefaultCarService;
import com.savage.svc.services.DefaultFloorService;
import com.savage.svc.services.DefaultRequestService;
import com.savage.svc.services.api.CarScheduler;
import com.savage.svc.services.api.CarService;
import com.savage.svc.services.api.FloorService;
import com.savage.svc.services.api.RequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableScheduling
public class SvcConfiguration {

   @Bean
   public ObjectMapper objectMapper() {
      ObjectMapper objectMapper = new ObjectMapper()
         .registerModule(new ParameterNamesModule())
         .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return objectMapper;
   }

   /**
    * Define the list of floors in the building.
    * Note although this is configurable, the UI is not responsive enough yet to change from the default (4 floor) configuration.
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
    * Note although this is configurable, the UI is not responsive enough yet to change from the default (2 car) configuration.
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
    * Define the floor service responsible for managing access to floors.
    */
   @Bean
   public FloorService floorService(@Value("${lobbyFloorIndex:0}") int lobbyIndex,
                                    List<Floor> floors) {
      if (floors == null || floors.size() < 2) {
         throw new IllegalArgumentException("Must have at least 2 floors.");
      }
      return DefaultFloorService.builder()
         .floors(floors)
         .lobbyIndex(lobbyIndex)
         .build();
   }

   /**
    * Define the car service responsible for managing access to cars.
    */
   @Bean
   public CarService carService(List<Car> cars) {
      if (cars == null || cars.isEmpty()) {
         throw new IllegalArgumentException("Must have at least 1 car.");
      }
      return DefaultCarService.builder()
         .cars(cars)
         .build();
   }

   /**
    * Define the request service that manages internal and external requests.
    * Internal request are from inside the elevator car (internal button panel).
    * External requests are from outside the elevator car (on the wall panel).
    */
   @Bean
   public RequestService requestService(@Value("${floorCount:4}") int floorCount,
                                        CarService carService) {
      return DefaultRequestService.builder()
         .requests(new ArrayList<>())
         .minFloor(0)
         .maxFloor(floorCount - 1)
         .carService(carService)
         .build();
   }

   /**
    * Define scheduler to route pending requests to elevator cars.
    */
   @Bean
   public CarScheduler carScheduler(CarService carService,
                                    RequestService requestService) {
      DefaultCarScheduler defaultCarScheduler = DefaultCarScheduler.builder()
         .carService(carService)
         .requestService(requestService)
         .build();
      return defaultCarScheduler;
   }

}
