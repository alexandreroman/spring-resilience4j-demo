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
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.PositiveOrZero;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
class TimeService {
    String whatTimeIsIt(long delaySec) {
        if (delaySec > 0) {
            final long delayMs = TimeUnit.SECONDS.toMillis(delaySec);
            log.info("Delaying response: {} ms", delayMs);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignore) {
            }
        }
        return OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}

@RestController
@RequiredArgsConstructor
@Slf4j
class TimeController {
    private final TimeService service;
    private final CircuitBreakerFactory cbf;

    @GetMapping(value = "/time", produces = MediaType.TEXT_PLAIN_VALUE)
    String time(@RequestParam(value = "delay", required = false, defaultValue = "0") @PositiveOrZero long delaySec) {
        final String time = cbf.create("time").run(() -> service.whatTimeIsIt(delaySec), throwable -> {
            log.warn("Got an error while getting time", throwable);
            return "UNKNOWN (timeout waiting for response)\n";
        });
        return "Time is: " + time;
    }
}

@Configuration
class TimeConfig {
    @Bean
    Customizer<Resilience4JCircuitBreakerFactory> timeCustomizer() {
        return factory -> factory.configure(builder -> builder.circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(2)).build()), "time");
    }
}
