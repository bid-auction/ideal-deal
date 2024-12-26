package com.auction.bid.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class MainResponse {
    private List<HotResponse> hotPage;
    private Page<PhaseCriteriaResponse> beforePhase;
    private Page<PhaseCriteriaResponse> ongoingPhase;
    private Page<PhaseCriteriaResponse> endPhase;
}
