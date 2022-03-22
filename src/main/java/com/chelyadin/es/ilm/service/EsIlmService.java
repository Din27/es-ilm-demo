package com.chelyadin.es.ilm.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class EsIlmService {

  private static final String URL_ILM_POLICY = "/_ilm/policy/messages_policy";
  private static final String URL_INDEX_TEMPLATE = "/_index_template/messages_template";
  private static final String URL_DOCUMENT = "/messages/_doc";

  private final RestHighLevelClient highLevelClient;

  private static final String PATH_RESOURCES = "src/main/resources/";

  @Autowired
  public EsIlmService(final RestHighLevelClient highLevelClient) {
    this.highLevelClient = highLevelClient;
  }

  @PostConstruct
  @SneakyThrows
  public void startIlm() {
    // create/update the ILM policy
    sendRequest(HttpMethod.PUT.toString(), URL_ILM_POLICY, getIlmPolicy());

    // create/update the index template which will be used on each index creation during each rollover
    sendRequest(HttpMethod.PUT.toString(), URL_INDEX_TEMPLATE, getIndexTemplate());

    // add a first document to create the first index and start the ILM process
    sendRequest(HttpMethod.POST.toString(), URL_DOCUMENT, getDocument());
  }

  public void sendRequest(String method, String url, String body) throws IOException {
    Request request = new Request(method, url);
    if (body != null) {
      request.setJsonEntity(body);
    }
    logResponse(highLevelClient.getLowLevelClient().performRequest(request));
  }

  public String getIlmPolicy() throws IOException {
    return readFile("ilm_policy.json");
  }

  public  String getIndexTemplate() throws IOException {
    return readFile("index_template.json");
  }

  public String getDocument() throws IOException {
    return readFile("message.json");
  }

  public String readFile(String fileName) throws IOException {
    Path path = Paths.get(PATH_RESOURCES + fileName);
    return Files.readString(path);
  }

  private void logResponse(final Response response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    String responseBody = EntityUtils.toString(response.getEntity());

    log.debug(String.format("%s %s: %d:", response.getRequestLine().getMethod(),
        response.getRequestLine().getUri(), statusCode));
    log.debug(responseBody);
  }
}
