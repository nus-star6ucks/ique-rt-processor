package com.iqueue.dataflow.rtprocessor.config;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iqueue.dataflow.rtprocessor.model.PredictionsDTO;
import com.iqueue.dataflow.rtprocessor.model.RecommendationDTO;
import com.iqueue.dataflow.rtprocessor.model.StoreInfo;
import com.iqueue.dataflow.rtprocessor.model.UserInfo;
import com.iqueue.dataflow.rtprocessor.service.RTClient;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
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
                  inputNode =
                      objectMapper.readTree(
                          "{\n"
                              + "    \"inputs\": {\n"
                              + "        \"user_id\": [\n"
                              + "\""
                              + userInfo.getUserId()
                              + "\""
                              + "        ],\n"
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
                CompletableFuture<PredictionsDTO> geoPredictionFuture =
                    CompletableFuture.supplyAsync(() -> rtClient.geoRecommendationList(inputNode));
                CompletableFuture<PredictionsDTO> alsPredictionFuture =
                    CompletableFuture.supplyAsync(() -> rtClient.alsRecommendationList(inputNode));
                CompletableFuture<RecommendationDTO> combineFutures =
                    geoPredictionFuture.thenCombineAsync(
                        alsPredictionFuture,
                        (r1, r2) -> {
                          double score1 = r1.getPredictions().size(),
                              score2 = r1.getPredictions().size();
                          final double factor = 0.7;
                          Map<String, Double> result = new HashMap<>();
                          for (String k : r1.getPredictions()) {
                            result.put(k, score1 * factor);
                            score1--;
                          }
                          for (String k : r2.getPredictions()) {
                            result.merge(k, score2 * (1 - factor), Double::sum);
                            score2 -=
                                (double) r1.getPredictions().size() / r2.getPredictions().size();
                          }
                          List<StoreInfo> storeInfoList = new ArrayList<>();
                          result.entrySet().stream()
                              .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                              .limit(10)
                              .forEachOrdered(
                                  e -> {
                                    StoreInfo storeInfo = new StoreInfo();
                                    storeInfo.setStoreId(e.getKey());
                                    storeInfoList.add(storeInfo);
                                  });
                          return new RecommendationDTO(userInfo.getUserId(), storeInfoList);
                        });
                try {
                  return combineFutures.get(30, SECONDS);
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                  throw new RuntimeException(e);
                }
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
