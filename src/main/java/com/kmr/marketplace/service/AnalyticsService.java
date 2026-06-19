package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.AnalyticsResponse;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.entity.UserRole;
import com.kmr.marketplace.repository.*;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ProductRepository  productRepo;
    private final CategoryRepository categoryRepo;
    private final ShopRepository     shopRepo;
    private final UserRepository     userRepo;
    private final AuthHelper         authHelper;

    public AnalyticsService(ProductRepository productRepo,
                             CategoryRepository categoryRepo,
                             ShopRepository shopRepo,
                             UserRepository userRepo,
                             AuthHelper authHelper) {
        this.productRepo  = productRepo;
        this.categoryRepo = categoryRepo;
        this.shopRepo     = shopRepo;
        this.userRepo     = userRepo;
        this.authHelper   = authHelper;
    }

    public AnalyticsResponse getSummary() {
        User user = authHelper.currentUser();   // 401 if not authenticated

        long products   = productRepo.countByActiveTrue();
        long categories = categoryRepo.count();
        long shops      = shopRepo.countByApprovedTrue();

        if (user.getRole() == UserRole.ADMIN) {
            return new AnalyticsResponse(
                    "ADMIN", products, categories, shops,
                    userRepo.count(), 0L, 0.0
            );
        }

        // VENDOR / CUSTOMER — show platform stats, hide user count
        return new AnalyticsResponse(
                user.getRole().name(), products, categories, shops,
                -1L, 0L, 0.0
        );
    }
}
