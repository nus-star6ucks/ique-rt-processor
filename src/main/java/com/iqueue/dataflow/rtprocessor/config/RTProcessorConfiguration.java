package com.iqueue.dataflow.rtprocessor.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqueue.dataflow.rtprocessor.model.PredictionsDTO;
import com.iqueue.dataflow.rtprocessor.model.RecommendationDTO;
import com.iqueue.dataflow.rtprocessor.model.StoreInfo;
import com.iqueue.dataflow.rtprocessor.model.UserInfo;
import com.iqueue.dataflow.rtprocessor.service.RTClient;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerde;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class RTProcessorConfiguration {

    private final RTClient rtClient;
    private final ObjectMapper objectMapper;
    @Bean
    public Consumer<KStream<String, UserInfo>> recommendation() {

    return userInfoKStream -> {
      userInfoKStream
          .mapValues(
              userInfo -> {
                  log.info("user information: " + userInfo.getUserId());
                  JsonNode inputNode;
                  try {
                      inputNode = objectMapper.readTree(
                              "{\n"
                                      + "    \"inputs\": {\n"
                                      + "        \"latitude\": [\n"
                                      + userInfo.getLatitude()
                                      + "        ],\n"
                                      + "        \"longitude\": [\n"
                                      + userInfo.getLongitude()
                                      + "        ]\n"
                                      + "    }\n"
                                      + "}");
                  } catch (JsonProcessingException e) {
                      throw new RuntimeException(e);
                  }
                  PredictionsDTO predictionsDTO = rtClient.recommendationList(inputNode);
                  List<StoreInfo> storeInfoList = new ArrayList<>();
                  predictionsDTO.getPredictions().forEach(e -> {
                    StoreInfo storeInfo = new StoreInfo();
                    storeInfo.setStoreId(e.get("business_id").toString());
                    storeInfoList.add(storeInfo);
                });
                  return new RecommendationDTO(userInfo.getUserId(), storeInfoList);
              })
          .groupByKey(Grouped.valueSerde(new JsonSerde<>(RecommendationDTO.class)))
          .reduce(
              (v1, v2) -> v2,
              Materialized.<String, RecommendationDTO, KeyValueStore<Bytes, byte[]>>as(
                      "recommendation")
                  .withKeySerde(Serdes.String())
                  .withValueSerde(new JsonSerde<>(RecommendationDTO.class)));
    };
    }
}
