package com.savage.svc.services;

import com.savage.svc.dto.Floor;
import com.savage.svc.services.api.FloorService;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.StampedLock;

/**
 * Manages access to our list of floors.
 */
@Builder
public class DefaultFloorService implements FloorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFloorService.class);

    private final List<Floor> floors;
    private final int lobbyIndex;
    @Builder.Default
    private final StampedLock lock = new StampedLock();

    @Override
    public List<Floor> getFloors() {
        long stamp = lock.readLock();
        try {
            return Collections.unmodifiableList(floors);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Floor getFloorById(int id) {
        long stamp = lock.readLock();
        try {
            return floors.stream()
               .filter(f -> f.getId() == id)
               .findFirst().orElse(null);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public Floor getLobbyFloor() {
        long stamp = lock.readLock();
        try {
            return this.getFloors().get(this.lobbyIndex);
        } finally {
            lock.unlockRead(stamp);
        }
   }

}
