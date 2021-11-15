package com.fps.charging.service;

import com.fps.charging.adapter.mapper.ChargingProfileMapper;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import com.fps.charging.repository.OcppTagUpdateRepository;
import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import de.rwth.idsg.steve.repository.OcppTagRepository;
import de.rwth.idsg.steve.web.dto.ChargingProfileForm;
import jooq.steve.db.tables.records.OcppTagRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultChargingProfileService implements ChargingProfileService {

  private final ChargingProfileRepository chargingProfileRepository;
  private final OcppTagRepository ocppTagRepository;
  private final OcppTagUpdateRepository ocppTagUpdateRepository;

  @Override
  public void processChargingProfileMessage(ChargingProfileRequest chargingProfileRequest) {

    ChargingProfileForm chargingProfileForm = ChargingProfileMapper.toChargingProfile(
        chargingProfileRequest);
    System.out.println(chargingProfileForm);

    int chargingProfileId = chargingProfileRepository.add(chargingProfileForm);

    OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(
        chargingProfileRequest.getIdTag());

    if (ocppTag == null) {
      throw new SteveException(
          "No ocpp_tag record is present for idTag=" + chargingProfileRequest.getIdTag());
    }

    ocppTagUpdateRepository.updateOcppTagWithChargingProfile(ocppTag.getIdTag(),
        chargingProfileId);

  }
}
