package com.auction.bid.domain.product;

import com.auction.bid.domain.bid.BidDto;
import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.category.CategoryRepository;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.photo.PhotoRepository;
import com.auction.bid.domain.product.dto.*;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.CategoryException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.PhotoException;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.querydsl.QueryDslRepository;
import com.auction.bid.global.scheduler.AuctionScheduler;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.auction.bid.global.scheduler.ConstAuction.AUCTION;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PhotoRepository photoRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final TaskScheduler taskScheduler;
    private final AuctionScheduler auctionScheduler;
    private final QueryDslRepository queryDslRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

    @Override
    public ProductDto.Response register(List<MultipartFile> images, ProductDto.Request request, String token) {
        if (request.getAuctionStart().isBefore(LocalDateTime.now())) {
            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }

        if (request.getAuctionEnd().isBefore(request.getAuctionStart())) {
            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        UUID memberId = jwtUtil.getMemberUUIDFromToken(token);

        Member findMember = memberRepository.findByMemberUUID(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));

        Category findCategory = categoryRepository.findByCategoryName(request.getCategory())
                .orElseThrow(() -> new CategoryException(ErrorCode.NOT_FOUND_CATEGORY));

        Product product = ProductDto.Request.toEntity(request, findMember, findCategory);

        uploadPhoto(product, images);
        Product savedProduct = productRepository.save(product);

        scheduleAuction(product);
        return ProductDto.Response.fromEntity(savedProduct);
    }

    private void uploadPhoto(Product product, List<MultipartFile> images){

        try {
            String uploadsDir = "src/main/resources/static/uploads/photos/";

            for (MultipartFile image : images){
                String dbFilePath = saveImage(image, uploadsDir);

                Photo photo = new Photo(dbFilePath, product);
                product.addPhoto(photo);
            }
        } catch (IOException e) {
            throw new PhotoException(ErrorCode.PHOTO_UPLOAD_FAILED);
        }

    }

    private String saveImage(MultipartFile image, String uploadsDir) throws IOException{
        String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
        String filePath = uploadsDir + fileName;
        String dbFilePath = "/uploads/photos/" + fileName;

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        Files.write(path, image.getBytes());

        return dbFilePath;
    }

    private void scheduleAuction(Product product) {
        Instant startDate = product.getAuctionStart().atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.openAuction(product), startDate);

        Instant endDate = product.getAuctionEnd().atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.closeAuction(product), endDate);
    }

    @Override
    public boolean isOnGoing(Long productId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        return findProduct.getProductBidPhase().equals(ProductBidPhase.ONGOING);
    }

    @Override
    public Map<String, List<RankingResponse>> getRankings() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfWeek = now.with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = now.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);
        Pageable pageable = PageRequest.of(0, 10);

        Map<String, List<Sale>> rankingForThisWeek =
                queryDslRepository.findTop10SaleThisWeek(
                        startOfWeek,
                        endOfWeek,
                        pageable
                );

        return RankingResponse.fromSaleListToMap(rankingForThisWeek);
    }

    @Override
    public Page<PhaseCriteriaResponse> getBidBefore(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.BEFORE);
    }

    @Override
    public Page<PhaseCriteriaResponse> getBidOngoing(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.ONGOING);
    }

    @Override
    public Page<PhaseCriteriaResponse> getBidEnded(int page, int size) {
        return getPhaseResponse(page, size, ProductBidPhase.ENDED);
    }

    @Override
    public ProductDto.Response update(Long productId, List<MultipartFile> images, ProductDto.Request request, String token) {

        if (productRepository.existsByTitle(request.getTitle())){
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT);
        }

        if(request.getAuctionStart().isBefore(LocalDateTime.now())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }
        if(request.getAuctionEnd().isBefore(request.getAuctionStart())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        UUID memberId = jwtUtil.getMemberUUIDFromToken(token);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        Product productUpdate = Product.builder()
                .id(product.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .startBid(request.getStartBid())
                .auctionStart(request.getAuctionStart())
                .auctionEnd(request.getAuctionEnd())
                .member((memberRepository.findByMemberUUID(memberId).orElseThrow(
                        () -> new MemberException(ErrorCode.NOT_EXIST_MEMBER))))
                .category(categoryRepository.findByCategoryName(request.getCategory()).orElseThrow(
                        () -> new CategoryException(ErrorCode.NOT_FOUND_CATEGORY)))
                .build();

        Product savedProduct = productRepository.save(productUpdate);
        uploadPhoto(productUpdate, images);
        return ProductDto.Response.fromEntity(savedProduct);
    }

    @Override
    public List<HotResponse> getHotPage(int page, int size) {
        int start = (page - 1) * size;
        int end = page * size;
        return getHotResponse(start, end);
    }

    @Override
    public Page<RankingResponse> getRankingsHighest(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> highestPage = queryDslRepository.getHighestSaleList(pageable);
        return RankingResponse.fromSalePageToResPage(highestPage);
    }

    @Override
    public Page<RankingResponse> getRankingsLowest(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sale> lowestSaleList = queryDslRepository.getLowestSaleList(pageable);
        return RankingResponse.fromSalePageToResPage(lowestSaleList);
    }

    @Override
    public List<HotResponse> getHotResponse(int start, int end) {
        HashOperations<String, Long, List<BidDto>> redisHash = redisTemplate.opsForHash();
        Map<Long, List<BidDto>> hashEntries = redisHash.entries(AUCTION);
        List<Map.Entry<Long, List<BidDto>>> sortedHashEntries = hashEntries.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .toList();

        int listSize = sortedHashEntries.size();
        if (start >= listSize) {
            return List.of();
        }

        List<Map.Entry<Long, List<BidDto>>> selectedEntries = sortedHashEntries.subList(start, Math.min(end, listSize));

        List<Long> productIds = selectedEntries.stream()
                .map(Map.Entry::getKey)
                .toList();

        List<Product> productList = queryDslRepository.findAllByProductIds(productIds);

        Map<Long, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        List<Product> sortedProductList = productIds.stream()
                .map(productMap::get)
                .filter(Objects::nonNull)
                .toList();

        return sortedProductList.stream()
                .map(HotResponse::fromProductEntity)
                .toList();
    }

    @Override
    public MainResponse getMainPage() {
        List<HotResponse> hotPage = getHotPage(1, 10);
        Page<PhaseCriteriaResponse> beforePhase = getPhaseResponse(0, 10, ProductBidPhase.BEFORE);
        Page<PhaseCriteriaResponse> ongoingPhase = getPhaseResponse(0, 10, ProductBidPhase.ONGOING);
        Page<PhaseCriteriaResponse> endPhase = getPhaseResponse(0, 10, ProductBidPhase.ENDED);
        return new MainResponse(hotPage, beforePhase, ongoingPhase, endPhase);
    }

    private Page<PhaseCriteriaResponse> getPhaseResponse(
            int page,
            int size,
            ProductBidPhase phase
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = queryDslRepository.getProductByPhase(phase, pageable);

        List<PhaseCriteriaResponse> resList = productPage.stream()
                .map(PhaseCriteriaResponse::fromProductEntity)
                .toList();

        return PageableExecutionUtils.getPage(resList, pageable, productPage::getTotalElements);
    }

    @Override
    public void delete(Long productId){
        productRepository.deleteById(productId);
    }

    @Override
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
    }

    @Override
    public ProductGetDto.Response getProduct(Long productId){
        List<Photo> photos = photoRepository.findByProductId(productId);
        if (photos.isEmpty()){
            throw new PhotoException(ErrorCode.PHOTO_NOT_FOUND);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        return ProductGetDto.Response.fromEntity(product, photos);
    }

}
