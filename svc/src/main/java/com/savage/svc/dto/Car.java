package com.savage.svc.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Value
@Jacksonized
public class Car {
   private static final Logger LOGGER = LoggerFactory.getLogger(Car.class);

   private int id;
   @With
   private final int currentFloor;
   @With
   private final Direction direction;
   @With
   private final boolean enabled;
   @With
   private final CarRequest request;

}
