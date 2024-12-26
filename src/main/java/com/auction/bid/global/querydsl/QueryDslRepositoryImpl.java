package com.auction.bid.global.querydsl;

import com.auction.bid.domain.auction.Auction;
import com.auction.bid.domain.auction.AuctionStatus;
import com.auction.bid.domain.bid.Bid;
import com.auction.bid.domain.product.Product;
import com.auction.bid.domain.product.ProductBidPhase;
import com.auction.bid.domain.sale.Sale;
import com.auction.bid.domain.sale.SaleStatus;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.auction.bid.domain.auction.QAuction.auction;
import static com.auction.bid.domain.bid.QBid.bid;
import static com.auction.bid.domain.member.QMember.member;
import static com.auction.bid.domain.photo.QPhoto.photo;
import static com.auction.bid.domain.product.QProduct.product;
import static com.auction.bid.domain.sale.QSale.sale;

@Repository
public class QueryDslRepositoryImpl implements QueryDslRepository{

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public QueryDslRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Map<String, List<Sale>> findTop10SaleThisWeek(
            LocalDateTime startOfWeek,
            LocalDateTime endOfWeek,
            Pageable pageable) {

        List<Sale> saleHighestList = getSales(startOfWeek, endOfWeek, pageable, sale.salePrice.desc());
        List<Sale> saleLowestList = getSales(startOfWeek, endOfWeek, pageable, sale.salePrice.asc());

        Map<String, List<Sale>> res = new HashMap<>();
        res.put("HighestList", saleHighestList);
        res.put("LowestList", saleLowestList);
        return res;
    }

    @Override
    public Page<Sale> getHighestSaleList(Pageable pageable) {
        return getSales(pageable, sale.salePrice.desc());
    }

    @Override
    public Page<Sale> getLowestSaleList(Pageable pageable) {
        return getSales(pageable, sale.salePrice.asc());
    }

    @Override
    public List<Product> findAllByProductIds(List<Long> productIds) {
        return queryFactory
                .selectFrom(product)
                .leftJoin(product.photos).fetchJoin()
                .where(product.id.in(productIds))
                .fetch();
    }

    @Override
    public Page<Product> getProductByPhase(ProductBidPhase phase, Pageable pageable) {
        List<Product> products = queryFactory
                .selectFrom(product)
                .leftJoin(product.photos).fetchJoin()
                .where(product.productBidPhase.eq(phase))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(product)
                .where(product.productBidPhase.eq(phase));

        return PageableExecutionUtils.getPage(products, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Auction> getAuctionList(UUID memberUUID, Pageable pageable, AuctionStatus auctionStatus) {
        BooleanExpression condition =
                auctionStatus != null ?
                        auction.auctionStatus.eq(auctionStatus) : null;

        List<Auction> auctionList = queryFactory
                .selectFrom(auction)
                .leftJoin(auction.member, member).fetchJoin()
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(product.photos).fetchJoin()
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(auction.count())
                .from(auction)
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                );

        return PageableExecutionUtils.getPage(auctionList, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Bid> findAllByProductId(Long productId) {
        return queryFactory
                .selectFrom(bid)
                .leftJoin(bid.product, product).fetchJoin()
                .leftJoin(bid.member, member).fetchJoin()
                .where(product.id.eq(productId))
                .fetch();
    }

    @Override
    public Auction getAuctionEagerly(Long auctionId) {
        return queryFactory
                .selectFrom(auction)
                .leftJoin(auction.product, product).fetchJoin()
                .leftJoin(product.member).fetchJoin()
                .where(auction.id.eq(auctionId))
                .fetchOne();
    }

    @Override
    public List<Sale> getSaleList(UUID memberUUID, Pageable pageable, SaleStatus saleStatus) {
        BooleanExpression condition =
                saleStatus != null ?
                        sale.saleStatus.eq(saleStatus) : null;

        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.member, member).fetchJoin()
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos).fetchJoin()
                .where(
                        member.memberUUID.eq(memberUUID),
                        condition
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Sale getSaleEagerly(Long saleId) {
        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(sale.member).fetchJoin()
                .where(sale.id.eq(saleId))
                .fetchOne();
    }

    private List<Sale> getSales(LocalDateTime startOfWeek, LocalDateTime endOfWeek, Pageable pageable, OrderSpecifier<?> orderSpecifier) {
        return queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos, photo).fetchJoin()
                .where(
                        sale.saleStatus.eq(SaleStatus.SALE_SUCCESS),
                        sale.createdAt.between(startOfWeek, endOfWeek)
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Page<Sale> getSales(Pageable pageable, OrderSpecifier<?> orderSpecifier) {
        List<Sale> saleList = queryFactory
                .selectFrom(sale)
                .leftJoin(sale.product, product).fetchJoin()
                .leftJoin(product.photos, photo).fetchJoin()
                .where(
                        sale.saleStatus.eq(SaleStatus.SALE_SUCCESS)
                )
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(sale.count())
                .from(sale)
                .where(sale.saleStatus.eq(SaleStatus.SALE_SUCCESS));

        return PageableExecutionUtils.getPage(saleList, pageable, countQuery::fetchOne);
    }

}
