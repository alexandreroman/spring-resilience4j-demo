/*
 * Copyright (c) 2020 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.springresilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
@Slf4j
class TimeoutController {
    private final RestTemplateBuilder restTemplateBuilder;
    private final CircuitBreakerFactory cbf;

    @Value("${app.timeout.network:2s}")
    private Duration networkTimeout;

    @GetMapping(value = "/timeout", produces = MediaType.TEXT_PLAIN_VALUE)
    String timeout(@RequestParam(value = "delay", required = false, defaultValue = "0") @PositiveOrZero long delaySec) {
        final String result = cbf.create("timeout").run(callExternalService(delaySec), throwable -> {
            log.warn("Timeout while calling network service", throwable);
            return (throwable instanceof RestClientException ? "NETWORK_TIMEOUT" : "PROCESS_TIMEOUT") + "\n";
        });
        return "Network call result: " + result + "\n";
    }

    private Supplier<String> callExternalService(long delaySec) {
        return () -> {
            final String url =
                    UriComponentsBuilder.fromHttpUrl("http://httpbin.org").pathSegment("delay", String.valueOf(delaySec)).toUriString();
            log.info("Calling service: {}", url);

            final RestTemplate client = restTemplateBuilder.setConnectTimeout(networkTimeout)
                    .setReadTimeout(networkTimeout).build();

            final ResponseEntity<String> resp = client.getForEntity(url, String.class);
            return (resp.getStatusCode().is2xxSuccessful() ? "SUCCESSFUL" : "FAILED");
        };
    }
}

@Configuration
class TimeoutConfig {
    @Value("${app.timeout.process:4s}")
    private Duration processTimeout;

    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> timeoutCustomizer() {
        return factory -> factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(processTimeout).build()), "timeout");
    }
}
