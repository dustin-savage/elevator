package com.savage.svc.rest;

import com.savage.svc.dto.Car;
import com.savage.svc.services.api.CarService;
import com.savage.svc.services.api.RequestService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("elevator")
public class CarsController {
   private static final Logger LOGGER = LoggerFactory.getLogger(CarsController.class);

   private final CarService carService;
   private final RequestService requestService;

   @GetMapping("cars")
   public List<Car> getCars() {
      return carService.getCars();
   }

   @GetMapping("cars/{id}")
   public ResponseEntity<Car> getCar(@PathVariable("id") int id) {
      Car car = carService.getCarById(id);
      if (car != null) {
         return ResponseEntity.ok().body(car);
      }
      return ResponseEntity.notFound().build();
   }

   @PostMapping("cars/{id}/completeRequest")
   public ResponseEntity<Car> completeRequest(@PathVariable("id") int id, @RequestBody Car carParam) {
      if (carParam.getId() != id) {
         LOGGER.error("Bad request. Car id does not match path variable.");
         return ResponseEntity.badRequest().build();
      }
      Car car = carService.getCarById(id);
      if (car != null) {
         car.setCurrentFloor(carParam.getCurrentFloor());
         requestService.completeRequests(car);
         car.setRequest(null);
         return ResponseEntity.ok(car);
      }
      return ResponseEntity.notFound().build();
   }

}
