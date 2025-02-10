package com.savage.svc.services.api;

import com.savage.svc.dto.Car;
import com.savage.svc.dto.CarRequest;

import java.util.List;

public interface RequestService {
   CarRequest addRequest(CarRequest request);

   CarRequest getRequestCandidate(Car car);

   boolean assignRequest(String requestId, Car car);

   List<CarRequest> getRequests();

   CarRequest getRequestById(String id);

   void completeRequests(Car car);
}
