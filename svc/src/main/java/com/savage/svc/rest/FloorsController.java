package com.savage.svc.rest;

import com.savage.svc.dto.Floor;
import com.savage.svc.services.api.FloorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("elevator")
public class FloorsController {
   private static final Logger LOGGER = LoggerFactory.getLogger(FloorsController.class);

   private final FloorService floorService;

   @GetMapping("floors")
   public List<Floor> getFloors() {
      return floorService.getFloors();
   }

   @GetMapping("floors/{id}")
   public ResponseEntity<Floor> getFloor(@PathVariable("id") int id) {
      Floor floor = floorService.getFloorById(id);
      if (floor != null) {
         return ResponseEntity.ok().body(floor);
      }
      return ResponseEntity.notFound().build();
   }

}
