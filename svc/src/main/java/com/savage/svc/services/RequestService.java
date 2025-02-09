package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.Direction;
import com.savage.svc.dto.CarRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Builder
@AllArgsConstructor
public class RequestService {

   private static final Logger LOGGER = LoggerFactory.getLogger(RequestService.class);

   private final int minFloor;
   private final int maxFloor;
   private final List<CarRequest> requests;

   public CarRequest addRequest(CarRequest request) {
      if (request.getFloor() < minFloor || request.getFloor() > maxFloor) {
         throw new IllegalArgumentException("Request Floor out of range.");
      }
      request.setId(UUID.randomUUID().toString());
      requests.add(request);
      return request;
   }

   public CarRequest getRequestCandidate(Car car) {
      // TODO: Simplify this method's logic.
      CarRequest req = null;
      List<CarRequest> candidates = requests.stream()
         .filter(r -> r.getAssignedCarId() == -1 || r.getAssignedCarId() == car.getId())
         .toList();
      if (!candidates.isEmpty()) {
         // Car has work. Need to get top priority for this car.
         // Find first request in car's direction
         req = candidates.stream()
            // Any requests going in same direction
            .filter(r -> r.getDirection() == car.getDirection())
            // Want requests above car if going up, below car if going down.
            .filter(r -> car.getDirection() == Direction.UP ? r.getFloor() >= car.getCurrentFloor() : r.getFloor() <= car.getCurrentFloor())
            // Sort requests by closest distance to car
            .sorted(Comparator.comparing((CarRequest r) -> Math.abs(car.getCurrentFloor() - r.getFloor())))
            // Find first request only, otherwise null
            .findFirst().orElse(null);
         if (req == null) {
            // Check the other direction
            req = candidates.stream()
               // Any requests the car is heading to but going in the opposite direction
               .filter(r -> r.getDirection() != car.getDirection())
               // Want requests above car if going up, below car if going down.
               .filter(r -> car.getDirection() == Direction.UP ? r.getFloor() >= car.getCurrentFloor() : r.getFloor() <= car.getCurrentFloor())
               // Sort requests by furthest distance from the car
               .sorted(Comparator.comparing((CarRequest r) -> Math.abs(car.getCurrentFloor() - r.getFloor())).reversed())
               // Find first request only, otherwise null
               .findFirst().orElse(null);
         }
         if (req == null) {
            // Prioritize internal requests (from people in the car)
            req = candidates.stream()
               // Any requests the car is heading to but going in the opposite direction
               .filter(r -> r.getAssignedCarId() == car.getId())
               // Sort requests by closest distance to the car
               .sorted(Comparator.comparing((CarRequest r) -> Math.abs(car.getCurrentFloor() - r.getFloor())))
               // Find first request only, otherwise null
               .findFirst().orElse(null);
         }
         if (req == null) {
            // Just pick the closest one then
            req = candidates.stream()
               // Sort requests by distance to car
               .sorted(Comparator.comparing((CarRequest r) -> Math.abs(car.getCurrentFloor() - r.getFloor())))
               // Find first request only, otherwise null
               .findFirst().orElse(null);
         }
      }
      return req;
   }

   public boolean assignRequest(String requestId, Car car) {
      if (requestId == null) {
         throw new IllegalArgumentException("Request Id must not be null.");
      }
      // Find request by id
      CarRequest request = requests.stream()
         .filter(r -> r.getId().equals(requestId))
         .findFirst().orElse(null);
      if (request != null) {
         // Assign it to the car.
         request.setAssignedCarId(car.getId());
         return true;
      }
      return false;
   }

   public List<CarRequest> getRequests() {
      return requests;
   }

   public CarRequest getRequestById(String id) {
      return requests.stream()
         .filter(r -> id != null && id.equals(r.getId()))
         .findFirst().orElse(null);
   }

   public void completeRequests(Car car) {
      // Find requests that car is currently fulfilling based on its floor and direction
      List<CarRequest> completeRequests = requests.stream()
         .filter(r -> r.getFloor() == car.getCurrentFloor())
         .filter(r -> (r.getAssignedCarId() == car.getId())
            || (r.getAssignedCarId() == -1 && r.getDirection() == car.getDirection()))
         .toList();
      if (!completeRequests.isEmpty()) {
         // Log completion and remove from request list.
         LOGGER.info("Requests completed: " + completeRequests);
         requests.removeAll(completeRequests);
      }
   }

}
