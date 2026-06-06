package com.company.learninghub.common.health;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HealthControllerTest {

    @Test
    void healthReturnsUpStatus() {
        HealthController controller = new HealthController();

        ResponseEntity<Map<String, Object>> response = controller.health();

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody())
                .containsEntry("status", "UP")
                .containsEntry("service", "engineering-learning-hub")
                .containsKey("timestampUtc");
    }
}

