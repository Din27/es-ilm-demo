package com.chelyadin.es.ilm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@AllArgsConstructor
@Document(indexName = "messages", createIndex = false)
public class Message {
  @Id
  private String id;
  @Field(type = FieldType.Text)
  private String message;
  @Field(name = "@timestamp", type = FieldType.Date, format = DateFormat.epoch_millis)
  private Date timestamp; // TODO add or write localdatetime converter
}
