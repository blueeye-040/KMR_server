package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.AddressDto;
import com.kmr.marketplace.dto.AddressRequest;
import com.kmr.marketplace.entity.Address;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.AddressRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepo;
    private final AuthHelper authHelper;

    public AddressService(AddressRepository addressRepo, AuthHelper authHelper) {
        this.addressRepo = addressRepo;
        this.authHelper  = authHelper;
    }

    @Transactional(readOnly = true)
    public List<AddressDto> list() {
        User user = authHelper.currentUser();
        return addressRepo.findByUserIdOrderByIsDefaultDescIdDesc(user.getId())
                .stream().map(AddressService::toDto).toList();
    }

    public AddressDto create(AddressRequest req) {
        User user = authHelper.currentUser();
        boolean makeDefault = req.isDefault()
                || addressRepo.findByUserIdOrderByIsDefaultDescIdDesc(user.getId()).isEmpty();
        if (makeDefault) addressRepo.clearDefault(user.getId());

        Address a = new Address();
        a.setUser(user);
        apply(a, req);
        a.setDefault(makeDefault);
        return toDto(addressRepo.save(a));
    }

    public AddressDto update(Long id, AddressRequest req) {
        User user = authHelper.currentUser();
        Address a = owned(id, user.getId());
        if (req.isDefault()) addressRepo.clearDefault(user.getId());
        apply(a, req);
        a.setDefault(req.isDefault());
        return toDto(addressRepo.save(a));
    }

    public void delete(Long id) {
        User user = authHelper.currentUser();
        addressRepo.delete(owned(id, user.getId()));
    }

    public AddressDto setDefault(Long id) {
        User user = authHelper.currentUser();
        Address a = owned(id, user.getId());
        addressRepo.clearDefault(user.getId());
        a.setDefault(true);
        return toDto(addressRepo.save(a));
    }

    private Address owned(Long id, Long userId) {
        return addressRepo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void apply(Address a, AddressRequest req) {
        a.setName(req.name());
        a.setPhone(req.phone());
        a.setAddressLine1(req.addressLine1());
        a.setAddressLine2(req.addressLine2());
        a.setCity(req.city());
        a.setState(req.state());
        a.setPincode(req.pincode());
        a.setType(req.type() == null ? "HOME" : req.type());
    }

    static AddressDto toDto(Address a) {
        return new AddressDto(a.getId(), a.getName(), a.getPhone(),
                a.getAddressLine1(), a.getAddressLine2(), a.getCity(),
                a.getState(), a.getPincode(), a.isDefault(), a.getType());
    }
}
