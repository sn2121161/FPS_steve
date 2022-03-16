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
package com.fps.charging.adapter;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fps.charging.JsonUtils;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import com.fps.charging.security.AzureKeyVaultAdapter;
import com.fps.charging.service.ChargingProfileService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * This class is used to connect Azure Service Queue to consume and process new charging profile messages created vy FPS adapter
 *
 * @author Mehmet Dongel <mehmet.dongel@gmail.com>
 * @since 05.11.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AzureServiceBusQueueAdapter {

  // Used to get the queue connection information from Azure Key Vault
  private final AzureKeyVaultAdapter azureKeyVaultAdapter;

  // Used to save the new charging profile coming from FPS and to apply this profile to the charging points
  private final ChargingProfileService chargingProfileService;

  // Used to connect Azure Service Queue to consume and process new charging profile messages created vy FPS adapter
  private ServiceBusProcessorClient queueProcessorClient;


  // This method is called by spring framework whle the application is starting up. This method performs
  // connection to Azure Service Queue
  @EventListener(ContextRefreshedEvent.class)
  @Order(2)
  public void start() throws InterruptedException {
    System.out.println("Starting azure Integration Queue Adapter");
    createQueueListener();
  }

  @EventListener(ContextStoppedEvent.class)
  public void stop() throws InterruptedException {
    System.out.println("Stopping and closing the processor");
    queueProcessorClient.close();
  }

  // handles received messages
  public void createQueueListener() throws InterruptedException {
    CountDownLatch countdownLatch = new CountDownLatch(1);

    // Create an instance of the processor through the ServiceBusClientBuilder
    queueProcessorClient = new ServiceBusClientBuilder()
        .connectionString(azureKeyVaultAdapter.getQueueConnectionString())
        .processor()
        .queueName(azureKeyVaultAdapter.getQueueName())
        .processMessage(this::processMessage)
//         .processError(context -> processError(context, countdownLatch))
        .buildProcessorClient();

    System.out.println("Starting the processor -> Queue Adapter");
    queueProcessorClient.start();
    System.out.println("Started the processor -> Queue Adapter");
  }

  // This method consumes the new charging profile message from Azure Service Queue
  private void processMessage(ServiceBusReceivedMessageContext context) {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
        message.getMessageId(),
        message.getSequenceNumber(), message.getBody());

    log.info("Processing message. Session: {}, Sequence #: {}. Contents: {}%n",
        message.getMessageId(), message.getSequenceNumber(), message.getBody());

    ChargingProfileRequest chargingProfileRequest = JsonUtils.toObject(message.getBody().toString(),
        ChargingProfileRequest.class);

    try {
      chargingProfileService.processChargingProfileMessage(chargingProfileRequest);
    } catch (Exception e) {
      log.error("Unable to process new charging profile coming from FPS. Message:{}",
          message.getBody(), e);
    }

  }

  // This method consumes the error message which is thrown at processMessage method.
  private void processError(ServiceBusErrorContext context, CountDownLatch countdownLatch) {
    System.out.printf("Error when receiving messages from namespace: '%s'. Entity: '%s'%n",
        context.getFullyQualifiedNamespace(), context.getEntityPath());

    if (!(context.getException() instanceof ServiceBusException)) {
      System.out.printf("Non-ServiceBusException occurred: %s%n", context.getException());
      return;
    }

    ServiceBusException exception = (ServiceBusException) context.getException();
    ServiceBusFailureReason reason = exception.getReason();

    if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
        || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
        || reason == ServiceBusFailureReason.UNAUTHORIZED) {
      System.out.printf("An unrecoverable error occurred. Stopping processing with reason %s: %s%n",
          reason, exception.getMessage());

      countdownLatch.countDown();
    } else if (reason == ServiceBusFailureReason.MESSAGE_LOCK_LOST) {
      System.out.printf("Message lock lost for message: %s%n", context.getException());
    } else if (reason == ServiceBusFailureReason.SERVICE_BUSY) {
      try {
        // Choosing an arbitrary amount of time to wait until trying again.
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        System.err.println("Unable to sleep for period of time");
      }
    } else {
      System.out.printf("Error source %s, reason %s, message: %s%n", context.getErrorSource(),
          reason, context.getException());
    }
  }


}
