package com.savage.svc.dto;

import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Builder
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
@Value
@Jacksonized
public class CarRequest {
   @With
   private final String id;
   @With
   private final int floor;
   @With
   private final Direction direction;
   @With
   private final int assignedCarId; // -1 means unassigned
}
