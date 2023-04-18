package com.iqueue.dataflow.rtprocessor.config;

import com.iqueue.dataflow.rtprocessor.model.StoreInfo;
import com.iqueue.dataflow.rtprocessor.model.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Configuration
public class RTProcessorConfiguration {

    @Bean
    public Function<UserInfo, List<StoreInfo>> recommendation() {
        return userInfo -> {
            log.info("user information: " + userInfo.getUserId());
            List<StoreInfo> storeInfoList= new ArrayList<>();
            StoreInfo storeInfo = new StoreInfo();
            storeInfo.setStoreId(1L);
            storeInfo.setLongitude(103.85);
            storeInfo.setLatitude(1.3);
            storeInfoList.add(storeInfo);
            // Todo: Remote calling the data model.

            return storeInfoList;
        };
    }
}
