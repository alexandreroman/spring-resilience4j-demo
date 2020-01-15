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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationTests {
    @Autowired
    private TestRestTemplate client;

    @Test
    public void contextLoads() {
    }

    @Test
    void testIndex() {
        final ResponseEntity<String> resp = client.getForEntity("/", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testPrometheus() {
        final ResponseEntity<String> resp = client.getForEntity("/actuator/prometheus", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testFault() {
        final ResponseEntity<String> resp = client.getForEntity("/fault", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("Did the service fail? YES\n");
    }

    @Test
    void testDivideOp() {
        final ResponseEntity<String> resp = client.getForEntity("/divide/10/by/2", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("10 / 2 = 5\n");
    }

    @Test
    void testDivideOpError() {
        final ResponseEntity<String> resp = client.getForEntity("/divide/10/by/0", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("There was an error: cannot divide 10 by 0\n");
    }

    @Test
    void testTime() {
        final ResponseEntity<String> resp = client.getForEntity("/time", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).startsWith("Time is: 20");
    }

    @Test
    void testTimeError() {
        final ResponseEntity<String> resp = client.getForEntity("/time?delay=3", String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("Time is: UNKNOWN (timeout waiting for response)\n");
    }
}
