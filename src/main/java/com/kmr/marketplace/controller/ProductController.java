package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.Product;
import com.kmr.marketplace.entity.ShopProduct;
import com.kmr.marketplace.repository.ProductRepository;
import com.kmr.marketplace.repository.ShopProductRepository;
import com.kmr.marketplace.service.ProductDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Transactional(readOnly = true)
public class ProductController {

    private final ProductRepository      productRepo;
    private final ShopProductRepository  shopProductRepo;
    private final ProductDetailService   detailService;

    public ProductController(ProductRepository productRepo,
                              ShopProductRepository shopProductRepo,
                              ProductDetailService detailService) {
        this.productRepo     = productRepo;
        this.shopProductRepo = shopProductRepo;
        this.detailService   = detailService;
    }

    /**
     * GET /api/products/{id}
     * Full product detail — public.
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailDto> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(detailService.getDetail(id));
    }

    /**
     * POST /api/products/{id}/reviews  — requires auth
     */
    @PostMapping("/products/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewDto addReview(@PathVariable Long id,
                               @RequestBody AddReviewRequest req) {
        return detailService.addReview(id, req);
    }

    /**
     * GET /api/products?categoryId=1&page=0&size=20
     * Public — no auth required.
     */
    @GetMapping("/products")
    public ResponseEntity<PagedProductResponse> getProducts(
            @RequestParam(required = false) Long   categoryId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<Product> productPage = (categoryId != null)
                ? productRepo.findByCategoryIdAndActiveTrueOrderByIdDesc(categoryId, pageable)
                : productRepo.findByActiveTrueOrderByIdDesc(pageable);

        // Batch-load best shop prices in a single query
        List<Long> ids = productPage.getContent().stream()
                .map(Product::getId).toList();

        Map<Long, ShopProduct> bestPrice = ids.isEmpty() ? Map.of() :
                shopProductRepo.findByProductIds(new ArrayList<>(ids))
                        .stream()
                        .collect(Collectors.toMap(
                                sp -> sp.getProduct().getId(),
                                sp -> sp,
                                (a, b) -> a.getSellingPrice().compareTo(b.getSellingPrice()) <= 0 ? a : b
                        ));

        List<ProductDto> dtos = productPage.getContent().stream()
                .map(p -> {
                    ShopProduct sp = bestPrice.get(p.getId());
                    if (sp == null) return null;
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

        return ResponseEntity.ok(new PagedProductResponse(
                dtos,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast()
        ));
    }
}
