package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"connectorId", "transactionId", "meterValue"})
public class MeterValuesRequest {

  private int connectorId;
  private Integer transactionId;
  private List<MeterValue> meterValue;
}
