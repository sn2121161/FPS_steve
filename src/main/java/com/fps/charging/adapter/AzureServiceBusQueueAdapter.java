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
import com.fps.charging.service.ChargingProfileService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class AzureServiceBusQueueAdapter {

  static String queueConnectionString = "Endpoint=sb://steve-to-fps.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=Ig2l2323OvjTtL2Upf9sENuWyrwGEkeVxMHluJSt/MI=";
  static String queueName = "schedule-matcher";

  private ServiceBusSenderClient senderClient;
  private ServiceBusProcessorClient processorClient;
  private ServiceBusProcessorClient queueProcessorClient;

  private final ChargingProfileService chargingProfileService;

  public AzureServiceBusQueueAdapter( ChargingProfileService chargingProfileService) {
    this.chargingProfileService = chargingProfileService;
  }

  @EventListener(ContextRefreshedEvent.class)
  public void start() throws InterruptedException {
    System.out.println("Starting azure Integration");
    createQueueListener();

  }

  @EventListener(ContextStoppedEvent.class)
  public void stop() throws InterruptedException {
    System.out.println("Stopping and closing the processor");
    processorClient.close();
    queueProcessorClient.close();
  }

  // handles received messages
  public void createQueueListener() throws InterruptedException {
    CountDownLatch countdownLatch = new CountDownLatch(1);

    // Create an instance of the processor through the ServiceBusClientBuilder
    queueProcessorClient = new ServiceBusClientBuilder()
        .connectionString(queueConnectionString)
        .processor()
        .queueName(queueName)
        .processMessage(this::processMessage)
        .processError(context -> processError(context, countdownLatch))
        .buildProcessorClient();

    System.out.println("Starting the processor");
    queueProcessorClient.start();



  }

  private void processMessage(ServiceBusReceivedMessageContext context) {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
        message.getMessageId(),
        message.getSequenceNumber(), message.getBody());

    ChargingProfileRequest chargingProfileRequest = JsonUtils.toObject(message.getBody().toString(),
        ChargingProfileRequest.class);

    chargingProfileService.processChargingProfileMessage(chargingProfileRequest);



  }

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