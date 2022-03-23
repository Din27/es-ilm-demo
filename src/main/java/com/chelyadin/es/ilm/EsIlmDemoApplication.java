package com.chelyadin.es.ilm;

import com.chelyadin.es.ilm.config.properties.EsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EsProperties.class)
public class EsIlmDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(EsIlmDemoApplication.class, args);
  }

}
