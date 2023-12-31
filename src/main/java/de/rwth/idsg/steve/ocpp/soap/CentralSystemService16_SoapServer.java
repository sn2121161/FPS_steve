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
package de.rwth.idsg.steve.ocpp.soap;

import com.fps.charging.JsonUtils;
import com.fps.charging.adapter.model.HeartBeatRequestOcppMessage;
import com.fps.charging.adapter.model.MeterValue;
import com.fps.charging.adapter.model.MeterValuesRequestOcppMessage;
import com.fps.charging.adapter.model.SampledValue;
import com.fps.charging.adapter.model.StartTransactionRequestOcppMessage;
import com.fps.charging.adapter.model.StatusNotificationRequestOcppMessage;
import com.fps.charging.adapter.model.StopTransactionRequestOcppMessage;
import com.fps.charging.listener.AuthorizationListener;
import com.fps.charging.listener.StartTransactionListener;
import com.fps.charging.service.MessageSender;
import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.service.CentralSystemService16_Service;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.jws.WebService;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.BindingType;
import javax.xml.ws.Response;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.SOAPBinding;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.AuthorizeRequest;
import ocpp.cs._2015._10.AuthorizeResponse;
import ocpp.cs._2015._10.BootNotificationRequest;
import ocpp.cs._2015._10.BootNotificationResponse;
import ocpp.cs._2015._10.CentralSystemService;
import ocpp.cs._2015._10.DataTransferRequest;
import ocpp.cs._2015._10.DataTransferResponse;
import ocpp.cs._2015._10.DiagnosticsStatusNotificationRequest;
import ocpp.cs._2015._10.DiagnosticsStatusNotificationResponse;
import ocpp.cs._2015._10.FirmwareStatusNotificationRequest;
import ocpp.cs._2015._10.FirmwareStatusNotificationResponse;
import ocpp.cs._2015._10.HeartbeatRequest;
import ocpp.cs._2015._10.HeartbeatResponse;
import ocpp.cs._2015._10.MeterValuesRequest;
import ocpp.cs._2015._10.MeterValuesResponse;
import ocpp.cs._2015._10.StartTransactionRequest;
import ocpp.cs._2015._10.StartTransactionResponse;
import ocpp.cs._2015._10.StatusNotificationRequest;
import ocpp.cs._2015._10.StatusNotificationResponse;
import ocpp.cs._2015._10.StopTransactionRequest;
import ocpp.cs._2015._10.StopTransactionResponse;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 13.03.2018
 */
@Slf4j
@Service
@Addressing(enabled = true, required = false)
@BindingType(value = SOAPBinding.SOAP12HTTP_BINDING)
@WebService(
    serviceName = "CentralSystemService",
    portName = "CentralSystemServiceSoap12",
    targetNamespace = "urn://Ocpp/Cs/2015/10/",
    endpointInterface = "ocpp.cs._2015._10.CentralSystemService")
public class CentralSystemService16_SoapServer implements CentralSystemService {

  @Autowired
  private MessageSender messageSender;

  @Autowired
  private CentralSystemService16_Service service;

  @Autowired
  private AuthorizationListener authorizationListener;

  @Autowired
  private StartTransactionListener startTransactionListener;

  public BootNotificationResponse bootNotificationWithTransport(BootNotificationRequest parameters,
      String chargeBoxIdentity, OcppProtocol protocol) {
    if (protocol.getVersion() != OcppVersion.V_16) {
      throw new IllegalArgumentException("Unexpected OCPP version: " + protocol.getVersion());
    }
    return service.bootNotification(parameters, chargeBoxIdentity, protocol);
  }

  @Override
  public BootNotificationResponse bootNotification(BootNotificationRequest parameters,
      String chargeBoxIdentity) {
    return this.bootNotificationWithTransport(parameters, chargeBoxIdentity,
        OcppProtocol.V_16_SOAP);
  }

  @Override
  public FirmwareStatusNotificationResponse firmwareStatusNotification(
      FirmwareStatusNotificationRequest parameters,
      String chargeBoxIdentity) {
    return service.firmwareStatusNotification(parameters, chargeBoxIdentity);
  }

