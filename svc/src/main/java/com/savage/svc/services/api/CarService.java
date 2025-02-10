package com.savage.svc.services.api;

import com.savage.svc.dto.Car;

import java.util.List;

public interface CarService {
   List<Car> getCars();

   Car getCarById(int id);

}
