package com.savage.svc.services.api;

import com.savage.svc.dto.Floor;

import java.util.List;

public interface FloorService {

   List<Floor> getFloors();

   Floor getFloorById(int id);

   Floor getLobbyFloor();
}
