package com.savage.svc.dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class CarRequest {
   private String id;
   private int floor;
   private Direction direction;
   private int assignedCarId;
}
