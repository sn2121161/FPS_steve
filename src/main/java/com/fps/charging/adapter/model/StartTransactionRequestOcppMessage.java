package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fps.charging.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.joda.time.DateTime;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({"startTransactionRequest", "chargeBoxId", "transactionId"})
public class StartTransactionRequestOcppMessage {

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("ChargePointIdentifier")
  private String chargeBoxId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("TransactionId")
  private String transactionId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("StartTransactionRequest")
  private StartTransactionRequest startTransactionRequest;


  public static void main(String[] args) {

    StartTransactionRequestOcppMessage build = StartTransactionRequestOcppMessage.builder()
        .chargeBoxId("Liv01")
        .transactionId("261")
        .startTransactionRequest(StartTransactionRequest.builder()
            .connectorId(1)
            .idTag("048039EA726C80")
            .timestamp(DateTime.now())
            .meterStart(174992).build())
        .build();

    String message = JsonUtils.toJson(build);
    System.out.println(message);
  }

}