  @Override
  public StatusNotificationResponse statusNotification(StatusNotificationRequest parameters,
      String chargeBoxIdentity) {

    StatusNotificationResponse statusNotificationResponse = service.statusNotification(parameters,
        chargeBoxIdentity);
    if (statusNotificationResponse != null) {
      try {
        StatusNotificationRequestOcppMessage statusNotificationRequestOcppMessage = StatusNotificationRequestOcppMessage.builder()
            .chargeBoxId(chargeBoxIdentity)
            .statusNotificationRequest(
                com.fps.charging.adapter.model.StatusNotificationRequest.builder()
                    .status(parameters.getStatus()==null?null:parameters.getStatus().name())
                    .errorCode(parameters.getErrorCode()==null?null:parameters.getErrorCode().name())
                    .connectorId(parameters.getConnectorId())
                    .info(parameters.getInfo())
                    .vendorErrorCode(parameters.getVendorErrorCode())
                    .vendorId(parameters.getVendorId())
                    .timestamp(parameters.getTimestamp())
                    .build())
            .build();
        log.info(
            "Created StatusNotificationRequestOcppMessage [{}] for topic to send to FPS back office",
            statusNotificationRequestOcppMessage);
        sendMessage(statusNotificationRequestOcppMessage);
        log.info(
            "Sent StatusNotificationRequestOcppMessage [{}] for topic to send to FPS back office",
            statusNotificationRequestOcppMessage);
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Exception while creating and sending FPS back office message for StatusNotificationReques",e);
      }

    }
    return statusNotificationResponse;
  }

  @Override
  public MeterValuesResponse meterValues(MeterValuesRequest parameters, String chargeBoxIdentity) {
    MeterValuesResponse meterValuesResponse = service.meterValues(parameters, chargeBoxIdentity);

    if (meterValuesResponse != null) {
      MeterValuesRequestOcppMessage occpMessage = null;
      try {
        occpMessage = buildMeterValuesRequestOcppMessage(parameters,
            chargeBoxIdentity);
        log.info("Created MeterValuesRequest:[{}] for topic to send to FPS back office", occpMessage);
        sendMessage(occpMessage);
        log.info("Sent MeterValuesRequest:[{}] for topic to send to FPS back office", occpMessage);
      } catch (Exception e) {
        log.error("Exception while creating and sending FPS back office message for MeterValuesRequest",e);
      }

    }

    return meterValuesResponse;
  }

  private MeterValuesRequestOcppMessage buildMeterValuesRequestOcppMessage(
      MeterValuesRequest parameters,
      String chargeBoxIdentity) {
    return MeterValuesRequestOcppMessage.builder()
        .chargeBoxId(chargeBoxIdentity)
        .meterValuesRequest(com.fps.charging.adapter.model.MeterValuesRequest.builder()
            .connectorId(parameters.getConnectorId())
            .transactionId(parameters.getTransactionId())
            .meterValue(parameters.getMeterValue().stream()
                .map(m -> buildMeterValue(m)).collect(Collectors.toList())).build())
        .build();
  }

  private MeterValue buildMeterValue(ocpp.cs._2015._10.MeterValue m) {
    return MeterValue.builder()
        .timestamp(m.getTimestamp())
        .sampledValue(
            m.getSampledValue().stream().map(s -> SampledValue.builder()
                .format(s.getFormat())
                .location(s.getLocation())
                .measurand(s.getMeasurand())
                .phase(s.getPhase())
                .context(s.getContext() == null ? null : s.getContext().name())
                .unit(s.getUnit())
                .value(s.getValue())
                .build()).collect(
                Collectors.toList()))
        .build();
  }

  @Override
  public DiagnosticsStatusNotificationResponse diagnosticsStatusNotification(
      DiagnosticsStatusNotificationRequest parameters, String chargeBoxIdentity) {
    return service.diagnosticsStatusNotification(parameters, chargeBoxIdentity);
  }

