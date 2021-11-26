package com.fps.charging.listener;

import com.fps.charging.repository.OcppTagUpdateRepository;
import com.fps.charging.service.ChargingProfileService;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.StartTransactionRequest;
import ocpp.cs._2015._10.StartTransactionResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StartTransactionListener {

  private final ChargingProfileService chargingProfileService;
  private final OcppTagUpdateRepository ocppTagUpdateRepository;

  public void process(StartTransactionRequest parameters, String chargeBoxIdentity,
      StartTransactionResponse startTransactionResponse) {

    log.info(
        "Applying charging profile to charge Box for Ocpp Tag in StartTransaction operation, idTag={}, chargeBoxId={}",
        parameters.getIdTag(), chargeBoxIdentity);

    updateChargingPointInfoForOcppTag(parameters, chargeBoxIdentity);

    try {
      chargingProfileService.applyChargingProfile(parameters.getIdTag());
    } catch (Exception e) {
      log.error(
          "Error while applying charging profile to charge Box for Ocpp Tag in StartTransaction operation, idTag={}, chargeBoxId={}",
          parameters.getIdTag(), chargeBoxIdentity, e);
    }


  }

  private void updateChargingPointInfoForOcppTag(StartTransactionRequest parameters,
      String chargeBoxIdentity) {
    try {

      OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(
          parameters.getIdTag());

      if (ocppTag == null) {
        log.error("No ocpp_tag record is present for idTag={}", parameters.getIdTag());
      }
      ocppTagUpdateRepository.updateOcppTagWithChargingBoxIdAndConnectorId(ocppTag.getIdTag(),
          chargeBoxIdentity, parameters.getConnectorId());

    } catch (Exception e) {
      log.error(
          "Error while linking chargeBoxId with Ocpp Tag in StartTransaction operation, idTag={}, chargeBoxId={}",
          parameters.getIdTag(), chargeBoxIdentity, e);
    }
  }
}
