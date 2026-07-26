package com.kmr.marketplace.repository;

import com.kmr.marketplace.dto.FacetsDto;
import com.kmr.marketplace.dto.ProductFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    @PersistenceContext
    private EntityManager em;

    // Each product joined to its best available vendor price + max discount.
    private static final String BASE =
            "FROM products p " +
            "JOIN categories c ON c.id = p.category_id " +
            "JOIN (SELECT product_id, MIN(selling_price) AS price, MAX(discount_percent) AS disc " +
            "      FROM shop_products WHERE available AND stock > 0 GROUP BY product_id) sp " +
            "  ON sp.product_id = p.id ";

    // Whitelisted ORDER BY clauses — never interpolate user input into SQL.
    private static final Map<String, String> SORTS = Map.of(
            "price_asc",  "sp.price ASC, p.id DESC",
            "price_desc", "sp.price DESC, p.id DESC",
            "newest",     "p.created_at DESC NULLS LAST, p.id DESC",
            "popularity", "p.review_count DESC, p.rating_avg DESC NULLS LAST, p.id DESC",
            "rating",     "p.rating_avg DESC NULLS LAST, p.review_count DESC, p.id DESC",
            "discount",   "sp.disc DESC, p.id DESC",
            "relevance",  "p.id DESC");

    @Override
    public ProductIdPage search(ProductFilter f) {
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(f, params, true, true, true);

        Query countQ = em.createNativeQuery("SELECT COUNT(*) " + BASE + where);
        params.forEach(countQ::setParameter);
        long total = ((Number) countQ.getSingleResult()).longValue();

        if (total == 0) return new ProductIdPage(List.of(), 0);

        String order = SORTS.getOrDefault(f.sort() == null ? "relevance" : f.sort(), "p.id DESC");
        Query q = em.createNativeQuery(
                "SELECT p.id " + BASE + where + " ORDER BY " + order + " LIMIT :__lim OFFSET :__off");
        params.forEach(q::setParameter);
        int size = Math.min(Math.max(f.size(), 1), 50);
        q.setParameter("__lim", size);
        q.setParameter("__off", (long) Math.max(f.page(), 0) * size);

        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();
        List<Long> ids = rows.stream().map(o -> ((Number) o).longValue()).toList();
        return new ProductIdPage(ids, total);
    }

    @Override
    public FacetsDto facets(ProductFilter f) {
        // Facets ignore the currently-selected brand/price/rating/discount so the
        // sheet always shows the full option set for the category/search context.
        Map<String, Object> params = new LinkedHashMap<>();
        String where = buildWhere(f, params, false, false, false);

        Query brandQ = em.createNativeQuery(
                "SELECT b.id, b.name, COUNT(*) " + BASE +
                "JOIN brands b ON b.id = p.brand_id " + where +
                " GROUP BY b.id, b.name ORDER BY COUNT(*) DESC, b.name ASC");
        params.forEach(brandQ::setParameter);
        @SuppressWarnings("unchecked")
        List<Object[]> brandRows = brandQ.getResultList();
        List<FacetsDto.BrandFacet> brands = new ArrayList<>();
        for (Object[] r : brandRows) {
            brands.add(new FacetsDto.BrandFacet(
                    ((Number) r[0]).longValue(), (String) r[1], ((Number) r[2]).longValue()));
        }

        Query priceQ = em.createNativeQuery(
                "SELECT COALESCE(MIN(sp.price),0), COALESCE(MAX(sp.price),0) " + BASE + where);
        params.forEach(priceQ::setParameter);
        Object[] pr = (Object[]) priceQ.getSingleResult();
        double minPrice = ((Number) pr[0]).doubleValue();
        double maxPrice = ((Number) pr[1]).doubleValue();

        return new FacetsDto(brands, minPrice, maxPrice);
    }

    // Builds "WHERE p.active AND ..." adding only the constraints that are set,
    // binding just those params (avoids Postgres null-type issues).
    private String buildWhere(ProductFilter f, Map<String, Object> params,
                              boolean includeBrand, boolean includePrice, boolean includeRatingDiscount) {
        List<String> c = new ArrayList<>();
        c.add("p.active = true");

        if (f.categoryId() != null) {
            c.add("p.category_id = :catId");
            params.put("catId", f.categoryId());
        }
        if (f.departmentId() != null) {
            c.add("c.parent_id = :deptId");
            params.put("deptId", f.departmentId());
        }
        if (f.q() != null && !f.q().isBlank()) {
            c.add("(p.name ILIKE :qlike OR " +
                  "to_tsvector('english', coalesce(p.name,'') || ' ' || coalesce(p.description,'')) " +
                  "@@ plainto_tsquery('english', :q))");
            params.put("qlike", "%" + f.q().trim() + "%");
            params.put("q", f.q().trim());
        }
        if (includeBrand && f.brandId() != null) {
            c.add("p.brand_id = :brandId");
            params.put("brandId", f.brandId());
        }
        if (includePrice && f.minPrice() != null) {
            c.add("sp.price >= :minPrice");
            params.put("minPrice", f.minPrice());
        }
        if (includePrice && f.maxPrice() != null) {
            c.add("sp.price <= :maxPrice");
            params.put("maxPrice", f.maxPrice());
        }
        if (includeRatingDiscount && f.minRating() != null) {
            c.add("p.rating_avg >= :minRating");
            params.put("minRating", f.minRating());
        }
        if (includeRatingDiscount && f.minDiscount() != null) {
            c.add("sp.disc >= :minDiscount");
            params.put("minDiscount", f.minDiscount());
        }
        return " WHERE " + String.join(" AND ", c);
    }
}
