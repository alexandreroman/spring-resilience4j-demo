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
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
class DivideOpService {
    BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b);
    }
}

@RestController
@RequiredArgsConstructor
@Slf4j
class DivideOpController {
    private final DivideOpService service;
    private final CircuitBreakerFactory cbf;

    @GetMapping(value = "/divide/{a}/by/{b}", produces = MediaType.TEXT_PLAIN_VALUE)
    String divide(@PathVariable("a") BigDecimal a, @PathVariable("b") BigDecimal b) {
        final AtomicBoolean error = new AtomicBoolean();
        final BigDecimal result = cbf.create("divideOp").run(() -> service.divide(a, b), throwable -> {
            log.warn("Divide error", throwable);
            error.set(true);
            return BigDecimal.ZERO;
        });
        if (error.get()) {
            return "There was an error: cannot divide " + a.toPlainString() + " by " + b.toPlainString() + "\n";
        }
        return a.toPlainString() + " / " + b.toPlainString() + " = " + result.toPlainString() + "\n";
    }
}
