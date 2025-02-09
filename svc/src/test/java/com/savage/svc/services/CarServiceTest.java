package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class CarServiceTest {

   /**
    * Parameterized test for the CarService.serviceRequests() method.
    *
    * @param cars                      - cars returned by mocked elevator service.
    * @param expectedCallsForCandidate - number of expected calls to get a request candidate.
    * @param candidate                 - the candidate returned by the mocked request service.
    * @param expectedCallsToAssign     - number of expected calls to assign a request to a car.
    */
   @ParameterizedTest
   @MethodSource("requestData")
   void serviceRequestsTest(List<Car> cars, Integer expectedCallsForCandidate, CarRequest candidate, Integer expectedCallsToAssign) {
      ElevatorService elevatorService = Mockito.mock(ElevatorService.class);
      Mockito.when(elevatorService.getCars())
         .thenReturn(cars);

      RequestService requestService = Mockito.mock(RequestService.class);
      Mockito.when(requestService.getRequestCandidate(ArgumentMatchers.any())).thenReturn(candidate);
      Mockito.when(requestService.assignRequest(ArgumentMatchers.anyString(), ArgumentMatchers.any()))
         .thenReturn(true);

      // GIVEN - a CarService
      CarService carService = CarService.builder()
         .elevatorService(elevatorService)
         .requestService(requestService)
         .build();

      // WHEN - serviceRequests() is called
      carService.serviceRequests();

      // THEN - requests are processed for the car as expected
      Mockito.verify(elevatorService, Mockito.times(1)).getCars();
      Mockito.verify(requestService, Mockito.times(expectedCallsForCandidate))
         .getRequestCandidate(ArgumentMatchers.any());
      Mockito.verify(requestService, Mockito.times(expectedCallsToAssign))
         .assignRequest(ArgumentMatchers.anyString(), ArgumentMatchers.any());

      if (candidate != null && cars.size() == expectedCallsToAssign) {
         for (Car car : cars) {
            Assertions.assertNotNull(car.getRequest());
         }
      }
   }

   public static Stream<Object> requestData() {
      // cars, callsForCandidate, expectedRequest
      return Stream.of(
         // 1. No cars. Expect getRequestCandidate not called.
         new Object[]{
            new ArrayList<>(), // cars
            0, // callsForCandidate
            null, // candidate
            0 // expectedCallsToAssign
         },
         // 2. Cars are disabled. Expect getRequestCandidate not called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(false)
                  .build()
            ), // cars
            0, // expectedCallsForCandidate
            null, // candidate
            0 // expectedCallsToAssign
         },
         // 3. Cars are enabled. Expect getRequestCandidate is called.
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
            2, // expectedCallsForCandidate
            null, // candidate
            0, // expectedCallsToAssign
         },
         // 4. Car has a request. Expect getRequestCandidate is not called.
         new Object[]{
            List.of(
               Car.builder()
                  .id(0)
                  .enabled(true)
                  .request(CarRequest.builder().build())
                  .build()
            ),
            0, // expectedCallsForCandidate
            null, // candidate
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
            1, // expectedCallsForCandidate
            CarRequest.builder()
               .id("123")
               .build(), // candidate
            1 // expectedCallsToAssign
         }
      );
   }

}