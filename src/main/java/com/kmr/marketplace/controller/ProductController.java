package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.*;
import com.kmr.marketplace.entity.Product;
import com.kmr.marketplace.entity.ShopProduct;
import com.kmr.marketplace.repository.ProductRepository;
import com.kmr.marketplace.repository.ShopProductRepository;
import com.kmr.marketplace.service.ProductDetailService;
import com.kmr.marketplace.service.ProductService;
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

    private final ProductDetailService   detailService;
    private final ProductService         productService;

    public ProductController(ProductDetailService detailService,
                             ProductService productService) {
        this.detailService   = detailService;
        this.productService  = productService;
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
     * GET /api/products — search / filter / sort / paginate. All params optional.
     * q, categoryId, departmentId, brandId, minPrice, maxPrice, minRating,
     * minDiscount, sort(relevance|price_asc|price_desc|newest|popularity|rating|discount),
     * page, size. Public — no auth required.
     */
    @GetMapping("/products")
    public ResponseEntity<PagedProductResponse> getProducts(
            @RequestParam(required = false) String  q,
            @RequestParam(required = false) Long    categoryId,
            @RequestParam(required = false) Long    departmentId,
            @RequestParam(required = false) Long    brandId,
            @RequestParam(required = false) Double  minPrice,
            @RequestParam(required = false) Double  maxPrice,
            @RequestParam(required = false) Double  minRating,
            @RequestParam(required = false) Integer minDiscount,
            @RequestParam(required = false) String  sort,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductFilter filter = new ProductFilter(q, categoryId, departmentId, brandId,
                minPrice, maxPrice, minRating, minDiscount, sort, page, size);
        return ResponseEntity.ok(productService.search(filter));
    }

    /**
     * GET /api/products/facets — available brands + price range for a category/search,
     * used to populate the filter sheet. Public.
     */
    @GetMapping("/products/facets")
    public ResponseEntity<FacetsDto> getFacets(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long   categoryId,
            @RequestParam(required = false) Long   departmentId) {
        ProductFilter filter = new ProductFilter(q, categoryId, departmentId,
                null, null, null, null, null, null, 0, 20);
        return ResponseEntity.ok(productService.facets(filter));
    }
}
