package com.savage.svc.services;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;
import com.savage.svc.dto.Direction;
import com.savage.svc.services.api.CarService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

/**
 * Manages access to the list of requests.
 */
@Builder
@AllArgsConstructor
public class DefaultRequestService implements com.savage.svc.services.api.RequestService {

   private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestService.class);

   private final int minFloor;
   private final int maxFloor;
   private final List<CarRequest> requests;
   private final CarService carService;
   @Builder.Default
   private final StampedLock lock = new StampedLock();

   /**
    * Add a request. For example this would be called when someone presses a button in the elevator,
    * or an up/down arrow on the wall outside the elevator car.
    */
   @Override
   public CarRequest addRequest(CarRequest request) {
      if (request.getFloor() < minFloor || request.getFloor() > maxFloor) {
         throw new IllegalArgumentException("Request Floor out of range.");
      }

      long stamp = lock.writeLock();
      try {
         CarRequest addedRequest = request.withId(UUID.randomUUID().toString());
         requests.add(addedRequest);
         return addedRequest;
      } finally {
         lock.unlockWrite(stamp);
      }
   }

   /**
    * Gets a candidate request for a given car based on position (which floor the car is on relative to the request),
    * and direction the car is moving.
    * There is opportunity to simplify and improve here.
    * Will determine the "best" request for the car, and if one exists, assign it to the car.
    */
   @Override
   public Car assignRequest(Car car) {
      if (car == null) {
         throw new IllegalArgumentException("Car must not be null");
      }
      if (car.getRequest() != null) {
         // Car already has a request. Return it.
         return car;
      }

      // TODO: Simplify this method's logic after tests are written.
      CarRequest req;
      long stamp = lock.writeLock();
      try {
         List<CarRequest> candidates = requests.stream()
            .filter(r -> r.getAssignedCarId() == -1 || r.getAssignedCarId() == car.getId())
            .toList();
         if (!candidates.isEmpty()) {
            // Car has work. Need to get top priority for this car.
            // Find first request in car's direction
            req = candidates.stream()
               // Any requests going in same direction
               .filter(r -> r.getDirection() == car.getDirection() || r.getDirection() == null)
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
                  .filter(r -> r.getDirection() != null && r.getDirection() != car.getDirection())
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
            if (req != null) {
               // Assign the request to the car so it knows to move. A car can only have one active request at a time,
               // and the request will be removed when the move is complete via the completeRequest() method.
               CarRequest updatedRequest = req.withAssignedCarId(car.getId());
               int i = requests.indexOf(req);
               requests.remove(req);
               requests.add(i, updatedRequest);
               // Update the car and save it
               Car carUpdate = car.withDirection(car.getCurrentFloor() <= req.getFloor() ? Direction.UP : Direction.DOWN);
               carUpdate = carUpdate.withRequest(req);
               return this.carService.save(carUpdate);
            }
         }
      } finally {
         lock.unlockWrite(stamp);
      }
      return car;
   }

   @Override
   public List<CarRequest> getRequests() {
      long stamp = lock.readLock();
      try {
         return Collections.unmodifiableList(requests);
      } finally {
         lock.unlockRead(stamp);
      }
   }

   @Override
   public CarRequest getRequestById(String id) {
      long stamp = lock.readLock();
      try {
         return requests.stream()
            .filter(r -> id != null && id.equals(r.getId()))
            .findFirst().orElse(null);
      } finally {
         lock.unlockRead(stamp);
      }
   }

   /**
    * Complete all requests for a given car.
    * This includes the request currently set on the car, but can also include other requests that
    * happen to be fulfilled by the car's current state.
    * This method is called after the elevator car door opens. Any requests on that floor, not assigned to other cars,
    * and going in the same direction are considered completed. They are safe to discard.
    */
   @Override
   public void completeRequests(Car car) {
      long stamp = lock.writeLock();
      try {
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
      } finally {
         lock.unlockWrite(stamp);
      }
   }

}
