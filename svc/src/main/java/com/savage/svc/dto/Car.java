package com.savage.svc.dto;

import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Car {
   private static final Logger LOGGER = LoggerFactory.getLogger(Car.class);

   private int id;
   private int currentFloor;
   private Direction direction;
   private boolean isDoorOpen;
   private boolean enabled;
   private CarRequest request;

}
