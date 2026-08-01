package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.Category;
import com.kmr.marketplace.entity.Product;
import com.kmr.marketplace.entity.ShopProduct;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.*;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class HomeService {

    private final BannerRepository       bannerRepo;
    private final CategoryRepository     categoryRepo;
    private final ProductRepository      productRepo;
    private final ShopProductRepository  shopProductRepo;
    private final ProductService         productService;
    private final AuthHelper             authHelper;

    public HomeService(BannerRepository bannerRepo,
                       CategoryRepository categoryRepo,
                       ProductRepository productRepo,
                       ShopProductRepository shopProductRepo,
                       ProductService productService,
                       AuthHelper authHelper) {
        this.bannerRepo      = bannerRepo;
        this.categoryRepo    = categoryRepo;
        this.productRepo     = productRepo;
        this.shopProductRepo = shopProductRepo;
        this.productService  = productService;
        this.authHelper      = authHelper;
    }

    public HomeResponse getHomeFeed() {
        // ── Banners (1 query) ─────────────────────────────────────
        List<BannerDto> banners = bannerRepo
                .findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(b -> new BannerDto(b.getId(), b.getTitle(), b.getSubtitle(),
                                        b.getImageUrl(), b.getBgColorHex()))
                .toList();

        // ── Categories (1 query) ──────────────────────────────────
        List<CategoryDto> categories = getCategories();

        // ── Product sections (3 queries) ─────────────────────────
        List<Product> featured    = productRepo.findTop10ByFeaturedTrueAndActiveTrueOrderByIdDesc();
        List<Product> newArrivals = productRepo.findTop10ByNewArrivalTrueAndActiveTrueOrderByCreatedAtDesc();
        List<Product> topSelling  = productRepo.findTop10ByTopSellingTrueAndActiveTrueOrderByIdDesc();

        // ── Batch-load best shop prices — 1 query for all sections
        Set<Long> allIds = new LinkedHashSet<>();
        featured.forEach(p    -> allIds.add(p.getId()));
        newArrivals.forEach(p -> allIds.add(p.getId()));
        topSelling.forEach(p  -> allIds.add(p.getId()));

        Map<Long, ShopProduct> bestPriceMap = buildBestPriceMap(new ArrayList<>(allIds));

        // Deals & Recommended reuse the search engine (best-price aware).
        List<ProductDto> deals = productService.search(new ProductFilter(
                null, null, null, null, null, null, null, 20, "discount", 0, 10)).content();
        List<ProductDto> recommended = productService.search(new ProductFilter(
                null, null, null, null, null, null, null, null, "rating", 0, 10)).content();

        return new HomeResponse(
                banners,
                categories,
                deals,
                toProductDtos(featured,    bestPriceMap),
                toProductDtos(newArrivals, bestPriceMap),
                toProductDtos(topSelling,  bestPriceMap),
                recommended,
                buildGreeting()
        );
    }

    /** Flat list of shoppable (leaf) categories — used by the home category grid. */
    public List<CategoryDto> getCategories() {
        return categoryRepo.findByActiveTrueAndParentIdIsNotNullOrderBySortOrderAscIdAsc()
                .stream()
                .map(HomeService::toCategoryDto)
                .toList();
    }

    /** Department → child categories tree — used by the Categories browse tab. */
    public List<CategoryTreeDto> getCategoryTree() {
        List<Category> leaves = categoryRepo.findByActiveTrueAndParentIdIsNotNullOrderBySortOrderAscIdAsc();
        Map<Long, List<CategoryDto>> childrenByParent = leaves.stream()
                .collect(Collectors.groupingBy(Category::getParentId, LinkedHashMap::new,
                        Collectors.mapping(HomeService::toCategoryDto, Collectors.toList())));

        return categoryRepo.findByActiveTrueAndParentIdIsNullOrderBySortOrderAscIdAsc()
                .stream()
                .map(d -> new CategoryTreeDto(d.getId(), d.getName(), d.getEmoji(),
                        d.getColorHex(), d.getImageUrl(),
                        childrenByParent.getOrDefault(d.getId(), List.of())))
                .filter(t -> !t.children().isEmpty())   // hide empty departments
                .toList();
    }

    private static CategoryDto toCategoryDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getEmoji(), c.getColorHex(), c.getImageUrl());
    }

    // ── Private helpers ───────────────────────────────────────────

    private Map<Long, ShopProduct> buildBestPriceMap(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        // One query for all; then pick cheapest per product in memory
        return shopProductRepo.findByProductIds(ids)
                .stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(),
                        sp -> sp,
                        (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b
                ));
    }

    private List<ProductDto> toProductDtos(List<Product> products,
                                            Map<Long, ShopProduct> bestPriceMap) {
        return products.stream()
                .map(p -> {
                    ShopProduct sp = bestPriceMap.get(p.getId());
                    if (sp == null) return null;   // no active seller — skip
                    return new ProductDto(
                            p.getId(), p.getName(), p.getSlug(), p.getImageUrl(),
                            p.getBrand()    != null ? p.getBrand().getName()    : null,
                            p.getCategory() != null ? p.getCategory().getName() : null,
                            sp.getMrp(), sp.getSellingPrice(),
                            sp.getDiscountPercent(), sp.getDeliveryDays(),
                            sp.getShop().getName(), sp.getShop().getId(),
                            p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0,
                            p.getReviewCount()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private String buildGreeting() {
        User user = authHelper.currentUserOrNull();
        String name = (user != null) ? user.getName().split(" ")[0] : "there";
        int hour = LocalTime.now().getHour();
        String time = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
        return time + ", " + name + "!";
    }
}
