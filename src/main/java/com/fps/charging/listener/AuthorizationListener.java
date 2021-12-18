/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2021 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.fps.charging.listener;

import com.fps.charging.repository.OcppTagUpdateRepository;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.AuthorizationStatus;
import ocpp.cs._2015._10.AuthorizeRequest;
import ocpp.cs._2015._10.AuthorizeResponse;
import org.springframework.stereotype.Service;

/**
 * This class is used to intercept Authorization request coming from charger and map vehicle RFID tag with charging box id
 *
 * @author Mehmet Dongel <mehmet.dongel@gmail.com>
 * @since 10.11.2021
 */
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
