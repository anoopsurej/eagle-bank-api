package com.eaglebank.api.user;

import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class UserMapper {

    public User toUser(CreateUserRequest request, String id, String hashedPassword) {
        Address address = new Address();
        address.setLine1(request.address().line1());
        address.setLine2(request.address().line2());
        address.setLine3(request.address().line3());
        address.setTown(request.address().town());
        address.setCounty(request.address().county());
        address.setPostcode(request.address().postcode());

        Instant now = Instant.now();
        User user = new User();
        user.setId(id);
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(hashedPassword);
        user.setAddress(address);
        user.setCreatedTimestamp(now);
        user.setUpdatedTimestamp(now);
        return user;
    }

    public void updateUser(User user, UpdateUserRequest request) {
        if (request.name() != null) user.setName(request.name());
        if (request.phoneNumber() != null) user.setPhoneNumber(request.phoneNumber());
        if (request.email() != null) user.setEmail(request.email());
        if (request.address() != null) {
            Address address = user.getAddress();
            address.setLine1(request.address().line1());
            address.setLine2(request.address().line2());
            address.setLine3(request.address().line3());
            address.setTown(request.address().town());
            address.setCounty(request.address().county());
            address.setPostcode(request.address().postcode());
        }
        user.setUpdatedTimestamp(java.time.Instant.now());
    }


    public UserResponse toResponse(User user) {
        AddressDto address = new AddressDto(
                user.getAddress().getLine1(),
                user.getAddress().getLine2(),
                user.getAddress().getLine3(),
                user.getAddress().getTown(),
                user.getAddress().getCounty(),
                user.getAddress().getPostcode()
        );
        return new UserResponse(
                user.getId(),
                user.getName(),
                address,
                user.getPhoneNumber(),
                user.getEmail(),
                user.getCreatedTimestamp(),
                user.getUpdatedTimestamp()
        );
    }
}
