package com.fps.charging.service;

import com.fps.charging.adapter.AzureServiceBusTopicAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AzureMessageSender implements MessageSender {

  private final AzureServiceBusTopicAdapter azureServiceBusTopicAdapter;

  @Override
  public void sendMessage(String message) {
    azureServiceBusTopicAdapter.sendMessage(message);

  }
}
