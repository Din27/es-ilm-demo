package com.chelyadin.es.ilm;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@SpringBootApplication
public class EsIlmDemoApplication {

  private static final String URL_ILM_POLICY = "/_ilm/policy/messages_policy";
  private static final String URL_INDEX_TEMPLATE = "/_index_template/messages_template";
  private static final String URL_DOCUMENT = "/messages/_doc";

  // TODO move to properties
  private static final String ES_HOST = "localhost";
  private static final int ES_PORT = 9200;
  private static final String ES_PROTOCOL = "https";
  private static final String ES_USERNAME = "elastic";
  private static final String ES_PASSWORD = "_CHANGE_ME_";

  private static final String PATH_RESOURCES = "src/main/resources/";

  public static void main(String[] args) {
    SpringApplication.run(EsIlmDemoApplication.class, args);

    try (RestClient client = buildEsHttpsClient()) {
      // create ILM policy
      sendRequest(HttpMethod.PUT.toString(), URL_ILM_POLICY, getIlmPolicy(), client);

      // create index template which will be used on each index creation during each rollover
      sendRequest(HttpMethod.PUT.toString(), URL_INDEX_TEMPLATE, getIndexTemplate(), client);

      // add a first document to create the index and start the process
      sendRequest(HttpMethod.POST.toString(), URL_DOCUMENT, getDocument(), client);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static RestClient buildEsHttpsClient() throws NoSuchAlgorithmException, KeyStoreException,
      KeyManagementException {
    SSLContextBuilder sslBuilder = SSLContexts.custom()
        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE);
    final SSLContext sslContext = sslBuilder.build();

    RestClientBuilder builder = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT, ES_PROTOCOL))
        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
            .setDefaultCredentialsProvider(buildCredentialsProvider())
            .setSSLContext(sslContext));
    return builder.build();
  }

  private static CredentialsProvider buildCredentialsProvider() {
    final CredentialsProvider credentialsProvider =
        new BasicCredentialsProvider();
    credentialsProvider.setCredentials(AuthScope.ANY,
        new UsernamePasswordCredentials(ES_USERNAME, ES_PASSWORD));
    return credentialsProvider;
  }

  public static void sendRequest(String method, String url, String body, RestClient client) throws IOException {
    Request request = new Request(method, url);
    if (body != null) {
      request.setJsonEntity(body);
    }
    logResponse(client.performRequest(request));
  }

  public static String getIlmPolicy() throws IOException {
    return readFile("ilm_policy.json");
  }

  public static String getIndexTemplate() throws IOException {
    return readFile("index_template.json");
  }

  public static String getDocument() throws IOException {
    return readFile("message.json");
  }

  public static String readFile(String fileName) throws IOException {
    Path path = Paths.get(PATH_RESOURCES + fileName);
    return Files.readString(path);
  }

  private static void logResponse(final Response response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    String responseBody = EntityUtils.toString(response.getEntity());

    log.debug(String.format("%s %s: %d:", response.getRequestLine().getMethod(),
        response.getRequestLine().getUri(), statusCode));
    log.debug(responseBody);
  }
}
