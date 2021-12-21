package com.fps.charging.security;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
/**
 * This class is used to get the secret parameter values from Azure Key Vault
 *
 *
 *
 * @author Mehmet Dongel <mehmet.dongel@gmail.com>
 * @since 05.11.2021
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AzureKeyVaultAdapter {

  private static final String ADMIN_PASSWORD = "admin-password";
  private static final String TOPIC_CONNECTION_STRING = "topic-connection-string";
  private static final String TOPIC_NAME = "topic-name";
  private static final String TOPIC_SUBSCRIPTION_NAME = "topic-subscription-name";
  private static final String QUEUE_CONNECTION_STRING = "queue-connection-string";
  private static final String QUEUE_NAME = "queue-name";
  private boolean initialized = false;

  private SecretClient secretClient;

  public String getAdminPassword() {
    return getValue(ADMIN_PASSWORD);
  }

  public String getTopicConnectionString() {
    return getValue(TOPIC_CONNECTION_STRING);
  }

  public String getTopicName() {
    return getValue(TOPIC_NAME);
  }

  public String getTopicSubscriptionName() {
    return getValue(TOPIC_SUBSCRIPTION_NAME);
  }

  public String getQueueConnectionString() {
    return getValue(QUEUE_CONNECTION_STRING);
  }

  public String getQueueName() {
    return getValue(QUEUE_NAME);
  }

  private String getValue(String queueName) {
    while (!initialized) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return secretClient.getSecret(queueName).getValue();
  }

  @EventListener(ContextRefreshedEvent.class)
  @Order(1)
  private void start() throws InterruptedException, ApplicationContextException {
    try {
      secretClient = new SecretClientBuilder()
          .vaultUrl("https://steve-flexpowerltdt.vault.azure.net")
          .credential(new DefaultAzureCredentialBuilder().build())
          .buildClient();

      KeyVaultSecret secret = secretClient.setSecret("test-key", "test-value");
      System.out.printf("Secret created with name \"%s\" and value \"%s\"%n", secret.getName(),
          secret.getValue());
      initialized = true;
    } catch (Exception e) {
      log.error("Unable to connect to Azure Key Vault: Stopping the server.", e);
      throw new ApplicationContextException(
          "Unable to connect to Azure Key Vault: Stopping the server", e);
    }
  }


}
