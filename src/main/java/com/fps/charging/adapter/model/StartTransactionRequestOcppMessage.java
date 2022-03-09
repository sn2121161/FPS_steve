package com.fps.charging.adapter.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ocpp.cs._2015._10.StartTransactionRequest;

@AllArgsConstructor
@Data
@Builder
public class StartTransactionRequestOcppMessage {

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("ChargePointIdentifier")
  private String chargeBoxId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("TransactionId")
  private Integer transactionId;

  @JsonInclude(Include.NON_NULL)
  @JsonProperty("StartTransactionRequest")
  private StartTransactionRequest startTransactionRequest;

}
