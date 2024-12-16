package com.auction.bid.domain.product.impl;

import com.auction.bid.domain.member.MemberRepository;
import com.auction.bid.domain.photo.Photo;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.dto.ProductDto;
import com.auction.bid.domain.product.repository.CategoryRepository;
import com.auction.bid.domain.product.repository.PhotoRepository;
import com.auction.bid.domain.product.repository.ProductRepository;
import com.auction.bid.domain.product.service.ProductService;
import com.auction.bid.global.exception.ErrorCode;
import com.auction.bid.global.exception.exceptions.ProductException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final PhotoRepository photoRepository;

    private final MemberRepository memberRepository;

    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public ProductDto.Response register( List<MultipartFile> images, ProductDto.Request request, UUID memberId){

        if (productRepository.existsByTitle(request.getTitle())){
            throw new ProductException(ErrorCode.DUPLICATE_PRODUCT);
        }

        if(request.getAuctionStart().isBefore(LocalDateTime.now())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_START_TIME_NOW_AFTER);
        }
        if(request.getAuctionEnd().isBefore(request.getAuctionStart())){
            throw new ProductException(ErrorCode.INVALID_AUCTION_END_TIME_START_AFTER);
        }

        System.out.println("Entering register method...");
        System.out.println("Request: " + request.toString());
        System.out.println("Member ID: " + memberId);

        Product product =  ProductDto.Request.toEntity(request);
        product.assignMember(memberRepository.findByMemberId(memberId).orElseThrow(
                () -> new IllegalArgumentException("멤버 값이 현재 없습니다.")));

        product.assignCategory(categoryRepository.findByCategoryName(request.getCategory()).orElseThrow(
                () -> new IllegalArgumentException("카테고리 값이 현재 없습니다.")));

        System.out.println("Before Saving Product: " + product.toString());

        uploadPhoto(product, images);
        Product savedProduct = productRepository.save(product);

        System.out.println("Saved Product ID: " + savedProduct.getId());
        return ProductDto.Response.fromEntity(savedProduct);

    }

    public void uploadPhoto(Product product, List<MultipartFile> images){
        try {
            String uploadsDir = "src/main/resources/static/uploads/photos/";

            for (MultipartFile image : images){
                String dbFilePath = saveImage(image, uploadsDir);

                Photo photo = new Photo(dbFilePath, product);
                photoRepository.save(photo);
            }
        } catch (IOException e) {
            throw new ProductException(ErrorCode.FILE_UPLOAD_FAILED);
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
}
