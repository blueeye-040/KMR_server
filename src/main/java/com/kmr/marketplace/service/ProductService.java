package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.Product;
import com.kmr.marketplace.entity.ShopProduct;
import com.kmr.marketplace.repository.ProductRepository;
import com.kmr.marketplace.repository.ProductSearchRepository;
import com.kmr.marketplace.repository.ShopProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product listing with search, filters and sort. Uses ProductSearchRepository to
 * resolve the matching, ordered, paged product ids, then attaches each product's
 * best (cheapest available) vendor price for display.
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository       productRepo;
    private final ShopProductRepository   shopProductRepo;
    private final ProductSearchRepository searchRepo;

    public ProductService(ProductRepository productRepo,
                          ShopProductRepository shopProductRepo,
                          ProductSearchRepository searchRepo) {
        this.productRepo     = productRepo;
        this.shopProductRepo = shopProductRepo;
        this.searchRepo      = searchRepo;
    }

    public PagedProductResponse search(ProductFilter filter) {
        int size = Math.min(Math.max(filter.size(), 1), 50);
        ProductSearchRepository.ProductIdPage page = searchRepo.search(filter);

        List<ProductDto> dtos = toDtos(page.ids());
        long total = page.total();
        int totalPages = (int) Math.ceil((double) total / size);
        boolean last = (filter.page() + 1) >= totalPages;

        return new PagedProductResponse(dtos, filter.page(), size, total, totalPages, last);
    }

    public FacetsDto facets(ProductFilter filter) {
        return searchRepo.facets(filter);
    }

    /** Build display DTOs for an ordered id list, preserving that order. */
    private List<ProductDto> toDtos(List<Long> ids) {
        if (ids.isEmpty()) return List.of();

        Map<Long, Product> products = productRepo.findByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        Map<Long, ShopProduct> best = shopProductRepo.findByProductIds(new ArrayList<>(ids)).stream()
                .collect(Collectors.toMap(
                        sp -> sp.getProduct().getId(), sp -> sp,
                        (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b,
                        LinkedHashMap::new));

        List<ProductDto> out = new ArrayList<>(ids.size());
        for (Long id : ids) {                       // preserve search order
            Product p = products.get(id);
            ShopProduct sp = best.get(id);
            if (p == null || sp == null) continue;
            out.add(new ProductDto(
                    p.getId(), p.getName(), p.getSlug(), p.getImageUrl(),
                    p.getBrand()    != null ? p.getBrand().getName()    : null,
                    p.getCategory() != null ? p.getCategory().getName() : null,
                    sp.getMrp(), sp.getSellingPrice(),
                    sp.getDiscountPercent(), sp.getDeliveryDays(),
                    sp.getShop().getName(), sp.getShop().getId(),
                    p.getRatingAvg() != null ? p.getRatingAvg().doubleValue() : 0.0,
                    p.getReviewCount()));
        }
        return out;
    }
}
