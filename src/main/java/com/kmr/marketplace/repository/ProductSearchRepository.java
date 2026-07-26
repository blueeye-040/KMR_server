package com.kmr.marketplace.repository;

import com.kmr.marketplace.dto.FacetsDto;
import com.kmr.marketplace.dto.ProductFilter;

import java.util.List;

/**
 * Dynamic product search / filter / sort backed by native SQL, computing each
 * product's best (cheapest available) vendor price for price filters and sorting.
 */
public interface ProductSearchRepository {

    /** Ordered page of matching product ids + total count. */
    ProductIdPage search(ProductFilter filter);

    /** Brands + price bounds available in the category/search context. */
    FacetsDto facets(ProductFilter filter);

    record ProductIdPage(List<Long> ids, long total) {}
}
