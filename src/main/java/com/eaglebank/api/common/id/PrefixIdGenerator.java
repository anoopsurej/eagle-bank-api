package com.eaglebank.api.common.id;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class PrefixIdGenerator {
    public String userId() {
        return "usr-" + generateId();
    }

    public String accountNumber() {
        int suffix = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("01%06d", suffix);
    }

    public String transactionId() {
        return "tan-" + generateId();
    }

    public String generateId() {
        return UUID.randomUUID().toString().replace("-","");
    }
}
