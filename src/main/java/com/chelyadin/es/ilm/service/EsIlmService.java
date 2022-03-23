package com.chelyadin.es.ilm.service;

import com.chelyadin.es.ilm.config.properties.EsProperties;
import com.chelyadin.es.ilm.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indexlifecycle.DeleteAction;
import org.elasticsearch.client.indexlifecycle.LifecycleAction;
import org.elasticsearch.client.indexlifecycle.LifecyclePolicy;
import org.elasticsearch.client.indexlifecycle.Phase;
import org.elasticsearch.client.indexlifecycle.PutLifecyclePolicyRequest;
import org.elasticsearch.client.indexlifecycle.ReadOnlyAction;
import org.elasticsearch.client.indexlifecycle.RolloverAction;
import org.elasticsearch.client.indexlifecycle.SetPriorityAction;
import org.elasticsearch.core.TimeValue;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EsIlmService {

  private static final String DATA_STREAM_NAME = "messages";
  private static final String ILM_POLICY_NAME = DATA_STREAM_NAME + "_policy";
  private static final String INDEX_TEMPLATE_NAME = DATA_STREAM_NAME + "_template";

  private static final String URL_ILM_POLICY = "/_ilm/policy/" + ILM_POLICY_NAME;
  private static final String URL_INDEX_TEMPLATE = "/_index_template/" + INDEX_TEMPLATE_NAME;
  private static final String URL_DOCUMENT = String.format("/%s/_doc", DATA_STREAM_NAME);

  private static final String PHASE_NAME_HOT = "hot";
  private static final String PHASE_NAME_WARM = "warm";
  private static final String PHASE_NAME_DELETE = "delete";
  private static final int PHASE_PRIORITY_HOT = 100;
  private static final int PHASE_PRIORITY_WARM = 50;

  private static final String PATH_RESOURCES = "src/main/resources/es/";
  private static final String FILE_NAME_ILM_POLICY = "ilm_policy.json";
  private static final String FILE_NAME_INDEX_TEMPLATE = "index_template.json";
  private static final String FILE_NAME_DOCUMENT = "message.json";

  private final RestHighLevelClient highLevelClient;
  private final EsProperties esProperties;
  private final MessageRepository messageRepository;

  @PostConstruct
  @SneakyThrows
  public void startIlm() {
    saveIlmPolicy();
    createIndexTemplate();
    saveTestDocument(); // TODO
  }

  private void saveIlmPolicy() throws IOException {
    // create/update the ILM policy
//    sendRequest(HttpMethod.PUT.toString(), URL_ILM_POLICY, getIlmPolicy());

    Map<String, Phase> phases = new HashMap<>();

    Map<String, LifecycleAction> hotActions = new HashMap<>();
    TimeValue hotMaxAge = TimeValue.parseTimeValue(esProperties.getIlm().getHotDuration(), PHASE_NAME_HOT + " phase max_age");
    hotActions.put(RolloverAction.NAME, new RolloverAction(
        null, null, hotMaxAge, null));
    hotActions.put(SetPriorityAction.NAME, new SetPriorityAction(PHASE_PRIORITY_HOT));
    phases.put(PHASE_NAME_HOT, new Phase(PHASE_NAME_HOT, TimeValue.ZERO, hotActions));

    Map<String, LifecycleAction> warmActions = new HashMap<>();
    warmActions.put(ReadOnlyAction.NAME, new ReadOnlyAction());
    warmActions.put(SetPriorityAction.NAME, new SetPriorityAction(PHASE_PRIORITY_WARM));
    phases.put(PHASE_NAME_WARM, new Phase(PHASE_NAME_WARM, TimeValue.ZERO, warmActions));

    Map<String, LifecycleAction> deleteActions =
        Collections.singletonMap(DeleteAction.NAME, new DeleteAction());
    phases.put(PHASE_NAME_DELETE, new Phase(PHASE_NAME_DELETE,
        TimeValue.parseTimeValue(esProperties.getIlm().getWarmDuration(), PHASE_NAME_DELETE + "phase min_age"), deleteActions));

    LifecyclePolicy policy = new LifecyclePolicy(ILM_POLICY_NAME, phases);
    PutLifecyclePolicyRequest putIlmPolicyRequest = new PutLifecyclePolicyRequest(policy);

    highLevelClient.indexLifecycle().putLifecyclePolicy(putIlmPolicyRequest, RequestOptions.DEFAULT);
  }

  private void createIndexTemplate() throws IOException {
    // create/update the index template which will be used on each index creation during each rollover
    sendRequestWithLowLevelClient(HttpMethod.PUT.toString(), URL_INDEX_TEMPLATE, getIndexTemplate());

//    PutIndexTemplateRequest putIndexTemplateRequest = new PutIndexTemplateRequest("my-template")
//        .patterns(List.of(DATA_STREAM_NAME))
//        .settings(Settings.builder()
//            .put("index.number_of_shards", 3)
//            .put("index.number_of_replicas", 1)
//            .put("index.lifecycle.name", POLICY_NAME));
  }

  private void saveTestDocument() throws IOException {
    // add a first document to create the first index and start the ILM process
    sendRequestWithLowLevelClient(HttpMethod.POST.toString(), URL_DOCUMENT, getDocument());

//    messageRepository.save(new Message("id", "message-text", LocalDateTime.now()));
  }

  private void sendRequestWithLowLevelClient(String method, String url, String body) throws IOException {
    Request request = new Request(method, url);
    if (body != null) {
      request.setJsonEntity(body);
    }
    logResponse(highLevelClient.getLowLevelClient().performRequest(request));
  }

  private String getIlmPolicy() throws IOException {
    return readEsFile(FILE_NAME_ILM_POLICY);
  }

  private  String getIndexTemplate() throws IOException {
    return readEsFile(FILE_NAME_INDEX_TEMPLATE);
  }

  private String getDocument() throws IOException {
    return readEsFile(FILE_NAME_DOCUMENT);
  }

  private String readEsFile(String fileName) throws IOException {
    Path path = Paths.get(PATH_RESOURCES + fileName);
    return Files.readString(path);
  }

  // TODO
  private void logResponse(final Response response) throws IOException {
    int statusCode = response.getStatusLine().getStatusCode();
    String responseBody = EntityUtils.toString(response.getEntity());

    log.debug(String.format("%s %s: %d:", response.getRequestLine().getMethod(),
        response.getRequestLine().getUri(), statusCode));
    log.debug(responseBody);
  }
}
