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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.math.BigDecimal;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
@RequiredArgsConstructor
@Slf4j
class IndexController {
    private final FaultyService service;
    private final CircuitBreakerFactory cbf;

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String index() {
        final String faultUrl =
                MvcUriComponentsBuilder.fromMethodName(FaultyController.class, "fault")
                        .build().toUriString();
        final String divideUrl =
                MvcUriComponentsBuilder.fromMethodName(DivideOpController.class, "divide",
                        BigDecimal.valueOf(10), BigDecimal.valueOf(2)).build().toUriString();
        final String timeUrl =
                MvcUriComponentsBuilder.fromMethodName(TimeController.class, "time", 1L).build().toUriString();
        final String timeoutUrl =
                MvcUriComponentsBuilder.fromMethodName(TimeoutController.class, "timeout", 1L).build().toUriString();
        return "These endpoints are available:\n" +
                " - " + faultUrl + "\n" +
                " - " + divideUrl + "\n" +
                " - " + timeUrl + "\n" +
                " - " + timeoutUrl + "\n";
    }
}
