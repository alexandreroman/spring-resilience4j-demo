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
import org.springframework.web.bind.annotation.RestController;

@Component
class FaultyService {
    boolean doSomething() {
        throw new IllegalArgumentException("Runtime error");
    }
}

@RestController
@RequiredArgsConstructor
@Slf4j
class FaultyController {
    private final FaultyService service;
    private final CircuitBreakerFactory cbf;

    @GetMapping(value = "/fault", produces = MediaType.TEXT_PLAIN_VALUE)
    String fault() {
        final boolean success = cbf.create("faulty").run(service::doSomething, throwable -> {
            log.warn("Got error from service", throwable);
            return false;
        });
        return "Did the service fail? " + (success ? "NO" : "YES") + "\n";
    }
}
