package com.savage.svc.rest;

import com.savage.svc.dto.CarRequest;
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
public class RequestsController {
   private static final Logger LOGGER = LoggerFactory.getLogger(RequestsController.class);

   private final RequestService requestService;

   @GetMapping("requests")
   public List<CarRequest> getRequests() {
      return requestService.getRequests();
   }

   @GetMapping("requests/{id}")
   public ResponseEntity<CarRequest> getCarRequest(@PathVariable("id") String id) {
      CarRequest request = requestService.getRequestById(id);
      if (request != null) {
         return ResponseEntity.ok().body(request);
      }
      return ResponseEntity.notFound().build();
   }

   @PostMapping("requests")
   public CarRequest saveRequest(@RequestBody CarRequest request) {
      CarRequest createdRequest = this.requestService.addRequest(request);
      return createdRequest;
   }

}
