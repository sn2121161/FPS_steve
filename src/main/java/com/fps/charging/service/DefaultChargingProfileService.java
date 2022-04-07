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
package com.fps.charging.service;

import com.fps.charging.adapter.mapper.ChargingProfileMapper;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import com.fps.charging.repository.OcppTagUpdateRepository;
import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.repository.OcppTagRepository;
import de.rwth.idsg.steve.repository.dto.ChargePoint.Details;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import de.rwth.idsg.steve.web.dto.ocpp.SetChargingProfileParams;
import java.util.ArrayList;
import java.util.List;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * This class is used to process new charging profile message coming from FPS
 *
 * @author Mehmet Dongel <mehmet.dongel@gmail.com>
 * @since 05.11.2021
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultChargingProfileService implements ChargingProfileService {

  private final ChargingProfileRepository chargingProfileRepository;
  private final OcppTagRepository ocppTagRepository;
  private final OcppTagUpdateRepository ocppTagUpdateRepository;
  private final ChargePointRepository chargePointRepository;


  @Autowired
  @Qualifier("ChargePointService16_Client")
  private ChargePointService16_Client client16;

  // Ths method updates the charging profile tables to store new charging profile
  // and updates ocpp_tag table to link vehicle rfid tag with the new charging profile
  @Override
  public void processChargingProfileMessage(ChargingProfileRequest chargingProfileRequest) {
    log.info(
        "Processing new charging profile message coming from FPS back office, idTag={}, chargingProfileId={}",
        chargingProfileRequest.getIdTag(), chargingProfileRequest.getChargingProfile());

    try {
      processDbOperations(chargingProfileRequest);
    } catch (Exception e) {
      log.error("Error while storing charging profile to db.", e);
    }
    try {
      applyChargingProfile(chargingProfileRequest.getIdTag());
    } catch (Exception e) {
      log.error(
          "Error while applying charging profile to charging point. chargingProfileRequest={}",
          chargingProfileRequest, e);
    }
  }

  // Ths method sends the charging profile to charge box
  public void applyChargingProfile(String idTag) {
    OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(idTag);

    if (ocppTag == null) {
      throw new SteveException(
          "No ocpp_tag record is present for idTag=" + idTag);
    }

    if (!isReadyforApplyChargingProfile(ocppTag)) {
      throw new SteveException(
          "Not suitable for apply charging profile. All of the fields "
              + "(ChargeBoxId,ChargingProfileId,ConnectorId) should be set for occpp_tag "
              + "record for idTag:"
              + idTag);
    }

    SetChargingProfileParams chargingProfileParams = new SetChargingProfileParams();
    chargingProfileParams.setChargingProfilePk(ocppTag.getChargingProfileId());
    chargingProfileParams.setConnectorId(ocppTag.getConnectorId());

    Details chargeBoxDetail = chargePointRepository.getByChargeBoxId(ocppTag.getChargeBoxId());

    if (chargeBoxDetail == null) {
      throw new SteveException(
          "No charge_box record is present for ChargeBoxId=" + ocppTag.getChargeBoxId());
    }

    OcppProtocol protocol = OcppProtocol.fromCompositeValue(
        chargeBoxDetail.getChargeBox().getOcppProtocol());

    List<ChargePointSelect> chargePointSelectList = new ArrayList<>();
    chargePointSelectList.add(new ChargePointSelect(protocol.getTransport(),
        chargeBoxDetail.getChargeBox().getChargeBoxId(),
        chargeBoxDetail.getChargeBox().getEndpointAddress()));
    chargingProfileParams.setChargePointSelectList(chargePointSelectList);

    client16.setChargingProfile(chargingProfileParams);

    log.info("SUCCESSFULLY SENT CHARGING PROFILE: {} ", chargingProfileParams);
  }

  private boolean isReadyforApplyChargingProfile(OcppTagRecord ocppTag) {
    return StringUtils.hasText(ocppTag.getChargeBoxId())
        && ocppTag.getConnectorId() != null
        && ocppTag.getChargingProfileId() != null;
  }

  private void processDbOperations(ChargingProfileRequest chargingProfileRequest) {
    ChargingProfileForm chargingProfileForm = ChargingProfileMapper.toChargingProfile(
        chargingProfileRequest);
    System.out.println(chargingProfileForm);

    int chargingProfileId = chargingProfileRepository.add(chargingProfileForm);

    OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(
        chargingProfileRequest.getIdTag());

    if (ocppTag == null) {
      log.error("No ocpp_tag record is present for idTag={}", chargingProfileRequest.getIdTag());
      return;
    }

    try {
      ocppTagUpdateRepository.updateOcppTagWithChargingProfile(ocppTag.getIdTag(),
          chargingProfileId);
    } catch (Exception e) {
      log.error("Unable to assign idTag={} with chargingProfile={}, exception is = {}",
          chargingProfileRequest.getIdTag(), chargingProfileId, e);

    }
  }
}
