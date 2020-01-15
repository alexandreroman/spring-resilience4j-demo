# Using Resilience4j with Spring Cloud Circuit Breaker

This project shows how to leverage [Resilience4j](https://github.com/resilience4j/resilience4j)
together with [Spring Cloud Circuit Breaker](https://spring.io/projects/spring-cloud-circuitbreaker).

Spring Cloud Circuit Breaker (SCCB) is a young project from the Spring Framework.
Using SCCB you get an abstraction layer over many circuit breaker implementations, such as:
 - Resilience4j
 - [Spring Retry](https://github.com/spring-projects/spring-retry)
 - [Sentinel](https://github.com/alibaba/Sentinel)
 - [Netflix Hystrix](https://github.com/Netflix/Hystrix) (now in [maintenance mode](https://github.com/Netflix/Hystrix#hystrix-status))

This way you can easily switch from an implementation to another one,
with no to low impact in your source code.

Let's see how you can leverage Resilience4j with your Spring Boot app.

## How to use it?

Compile this app using a JDK 11+:
```bash
$ ./mvnw clean package
```

Start the app:
```bash
$ java -jar target/spring-resilience4j-demo.jar
```

The app is exposing several endpoints at http://localhost:8080:
```bash
$ curl localhost:8080
These endpoints are available:
 - http://localhost:8080/fault
 - http://localhost:8080/divide/10/by/2
 - http://localhost:8080/time?delay=1
 - http://localhost:8080/timeout?delay=1
```

The home page shows you how to use demo endpoints.
Please refer to the [source code](src/main/java/fr/alexandreroman/demos/springresilience4j)
to learn what these endpoints do.

## How does it work?

Using SCCB with Resilience4j is pretty easy:
 - add a `CircuitBreakerFactory` in your component
 - create a `CircuitBreaker` instance using `create`
 - wrap your method call with `run`
 - pass a `Function` instance to handle faults.

Here's an example of using a circuit breaker:
```java
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
```

In this example, there is no single reference to Resilience4j API:
you may switch the circuit breaker implementation
without having to update your source code.

Resilience4j metrics are available in the Spring Boot Actuator Prometheus endpoint:
```bash
$ curl localhost:8080/actuator/prometheus
```

## Deploying to Pivotal Platform

This app can be easily deployed to [Pivotal Platform](https://pivotal.io/platform):
```bash
$ cf push
```

## Deploying to Kubernetes

This app can be deployed to any Kubernetes clusters.
Container image was built using [Cloud-Native Buildpacks](https://buildpacks.io).
Using the `pack` CLI, you create container images without having to write a `Dockerfile`.

Deploy this app to your cluster:
```bash
$ kubectl apply -f k8s
```

A namespace `spring-resilience4j-demo` is created for this app:
```bash
$ kubectl -n spring-resilience4j-demo get pod,svc
NAME                       READY   STATUS    RESTARTS   AGE
pod/app-549c8c898d-4wwc9   1/1     Running   0          38s

NAME          TYPE           CLUSTER-IP       EXTERNAL-IP     PORT(S)        AGE
service/app   LoadBalancer   10.100.200.221   35.240.113.73   80:30813/TCP   38s
```

## Contribute

Contributions are always welcome!

Feel free to open issues & send PR.

## License

Copyright &copy; 2020 [VMware, Inc](https://vmware.com).

This project is licensed under the [Apache Software License version 2.0](https://www.apache.org/licenses/LICENSE-2.0).
