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
import com.fps.charging.adapter.model.MeterValuesRequestOcppMessage;
import com.fps.charging.adapter.model.StartTransactionRequestOcppMessage;
import com.fps.charging.adapter.model.StatusNotificationRequestOcppMessage;
import com.fps.charging.adapter.model.StopTransactionRequestOcppMessage;
import com.fps.charging.listener.AuthorizationListener;
import com.fps.charging.listener.StartTransactionListener;
import com.fps.charging.service.MessageSender;
import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.ocpp.OcppVersion;
import de.rwth.idsg.steve.service.CentralSystemService16_Service;
import java.util.concurrent.Future;
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
      sendMessage(StatusNotificationRequestOcppMessage.builder()
          .chargeBoxId(chargeBoxIdentity)
          .statusNotificationRequest(parameters)
          .build());
    }
    return statusNotificationResponse;
  }

  @Override
  public MeterValuesResponse meterValues(MeterValuesRequest parameters, String chargeBoxIdentity) {
    MeterValuesResponse meterValuesResponse = service.meterValues(parameters, chargeBoxIdentity);

    if (meterValuesResponse != null) {
      sendMessage(MeterValuesRequestOcppMessage.builder()
          .chargeBoxId(chargeBoxIdentity)
          .meterValuesRequest(parameters)
          .build());
    }

    return meterValuesResponse;
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
      sendMessage(StartTransactionRequestOcppMessage.builder()
          .chargeBoxId(chargeBoxIdentity)
          .startTransactionRequest(parameters)
          .transactionId(startTransactionResponse.getTransactionId())
          .build());

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
      sendMessage(StopTransactionRequestOcppMessage.builder()
          .chargeBoxId(chargeBoxIdentity)
          .stopTransactionRequest(parameters)
          .build());
    }
    return stopTransactionResponse;
  }

  @Override
  public HeartbeatResponse heartbeat(HeartbeatRequest parameters, String chargeBoxIdentity) {
    HeartbeatResponse heartbeat = service.heartbeat(parameters, chargeBoxIdentity);
    if (heartbeat.isSetCurrentTime()) {

      sendMessage(HeartBeatRequestOcppMessage.builder()
          .chargeBoxId(chargeBoxIdentity)
          .heartbeatRequest(parameters)
          .build());
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
