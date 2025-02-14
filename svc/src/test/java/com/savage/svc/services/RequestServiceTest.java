package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import com.savage.svc.dto.Direction;
import com.savage.svc.services.api.CarService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class RequestServiceTest {

   /**
    * Parameterized test for the DefaultRequestService.getRequestCandidate() method.
    */
   @ParameterizedTest
   @MethodSource("assignRequestData")
   void assignRequestTest(Car car, List<CarRequest> requests, String expectedRequestId) {
      CarService carService = Mockito.mock(CarService.class);
      Mockito.doAnswer(invocationOnMock -> {
         Car carToSave = invocationOnMock.getArgument(0);
         return carToSave;
      }).when(carService).save(ArgumentMatchers.any());

      // GIVEN - a DefaultRequestService
      DefaultRequestService defaultRequestService = DefaultRequestService.builder()
         .requests(requests)
         .carService(carService)
         .build();

      // WHEN - assignRequest() is called
      Car carUpdate = defaultRequestService.assignRequest(car);

      // THEN - the expected request is assigned to the car.
      if (expectedRequestId == null) {
         // Expect no requests are appropriate for the car.
         Assertions.assertNull(carUpdate.getRequest());
      } else {
         // Confirm that the expected request was chosen.
         Assertions.assertNotNull(carUpdate.getRequest());
         Assertions.assertEquals(expectedRequestId, carUpdate.getRequest().getId());
      }
   }

   public static Stream<Object> assignRequestData() {
      // The following stream is passed as parameters to the above test.
      return Stream.of(
         // 1. One unassigned request. Should be assigned to the car.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .build(), // car
            new ArrayList<>(List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build()
            )), // requests
            "a" // expectedRequestId
         },
         // 2. One request is already assigned to another car. Expect no match.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            new ArrayList<>(List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.UP)
                  .assignedCarId(1)
                  .build()
            )), // requests
            null // expectedRequestId
         },
         // 3. Multiple unassigned requests in UP direction. Closest one should be chosen.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            new ArrayList<>(List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("b")
                  .floor(2)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("c")
                  .floor(1)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build()
            )), // requests
            "c" // expectedRequestId
         },
         // 4. Multiple unassigned requests in DOWN direction. Furthest one should be chosen.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            new ArrayList<>(List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.DOWN)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("b")
                  .floor(2)
                  .direction(Direction.DOWN)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("c")
                  .floor(1)
                  .direction(Direction.DOWN)
                  .assignedCarId(-1)
                  .build()
            )), // requests
            "a" // expectedRequestId
         },
         // 5. Internal request on the way gets assigned.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            new ArrayList<>(List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(2)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("b")
                  .floor(2)
                  .direction(Direction.DOWN)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("c")
                  .floor(1)
                  .direction(Direction.DOWN)
                  .assignedCarId(-1)
                  .build(),
               CarRequest.builder()
                  .id("d")
                  .floor(1)
                  .direction(null) // internal requests have no direction.
                  .assignedCarId(-1)
                  .build()
            )), // requests
            "d" // expectedRequestId
         }
         // TODO: Add more test cases
      );
   }

}