package com.eaglebank.api.common.id;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PrefixIdGenerator {
    public String userId() {
        return "usr" + generateId();
    }

    public String generateId() {
        return UUID.randomUUID().toString().replace("-","");
    }
}
