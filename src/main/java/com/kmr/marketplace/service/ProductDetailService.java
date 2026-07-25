package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.*;
import com.kmr.marketplace.repository.*;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductDetailService {

    private final ProductRepository      productRepo;
    private final ProductImageRepository productImageRepo;
    private final ShopProductRepository  shopProductRepo;
    private final ReviewRepository       reviewRepo;
    private final AuthHelper             authHelper;

    public ProductDetailService(ProductRepository productRepo,
                                ProductImageRepository productImageRepo,
                                ShopProductRepository shopProductRepo,
                                ReviewRepository reviewRepo,
                                AuthHelper authHelper) {
        this.productRepo      = productRepo;
        this.productImageRepo = productImageRepo;
        this.shopProductRepo  = shopProductRepo;
        this.reviewRepo       = reviewRepo;
        this.authHelper       = authHelper;
    }

    public ProductDetailDto getDetail(Long productId) {

        // 1. Product (with brand + category via EntityGraph)
        Product product = productRepo.findActiveById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        // 2. Gallery images
        List<String> gallery = productImageRepo
                .findByProductIdOrderBySortOrderAsc(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .toList();

        // 3. All sellers for this product, cheapest first
        List<ShopProduct> sellers =
                shopProductRepo.findByProductIdAndAvailableTrueOrderBySellingPriceAsc(productId);

        if (sellers.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No sellers available for this product");
        }

        ShopProduct cheapest = sellers.get(0);
        Shop bestShop = cheapest.getShop();

        List<SellerDto> allSellers = sellers.stream().map(sp -> new SellerDto(
                sp.getId(),
                sp.getShop().getId(),
                sp.getShop().getName(),
                sp.getShop().getLogoUrl(),
                sp.getShop().getRating() != null ? sp.getShop().getRating().doubleValue() : 0,
                sp.getShop().getTotalSales(),
                sp.getShop().getCity(),
                sp.getMrp().doubleValue(),
                sp.getSellingPrice().doubleValue(),
                sp.getDiscountPercent(),
                sp.getStock(),
                sp.getDeliveryDays(),
                sp.getShop().isOfficial()
        )).toList();

        // 4. Reviews (top 10, newest first)
        List<ReviewDto> reviews = reviewRepo
                .findByProductId(productId, PageRequest.of(0, 10))
                .getContent().stream()
                .map(r -> new ReviewDto(
                        r.getId(),
                        r.getUser().getName(),
                        r.getUser().getAvatarUrl(),
                        r.getRating(),
                        r.getTitle(),
                        r.getBody(),
                        r.isVerifiedPurchase(),
                        r.getHelpfulCount(),
                        r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
                )).toList();

        // 5. Rating summary
        List<Object[]> avgCountList = reviewRepo.avgAndCount(productId);
        Object[] avgCount = avgCountList.isEmpty() ? new Object[]{null, null} : avgCountList.get(0);
        double ratingAvg  = avgCount[0] != null ? ((Number) avgCount[0]).doubleValue() : 0.0;
        int reviewCount   = avgCount[1] != null ? ((Number) avgCount[1]).intValue()    : 0;

        // 6. Rating breakdown [5★, 4★, 3★, 2★, 1★]
        Map<Integer, Integer> breakMap = new HashMap<>();
        reviewRepo.ratingBreakdown(productId)
                .forEach(row -> breakMap.put(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue()));
        List<Integer> breakdown = List.of(
                breakMap.getOrDefault(5, 0),
                breakMap.getOrDefault(4, 0),
                breakMap.getOrDefault(3, 0),
                breakMap.getOrDefault(2, 0),
                breakMap.getOrDefault(1, 0));

        // 7. Related products (same category, up to 8, excluding self)
        Long catId = product.getCategory() != null ? product.getCategory().getId() : null;
        List<Product> relatedProds = (catId != null)
                ? productRepo.findRelated(catId, productId, PageRequest.of(0, 8))
                : List.of();

        Map<Long, ShopProduct> relatedPrices = buildBestPriceMap(
                relatedProds.stream().map(Product::getId).toList());

        List<ProductDto> related = relatedProds.stream()
                .map(p -> {
                    ShopProduct sp = relatedPrices.get(p.getId());
                    if (sp == null) return null;
                    return new ProductDto(
                            p.getId(), p.getName(), p.getSlug(), p.getImageUrl(),
                            p.getBrand()    != null ? p.getBrand().getName()    : null,
                            p.getCategory() != null ? p.getCategory().getName() : null,
                            sp.getMrp(), sp.getSellingPrice(),
                            sp.getDiscountPercent(), sp.getDeliveryDays(),
                            sp.getShop().getName(), sp.getShop().getId(),
                            p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0,
                            p.getReviewCount());
                })
                .filter(Objects::nonNull)
                .toList();

        return new ProductDetailDto(
                product.getId(), product.getName(), product.getSlug(),
                product.getDescription(), product.getImageUrl(), gallery,
                product.getBrand()    != null ? product.getBrand().getName()    : null,
                product.getBrand()    != null ? product.getBrand().getLogoUrl() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getId()   : null,
                product.getSpecifications(),
                product.isFeatured(), product.isNewArrival(), product.isTopSelling(),
                cheapest.getSellingPrice().doubleValue(),
                cheapest.getMrp().doubleValue(),
                cheapest.getDiscountPercent(),
                cheapest.getStock(),
                cheapest.getDeliveryDays(),
                bestShop.getName(), bestShop.getId(),
                bestShop.getRating() != null ? bestShop.getRating().doubleValue() : 0,
                cheapest.getId(),
                allSellers,
                ratingAvg, reviewCount, breakdown, reviews,
                related);
    }

    // ── Submit a new review ───────────────────────────────────────────────────

    @Transactional
    public ReviewDto addReview(Long productId, AddReviewRequest req) {
        User user = authHelper.currentUser();

        if (req.rating() < 1 || req.rating() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Rating must be between 1 and 5");
        }
        if (reviewRepo.existsByProductIdAndUserId(productId, user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already reviewed this product");
        }

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found"));

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating((short) req.rating());
        review.setTitle(req.title());
        review.setBody(req.body());
        review = reviewRepo.save(review);

        return new ReviewDto(review.getId(), user.getName(), user.getAvatarUrl(),
                review.getRating(), review.getTitle(), review.getBody(),
                false, 0, review.getCreatedAt().toString());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<Long, ShopProduct> buildBestPriceMap(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return shopProductRepo.findByProductIds(ids).stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp,
                        (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b));
    }
}
