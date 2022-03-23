package com.chelyadin.es.ilm.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "es")
public class EsProperties {

  private String host;
  private String port;
  private String username;
  private String password;
  private final Ilm ilm = new Ilm();

  @Data
  public static class Ilm {
    private String hotDuration;
    private String warmDuration;
  }
}