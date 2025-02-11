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
public class Floor {
   private final int id;
   private final String name;
}
