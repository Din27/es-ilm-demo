package com.chelyadin.es.ilm.repository;

import com.chelyadin.es.ilm.model.Message;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MessageRepository extends ElasticsearchRepository<Message, String> {
}
