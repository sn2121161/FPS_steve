package com.fps.charging.service;

import com.fps.charging.adapter.mapper.ChargingProfileMapper;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import com.fps.charging.repository.OcppTagUpdateRepository;
import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.OcppTransport;
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

  @Override
  public void processChargingProfileMessage(ChargingProfileRequest chargingProfileRequest) {

    processDbOperations(chargingProfileRequest);

    applyChargingProfile(chargingProfileRequest);


  }

  private void applyChargingProfile(ChargingProfileRequest chargingProfileRequest) {

    OcppTagRecord ocppTag = ocppTagUpdateRepository.findByOcppTag(
        chargingProfileRequest.getIdTag());

//    chargePointRepository.getDetails()

    SetChargingProfileParams chargingProfileParams = new SetChargingProfileParams();
    chargingProfileParams.setChargingProfilePk(ocppTag.getChargingProfileId());
    chargingProfileParams.setConnectorId(ocppTag.getConnectorId());

    Details chargeBoxDetail = chargePointRepository.getByChargeBoxId(ocppTag.getChargeBoxId());

    List<ChargePointSelect> chargePointSelectList = new ArrayList<>();
    chargePointSelectList.add(new ChargePointSelect(OcppTransport.SOAP,
        chargeBoxDetail.getChargeBox().getChargeBoxId(),
        chargeBoxDetail.getChargeBox().getEndpointAddress()));
    chargingProfileParams.setChargePointSelectList(chargePointSelectList);

    client16.setChargingProfile(chargingProfileParams);


    log.info("SUCCESSFULLY SENT CHARGING PROFILE: {} ", chargingProfileParams);

  }

  private void processDbOperations(ChargingProfileRequest chargingProfileRequest) {
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