  @Override
  public StartTransactionResponse startTransaction(StartTransactionRequest parameters,
      String chargeBoxIdentity) {
    StartTransactionResponse startTransactionResponse = service.startTransaction(parameters,
        chargeBoxIdentity);
    // todo: check response
    if (startTransactionResponse.isSetTransactionId()) {
      try {
        StartTransactionRequestOcppMessage startTransactionRequestOcppMessage = StartTransactionRequestOcppMessage.builder()
            .chargeBoxId(chargeBoxIdentity)
            .startTransactionRequest(com.fps.charging.adapter.model.StartTransactionRequest.builder()
                .meterStart(parameters.getMeterStart())
                .connectorId(parameters.getConnectorId())
                .idTag(parameters.getIdTag())
                .reservationId(parameters.getReservationId())
                .timestamp(parameters.getTimestamp())
                .build())
            .transactionId(String.valueOf(startTransactionResponse.getTransactionId()))
            .build();
        log.info("Created StartTransactionRequest:[{}] for topic to send to FPS back office",
            startTransactionRequestOcppMessage);
        sendMessage(startTransactionRequestOcppMessage);
        log.info("Sent StartTransactionRequest:[{}] for topic to send to FPS back office",
            startTransactionRequestOcppMessage);
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Exception while creating and sending FPS back office message for StartTransactionRequest",e);
      }

      try {
        startTransactionListener.process(parameters, chargeBoxIdentity, startTransactionResponse);
      } catch (Exception e) {
        log.error("Exception while handling    startTransaction message.", e);
      }
    }

    return startTransactionResponse;
  }

  @Override
  public StopTransactionResponse stopTransaction(StopTransactionRequest parameters,
      String chargeBoxIdentity) {
    StopTransactionResponse stopTransactionResponse = service.stopTransaction(parameters,
        chargeBoxIdentity);
    if (stopTransactionResponse.isSetIdTagInfo()) {
      try {
        StopTransactionRequestOcppMessage occpMessage = buildStopTransactionRequestOcppMessage(
            parameters, chargeBoxIdentity);
        log.info("Created StopTransactionRequest:[{}] for topic to send to FPS back office", occpMessage);
        sendMessage(occpMessage);
        log.info("Sent StopTransactionRequest:[{}] for topic to send to FPS back office", occpMessage);
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Exception while creating and sending FPS back office message for StopTransactionRequest",e);
      }

    }
    return stopTransactionResponse;
  }

  private StopTransactionRequestOcppMessage buildStopTransactionRequestOcppMessage(
      StopTransactionRequest parameters,
      String chargeBoxIdentity) {

    List<MeterValue> transactionData;
    if (parameters.getTransactionData().isEmpty()) {
      transactionData = Arrays.asList(MeterValue.builder()
          .timestamp(DateTime.now())
          .sampledValue(Arrays.asList(SampledValue.builder()
              .build()))
          .build());
    } else {
      transactionData = parameters.getTransactionData().stream()
          .map(m -> buildMeterValue(m))
          .collect(Collectors.toList());
    }

    return StopTransactionRequestOcppMessage.builder()
        .chargeBoxId(chargeBoxIdentity)
        .stopTransactionRequest(com.fps.charging.adapter.model.StopTransactionRequest.builder()
            .meterStop(parameters.getMeterStop())
            .idTag(parameters.getIdTag())
            .reason(parameters.getReason()==null?null:parameters.getReason().name())
            .transactionId(parameters.getTransactionId())
            .timestamp(parameters.getTimestamp())
            .transactionData(transactionData)
            .build())
        .build();
  }

  @Override
  public HeartbeatResponse heartbeat(HeartbeatRequest parameters, String chargeBoxIdentity) {
    log.info(
        "Creating HeartbeatRequest for  chargeBoxIdentity={} [message not processed yet]  for topic to send to FPS back office",
        chargeBoxIdentity);
    HeartbeatResponse heartbeat = service.heartbeat(parameters, chargeBoxIdentity);
    log.info(
        "Creating HeartbeatRequest for  chargeBoxIdentity={} [message processed with response=[{}]  for topic to send to FPS back office",
        chargeBoxIdentity,heartbeat);
    log.info(
        "Creating HeartbeatRequest for  chargeBoxIdentity={}  for topic to send to FPS back office",
        chargeBoxIdentity);
    if (heartbeat.isSetCurrentTime()) {
      try {
        log.info(
            "Creating HeartbeatRequest for  chargeBoxIdentity={} [CurrentTime is SET]  for topic to send to FPS back office",
            chargeBoxIdentity);
        HeartBeatRequestOcppMessage occpMessage = HeartBeatRequestOcppMessage.builder()
            .chargeBoxId(chargeBoxIdentity)
            .heartbeatRequest(parameters)
            .build();
        log.info(
            "Created HeartbeatRequest:[{}] with chargeBoxIdentity={}  for topic to send to FPS back office",
            occpMessage, chargeBoxIdentity);
        sendMessage(occpMessage);
        log.info(
            "Sent HeartbeatRequest:[{}] with chargeBoxIdentity={}  for topic to send to FPS back office",
            occpMessage, chargeBoxIdentity);
      } catch (Exception e) {
        e.printStackTrace();
        log.error("Exception while creating and sending FPS back office message for HeartbeatRequest",e);
      }
    }
    return heartbeat;
  }

