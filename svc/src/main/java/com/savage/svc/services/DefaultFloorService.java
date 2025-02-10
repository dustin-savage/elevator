package com.savage.svc.services;

import com.savage.svc.dto.Floor;
import com.savage.svc.services.api.FloorService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Manages access to our list of floors.
 */
@Builder
public class DefaultFloorService implements FloorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFloorService.class);

    private final List<Floor> floors;
    private final int lobbyIndex;

    @Override
    public List<Floor> getFloors() {
        return floors;
    }

    @Override
    public Floor getFloorById(int id) {
        return floors.stream()
           .filter(f -> f.getId() == id)
           .findFirst().orElse(null);
    }

    @Override
    public Floor getLobbyFloor() {
        return this.getFloors().get(this.lobbyIndex);
   }

}
