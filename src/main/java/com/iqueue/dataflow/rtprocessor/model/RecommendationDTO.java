package com.iqueue.dataflow.rtprocessor.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationDTO {
    private String userId;
    private List<StoreInfo> storeList;
}
