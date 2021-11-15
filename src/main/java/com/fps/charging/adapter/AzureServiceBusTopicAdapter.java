package com.fps.charging.adapter;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fps.charging.adapter.model.ChargingProfileRequest;
import de.rwth.idsg.steve.repository.ChargingProfileRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class AzureServiceBusTopicAdapter {

  static String topicConnectionString = "Endpoint=sb://steve-to-fps.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=Ig2l2323OvjTtL2Upf9sENuWyrwGEkeVxMHluJSt/MI=";
  static String topicName = "steve-to-fps-topic";
  static String topicSubscriptionName = "steve-to-fps-topic-subscription";

  private ServiceBusSenderClient senderClient;
  private ServiceBusProcessorClient processorClient;
  private ServiceBusProcessorClient queueProcessorClient;



  @EventListener(ContextRefreshedEvent.class)
  public void start() throws InterruptedException {
    System.out.println("Starting azure Integration");
//    createSender();
//    createTopicListener();
  }

  @EventListener(ContextStoppedEvent.class)
  public void stop() throws InterruptedException {
    System.out.println("Stopping and closing the processor");
    processorClient.close();
    queueProcessorClient.close();
  }

  private void createSender() {
    // create the Service Bus Sender client for the queue
    senderClient = new ServiceBusClientBuilder()
        .connectionString(topicConnectionString)
        .sender()
        .topicName(topicName)
        .buildClient();
  }

  public void sendMessage(String message) {
    // send one message to the topic
    senderClient.sendMessage(new ServiceBusMessage(message));
    System.out.println("Sent a single message to the topic: " + topicName);
  }

  // handles received messages
  public void createTopicListener() throws InterruptedException {
    CountDownLatch countdownLatch = new CountDownLatch(1);

    // Create an instance of the processor through the ServiceBusClientBuilder
    processorClient = new ServiceBusClientBuilder()
        .connectionString(topicConnectionString)
        .processor()
        .topicName(topicName)
        .subscriptionName(topicSubscriptionName)
        .processMessage(this::processMessage)
        .processError(context -> processError(context, countdownLatch))
        .buildProcessorClient();

    System.out.println("Starting the processor");
    processorClient.start();

  }

  private void processMessage(ServiceBusReceivedMessageContext context) {
    ServiceBusReceivedMessage message = context.getMessage();
    System.out.printf("Processing message. Session: %s, Sequence #: %s. Contents: %s%n",
        message.getMessageId(),
        message.getSequenceNumber(), message.getBody());

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
