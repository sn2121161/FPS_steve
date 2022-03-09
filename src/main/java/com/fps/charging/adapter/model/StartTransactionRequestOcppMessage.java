package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fps.charging.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.StartTransactionRequest;
import org.joda.time.DateTime;

@AllArgsConstructor
@Data
@Builder
@JsonPropertyOrder({ "startTransactionRequest", "chargeBoxId", "transactionId"})
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
        .chargeBoxId("chargeBoxId")
        .transactionId("12345")
        .startTransactionRequest(new StartTransactionRequest()
            .withConnectorId(1)
            .withIdTag("ABC2334")
            .withTimestamp(DateTime.now())
            .withMeterStart(0))
        .build();

    String message = JsonUtils.toJson(build);
    System.out.println(message);
  }

}
