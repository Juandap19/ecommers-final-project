package com.e2e.tests.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.lifecycle.Startables;
import com.e2e.tests.config.TestClientConfig;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(initializers = E2ESuite.Initializer.class)
@Import({TestRestFacade.class, TestClientConfig.class})
public class E2ESuite {

    protected static final Network network = Network.newNetwork();

    @Autowired
    protected TestRestFacade rest;

    protected static GenericContainer<?> zipkinContainer;
    protected static GenericContainer<?> serviceDiscoveryContainer;
    protected static GenericContainer<?> cloudConfigContainer;
    protected static GenericContainer<?> productServiceContainer;
    protected static GenericContainer<?> userServiceContainer;
    protected static GenericContainer<?> orderServiceContainer;
    protected static GenericContainer<?> paymentServiceContainer;
    protected static GenericContainer<?> favouriteServiceContainer;

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext context) {
            Environment env = context.getEnvironment();

            zipkinContainer = new GenericContainer<>("openzipkin/zipkin")
                    .withNetwork(network)
                    .withNetworkAliases("zipkin-container")
                    .withExposedPorts(9411)
                    .waitingFor(Wait.forHttp("/").forStatusCode(200));

            serviceDiscoveryContainer = new GenericContainer<>("minichocolate/service-discovery:latest")
                    .withNetwork(network)
                    .withNetworkAliases("service-discovery-container")
                    .withExposedPorts(8761)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

            cloudConfigContainer = new GenericContainer<>("minichocolate/cloud-config:latest")
                    .withNetwork(network)
                    .withNetworkAliases("cloud-config-container")
                    .withExposedPorts(9296)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", "http://service-discovery-container:8761/eureka/")
                    .withEnv("EUREKA_INSTANCE", "cloud-config-container")
                    .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));

            userServiceContainer = new GenericContainer<>("minichocolate/user-service:latest")
                    .withNetwork(network)
                    .withNetworkAliases("user-service-container")
                    .withExposedPorts(8700)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", "user-service-container")
                    .waitingFor(Wait.forHttp("/user-service/actuator/health").forStatusCode(200))
                    .withStartupTimeout(Duration.ofMinutes(3));

            productServiceContainer = new GenericContainer<>("minichocolate/product-service:latest")
                    .withNetwork(network)
                    .withNetworkAliases("product-service-container")
                    .withExposedPorts(8500)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", "product-service-container")
                    .waitingFor(Wait.forHttp("/product-service/actuator/health").forStatusCode(200))
                            .withStartupTimeout(Duration.ofMinutes(3));

            orderServiceContainer = new GenericContainer<>("minichocolate/order-service:latest")
                    .withNetwork(network)
                    .withNetworkAliases("order-service-container")
                    .withExposedPorts(8300)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", "order-service-container")
                    .waitingFor(Wait.forHttp("/order-service/actuator/health").forStatusCode(200))
                            .withStartupTimeout(Duration.ofMinutes(3));

            paymentServiceContainer = new GenericContainer<>("minichocolate/payment-service:latest")
                    .withNetwork(network)
                    .withNetworkAliases("payment-service-container")
                    .withExposedPorts(8400)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", "payment-service-container")
                    .waitingFor(Wait.forHttp("/payment-service/actuator/health").forStatusCode(200))
                            .withStartupTimeout(Duration.ofMinutes(3));

            favouriteServiceContainer = new GenericContainer<>("minichocolate/favourite-service:latest")
                    .withNetwork(network)
                    .withNetworkAliases("favourite-service-container")
                    .withExposedPorts(8800)
                    .withEnv("SPRING_PROFILES_ACTIVE", "dev")
                    .withEnv("SPRING_ZIPKIN_BASE_URL", "http://zipkin-container:9411")
                    .withEnv("SPRING_CONFIG_IMPORT", "optional:configserver:http://cloud-config-container:9296")
                    .withEnv("EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE", "http://service-discovery-container:8761/eureka")
                    .withEnv("EUREKA_INSTANCE", "favourite-service-container")
                    .waitingFor(Wait.forHttp("/favourite-service/actuator/health").forStatusCode(200))
                            .withStartupTimeout(Duration.ofMinutes(3));

            Startables.deepStart(Stream.of(zipkinContainer, serviceDiscoveryContainer)).join();
            Startables.deepStart(Stream.of(cloudConfigContainer)).join();
            Startables.deepStart(Stream.of(productServiceContainer)).join();
            Startables.deepStart(Stream.of(userServiceContainer)).join();
            Startables.deepStart(Stream.of(orderServiceContainer)).join();
            Startables.deepStart(Stream.of(paymentServiceContainer)).join();
            Startables.deepStart(Stream.of(favouriteServiceContainer)).join();


            Map<String, Object> props = Map.of(
                    "user.service.url", "http://" + userServiceContainer.getHost() + ":" + userServiceContainer.getMappedPort(8700),
                    "service.discovery.url", "http://" + serviceDiscoveryContainer.getHost() + ":" + serviceDiscoveryContainer.getMappedPort(8761),
                    "cloud.config.url", "http://" + cloudConfigContainer.getHost() + ":" + cloudConfigContainer.getMappedPort(9296),
                    "zipkin.url", "http://" + zipkinContainer.getHost() + ":" + zipkinContainer.getMappedPort(9411),
                    "product.service.url", "http://" + productServiceContainer.getHost() + ":" + productServiceContainer.getMappedPort(8500),
                    "order.service.url", "http://" + orderServiceContainer.getHost() + ":" + orderServiceContainer.getMappedPort(8300),
                    "payment.service.url", "http://" + paymentServiceContainer.getHost() + ":" + paymentServiceContainer.getMappedPort(8400),
                    "favourite.service.url", "http://" + favouriteServiceContainer.getHost() + ":" + favouriteServiceContainer.getMappedPort(8800)
            );

            ConfigurableEnvironment environment = context.getEnvironment();
            environment.getPropertySources().addFirst(new MapPropertySource("testcontainers", props));
        }
    }
}

