package com.auction.bid.domain.product;


import com.auction.bid.domain.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductService {
   ProductDto.Response register(List<MultipartFile> images, ProductDto.Request request, String token);

   ProductDto.Response update(Long id, List<MultipartFile> images, ProductDto.Request request, String token);

   void delete(Long id);

   ProductGetDto.Response getProduct(Long productId);

    Product findById(Long productId);

    boolean isOnGoing(Long productId);

    Map<String, List<RankingResponse>> getRankings();

    Page<RankingResponse> getRankingsHighest(int page, int size);

    Page<RankingResponse> getRankingsLowest(int page, int size);

    List<HotResponse> getHotResponse(int start, int end);

    List<HotResponse> getHotPage(int page, int size);

    MainResponse getMainPage();

    Page<PhaseCriteriaResponse> getBidBefore(int page, int size);

    Page<PhaseCriteriaResponse> getBidOngoing(int page, int size);

    Page<PhaseCriteriaResponse> getBidEnded(int page, int size);


}
