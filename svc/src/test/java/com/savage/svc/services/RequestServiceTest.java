package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import com.savage.svc.dto.Direction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

class RequestServiceTest {

   /**
    * Parameterized test for the DefaultRequestService.getRequestCandidate() method.
    */
   @ParameterizedTest
   @MethodSource("requestCandidateData")
   void getRequestCandidateTest(Car car, List<CarRequest> requests, String expectedRequestId) {

      // GIVEN - a DefaultRequestService
      DefaultRequestService defaultRequestService = DefaultRequestService.builder()
         .requests(requests)
         .build();

      // WHEN - getRequestCandidate() is called
      CarRequest request = defaultRequestService.getRequestCandidate(car);

      // THEN - the expected request is returned.
      if (expectedRequestId == null) {
         // Expect no requests are appropriate for the car.
         Assertions.assertNull(request);
      } else {
         // Confirm that the expected request was chosen.
         Assertions.assertNotNull(request);
         Assertions.assertEquals(expectedRequestId, request.getId());
      }
   }

   public static Stream<Object> requestCandidateData() {
      // The following stream is passed as parameters to the above test.
      return Stream.of(
         // 1. One unassigned request. Should be assigned to the car.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .build(), // car
            List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.UP)
                  .assignedCarId(-1)
                  .build()
            ), // requests
            "a" // expectedRequestId
         },
         // 2. One request is already assigned to another car. Expect no match.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            List.of(
               CarRequest.builder()
                  .id("a")
                  .floor(3)
                  .direction(Direction.UP)
                  .assignedCarId(1)
                  .build()
            ), // requests
            null // expectedRequestId
         },
         // 3. Multiple unassigned requests in UP direction. Closest one should be chosen.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            List.of(
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
            ), // requests
            "c" // expectedRequestId
         },
         // 4. Multiple unassigned requests in DOWN direction. Furthest one should be chosen.
         new Object[]{
            Car.builder()
               .id(2)
               .currentFloor(0)
               .direction(Direction.UP)
               .build(), // car
            List.of(
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
            ), // requests
            "a" // expectedRequestId
         }
         // TODO: Add more test cases
      );
   }

}