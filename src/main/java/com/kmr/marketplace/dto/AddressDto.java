package com.kmr.marketplace.dto;

public record AddressDto(
        Long    id,
        String  name,
        String  phone,
        String  addressLine1,
        String  addressLine2,
        String  city,
        String  state,
        String  pincode,
        boolean isDefault,
        String  type
) {}
