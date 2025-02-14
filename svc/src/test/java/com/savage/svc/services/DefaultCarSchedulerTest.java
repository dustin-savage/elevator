package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import com.savage.svc.services.api.CarService;
import com.savage.svc.services.api.RequestService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class DefaultCarSchedulerTest {

   /**
    * Parameterized test for the DefaultCarScheduler.serviceRequests() method.
    *
    * @param cars                      - cars returned by mocked elevator service.
    * @param expectedCallsToAssign     - number of expected calls to assign a request to a car.
    */
   @ParameterizedTest
   @MethodSource("requestData")
   void serviceRequestsTest(List<Car> cars, Integer expectedCallsToAssign) {
      List<Car> savedCars = new ArrayList<>();

      CarService carService = Mockito.mock(CarService.class);
      Mockito.when(carService.getCars())
         .thenReturn(cars);

      RequestService requestService = Mockito.mock(RequestService.class);
      Mockito.doAnswer(invocationOnMock -> {
         Car car = invocationOnMock.getArgument(0);
         return car.withRequest(CarRequest.builder()
            .id("123")
            .build());
      }).when(requestService).assignRequest(ArgumentMatchers.any());
      Mockito.doAnswer(invocationOnMock -> {
         Car car = invocationOnMock.getArgument(0);
         savedCars.add(car);
         return car;
      }).when(carService).save(ArgumentMatchers.any());

      // GIVEN - a DefaultCarScheduler
      DefaultCarScheduler defaultCarScheduler = DefaultCarScheduler.builder()
         .carService(carService)
         .requestService(requestService)
         .build();

      // WHEN - serviceRequests() is called
      defaultCarScheduler.serviceRequests();

      // THEN - requests are assigned to the car as expected
      Mockito.verify(carService, Mockito.times(1)).getCars();
      Mockito.verify(requestService, Mockito.times(expectedCallsToAssign))
         .assignRequest(ArgumentMatchers.any());

      if (!savedCars.isEmpty()) {
         Mockito.verify(carService, Mockito.times(expectedCallsToAssign)).save(ArgumentMatchers.any());
         for (Car car : savedCars) {
            Assertions.assertNotNull(car.getRequest());
         }
      }
   }

   public static Stream<Object> requestData() {
      // The following stream is passed as parameters to the above test.
      return Stream.of(
         // 1. No cars. Expect assignRequest not called.
         new Object[]{
            new ArrayList<>(), // cars
            0 // expectedCallsToAssign
         },
         // 2. Cars are disabled. Expect assignRequest not called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(false)
                  .build()
            ), // cars
            0 // expectedCallsToAssign
         },
         // 3. Cars are enabled. Expect assignRequest is called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(true)
                  .build(),
               Car.builder()
                  .id(1)
                  .enabled(true)
                  .build()
            ), // cars
            2, // expectedCallsToAssign
         },
         // 4. Car has a request. Expect assignRequest is not called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(true)
                  .request(CarRequest.builder().build())
                  .build()
            ),
            0 // expectedCallsToAssign
         },
         // 5. Request candidate is returned. Expect assignRequest is called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(true)
                  .build()
            ),
            1 // expectedCallsToAssign
         },
         // 6. 3 cars, but one disabled. Expect assignRequest is called twice.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(true)
                  .build(),
               Car.builder()
                  .id(1)
                  .enabled(false)
                  .build(),
               Car.builder()
                  .id(2)
                  .enabled(true)
                  .build()
            ), // cars
            2, // expectedCallsToAssign
         }
      );
   }

}