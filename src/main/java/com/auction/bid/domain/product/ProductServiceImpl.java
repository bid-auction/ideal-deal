package com.auction.bid.domain.product;

import com.auction.bid.domain.category.Category;
import com.auction.bid.domain.category.CategoryRepository;
import com.auction.bid.domain.member.Member;
import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.photo.PhotoRepository;
import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.CategoryException;
import com.auction.bid.global.exception.exceptions.MemberException;
import com.auction.bid.global.exception.exceptions.PhotoException;
import com.auction.bid.global.exception.exceptions.ProductException;
import com.auction.bid.global.scheduler.AuctionScheduler;
import com.auction.bid.global.security.jwt.JWTUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        Product savedProduct = productRepository.save(product);
        uploadPhoto(product, images);

        Instant startDate = product.getAuctionStart().atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.openAuction(product), startDate);

        Instant endDate = product.getAuctionEnd().atZone(Clock.systemDefaultZone().getZone()).toInstant();
        taskScheduler.schedule(() -> auctionScheduler.closeAuction(product), endDate);

        return ProductDto.Response.fromEntity(savedProduct);
    }

    @Override
    public ProductDto.Response getProductDetail(Long productId){
        List<Photo> photos = photoRepository.findByProductId(productId);
        if (photos.isEmpty()){
            throw new PhotoException(ErrorCode.PHOTO_NOT_FOUND);
        }

        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        return ProductDto.Response.fromEntity(findProduct, photos);
    }

    @Override
    public boolean isOnGoing(Long productId) {
        Product findProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));

        return findProduct.getProductBidPhase().equals(ProductBidPhase.ONGOING);
    }

    private void uploadPhoto(Product product, List<MultipartFile> images){

        try {
            String uploadsDir = "src/main/resources/static/uploads/photos/";

            for (MultipartFile image : images){
                String dbFilePath = saveImage(image, uploadsDir);

                Photo photo = new Photo(dbFilePath, product);
                photoRepository.save(photo);
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

//    @Override
//    public ProductDto.Response update(Long productId, List<MultipartFile> images, ProductDto.Request request, String token) {
//
//        if (productRepository.existsByTitle(request.getTitle())){
//            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT);
//        }
//
//        if(request.getAuctionStart().isBefore(LocalDateTime.now())){
//            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
//        }
//        if(request.getAuctionEnd().isBefore(request.getAuctionStart())){
//            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
//        }
//
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
//
//        Product productUpdate = Product.builder()
//                .id(product.getId())
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .startBid(request.getStartBid())
//                .auctionStart(request.getAuctionStart())
//                .auctionEnd(request.getAuctionEnd())
//                .member((memberRepository.findByMemberUUID(memberId).orElseThrow(
//                        () -> new IllegalArgumentException("멤버 값이 현재 없습니다."))))
//                .category(categoryRepository.findByCategoryName(request.getCategory()).orElseThrow(
//                                () -> new IllegalArgumentException("카테고리 값이 현재 없습니다.")))
//                .build();
//
//        uploadPhoto(productUpdate, images);
//        Product savedProduct = productRepository.save(productUpdate);
//
//        System.out.println("Saved Product ID: " + savedProduct.getId());
//        return ProductDto.Response.fromEntity(savedProduct);
//    }


    @Override
    public void delete(Long productId){
         productRepository.deleteById(productId);
    }

    @Override
    public Product findById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ErrorCode.NOT_EXISTS_PRODUCT));
    }

}
