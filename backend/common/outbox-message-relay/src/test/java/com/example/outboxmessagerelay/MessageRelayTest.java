package com.example.outboxmessagerelay;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestApplication.class)
public class MessageRelayTest {

    @DisplayName("")
    @Test
    void test() {
        System.out.println("확인");
    }
}
