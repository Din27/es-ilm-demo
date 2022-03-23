package com.chelyadin.es.ilm.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Document(indexName = "messages", createIndex = false)
public class Message {
  @Id
  private String id;
  @Field(type = FieldType.Text)
  private String message;
  @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
  private LocalDateTime timestamp = LocalDateTime.now();
}
