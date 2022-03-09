package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fps.charging.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.HeartbeatRequest;

@AllArgsConstructor
@Data
@Builder
public class HeartBeatRequestOcppMessage {

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("ChargePointIdentifier")
  private String chargeBoxId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("TransactionId")
  private Long transactionId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("HeartbeatRequest")
  private HeartbeatRequest heartbeatRequest;

  public static void main(String[] args) {
    String message = JsonUtils.toJson(HeartBeatRequestOcppMessage.builder()
        .chargeBoxId("chargeBoxId")
        .heartbeatRequest(new HeartbeatRequest())
        .build());
    System.out.println(message);
  }
}
