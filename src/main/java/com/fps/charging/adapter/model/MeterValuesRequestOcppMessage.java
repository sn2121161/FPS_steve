package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.MeterValuesRequest;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({ "meterValuesRequest", "chargeBoxId"})
public class MeterValuesRequestOcppMessage {
  @JsonInclude(Include.NON_NULL)
  @JsonProperty("ChargePointIdentifier")
  private String chargeBoxId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("MeterValuesRequest")
  private MeterValuesRequest meterValuesRequest;
}
