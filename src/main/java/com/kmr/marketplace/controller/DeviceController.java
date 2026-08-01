package com.kmr.marketplace.controller;

import com.kmr.marketplace.dto.RegisterDeviceRequest;
import com.kmr.marketplace.security.AuthHelper;
import com.kmr.marketplace.service.PushService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final PushService pushService;
    private final AuthHelper authHelper;

    public DeviceController(PushService pushService, AuthHelper authHelper) {
        this.pushService = pushService;
        this.authHelper  = authHelper;
    }

    /** Register this device's FCM token for push notifications. */
    @PostMapping("/token")
    public Map<String, Boolean> register(@Valid @RequestBody RegisterDeviceRequest req) {
        pushService.registerToken(authHelper.currentUser(), req.token(), req.platform());
        return Map.of("registered", true);
    }

    @DeleteMapping("/token")
    public Map<String, Boolean> unregister(@RequestBody RegisterDeviceRequest req) {
        pushService.unregisterToken(req.token());
        return Map.of("registered", false);
    }
}
