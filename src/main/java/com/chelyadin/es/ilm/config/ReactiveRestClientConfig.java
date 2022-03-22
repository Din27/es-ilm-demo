package com.chelyadin.es.ilm.config;

import lombok.SneakyThrows;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import javax.net.ssl.SSLContext;

@Configuration
public class ReactiveRestClientConfig extends AbstractElasticsearchConfiguration {

  // TODO move to properties
  private static final String ES_HOST = "localhost";
  private static final int ES_PORT = 9200;
  private static final String ES_USERNAME = "elastic";
  private static final String ES_PASSWORD = "WpEpXcISkYJ_6*cDyyN+";

  @SneakyThrows
  @Override
  @Bean
  public RestHighLevelClient elasticsearchClient() {
    SSLContextBuilder sslBuilder = SSLContexts.custom()
        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE);
    final SSLContext sslContext = sslBuilder.build();

    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(ES_HOST + ":" + ES_PORT)
        .usingSsl(sslContext)
        .withBasicAuth(ES_USERNAME, ES_PASSWORD) // put your credentials
        .build();
    return RestClients.create(clientConfiguration).rest();
  }

}