  @Override
  public AuthorizeResponse authorize(AuthorizeRequest parameters, String chargeBoxIdentity) {
    AuthorizeResponse authorizeResponse = service.authorize(parameters, chargeBoxIdentity);
    authorizationListener.process(parameters, chargeBoxIdentity, authorizeResponse);
    return authorizeResponse;
  }

  @Override
  public DataTransferResponse dataTransfer(DataTransferRequest parameters,
      String chargeBoxIdentity) {
    return service.dataTransfer(parameters, chargeBoxIdentity);
  }

  private void sendMessage(Object occpMessage) {
    String message = JsonUtils.toJson(occpMessage);

    log.info("Sending Ocpp Message to FPS back office : {} ", message);
    messageSender.sendMessage(message);
    log.info("Sent Ocpp Message to FPS back office : {} ", message);
  }

  // -------------------------------------------------------------------------
  // No-op
  // -------------------------------------------------------------------------

  @Override
  public Response<StopTransactionResponse> stopTransactionAsync(StopTransactionRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> stopTransactionAsync(StopTransactionRequest parameters, String chargeBoxIdentity,
      AsyncHandler<StopTransactionResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<StatusNotificationResponse> statusNotificationAsync(
      StatusNotificationRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> statusNotificationAsync(StatusNotificationRequest parameters,
      String chargeBoxIdentity,
      AsyncHandler<StatusNotificationResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<AuthorizeResponse> authorizeAsync(AuthorizeRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> authorizeAsync(AuthorizeRequest parameters, String chargeBoxIdentity,
      AsyncHandler<AuthorizeResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<StartTransactionResponse> startTransactionAsync(
      StartTransactionRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> startTransactionAsync(StartTransactionRequest parameters,
      String chargeBoxIdentity,
      AsyncHandler<StartTransactionResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<FirmwareStatusNotificationResponse> firmwareStatusNotificationAsync(
      FirmwareStatusNotificationRequest parameters, String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> firmwareStatusNotificationAsync(FirmwareStatusNotificationRequest parameters,
      String chargeBoxIdentity,
      AsyncHandler<FirmwareStatusNotificationResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<BootNotificationResponse> bootNotificationAsync(
      BootNotificationRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> bootNotificationAsync(BootNotificationRequest parameters,
      String chargeBoxIdentity,
      AsyncHandler<BootNotificationResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<HeartbeatResponse> heartbeatAsync(HeartbeatRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> heartbeatAsync(HeartbeatRequest parameters, String chargeBoxIdentity,
      AsyncHandler<HeartbeatResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<MeterValuesResponse> meterValuesAsync(MeterValuesRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> meterValuesAsync(MeterValuesRequest parameters, String chargeBoxIdentity,
      AsyncHandler<MeterValuesResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<DataTransferResponse> dataTransferAsync(DataTransferRequest parameters,
      String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> dataTransferAsync(DataTransferRequest parameters, String chargeBoxIdentity,
      AsyncHandler<DataTransferResponse> asyncHandler) {
    return null;
  }

  @Override
  public Response<DiagnosticsStatusNotificationResponse> diagnosticsStatusNotificationAsync(
      DiagnosticsStatusNotificationRequest parameters, String chargeBoxIdentity) {
    return null;
  }

  @Override
  public Future<?> diagnosticsStatusNotificationAsync(
      DiagnosticsStatusNotificationRequest parameters,
      String chargeBoxIdentity,
      AsyncHandler<DiagnosticsStatusNotificationResponse> asyncHandler) {
    return null;
  }
}
