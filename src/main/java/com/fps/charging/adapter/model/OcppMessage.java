package com.fps.charging.adapter.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class OcppMessage {
  private String chargeBoxId;
  private OccpMessageType occpMessageType;
  private Object body;
}
