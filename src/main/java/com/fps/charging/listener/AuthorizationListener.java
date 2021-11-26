package com.fps.charging.listener;

import com.fps.charging.repository.OcppTagUpdateRepository;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.AuthorizationStatus;
import ocpp.cs._2015._10.AuthorizeRequest;
import ocpp.cs._2015._10.AuthorizeResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationListener {

  private final OcppTagUpdateRepository ocppTagUpdateRepository;

  public void process(AuthorizeRequest parameters, String chargeBoxIdentity,
      AuthorizeResponse authorizeResponse) {
    try {
      if (authorizeResponse.getIdTagInfo().getStatus() != null &&
          authorizeResponse.getIdTagInfo().getStatus().equals(AuthorizationStatus.ACCEPTED)) {
        OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(
            parameters.getIdTag());

        if (ocppTag == null) {
          log.error("No ocpp_tag record is present for idTag={}", parameters.getIdTag());
          return;
        }
        ocppTagUpdateRepository.updateOcppTagWithChargingBoxIdAndConnectorId(ocppTag.getIdTag(),
            chargeBoxIdentity,1);
      }
    } catch (Exception e) {
      log.error(
          "Error while linking chargeBoxId with Ocpp Tag in authorize operation, idTag={}, chargeBoxId={}",
          parameters.getIdTag(), chargeBoxIdentity, e);
    }


  }
}
