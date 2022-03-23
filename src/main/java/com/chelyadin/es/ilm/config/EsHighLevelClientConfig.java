package com.chelyadin.es.ilm.config;

import com.chelyadin.es.ilm.config.properties.EsProperties;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class EsHighLevelClientConfig extends AbstractElasticsearchConfiguration {

  private final EsProperties esProperties;

  @SneakyThrows
  @Override
  @Bean
  public RestHighLevelClient elasticsearchClient() {
    SSLContextBuilder sslBuilder = SSLContexts.custom()
        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE);
    final SSLContext sslContext = sslBuilder.build();

    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(esProperties.getHost() + ":" + esProperties.getPort())
        .usingSsl(sslContext)
        .withBasicAuth(esProperties.getUsername(), esProperties.getPassword()) // put your credentials
        .build();
    return RestClients.create(clientConfiguration).rest();
  }

}