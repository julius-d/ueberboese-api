package com.github.juliusd.ueberboeseapi;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/** HTTP client setup shared by all outbound {@code WebClient}s (Bose proxy, TuneIn). */
@Configuration
@Slf4j
public class HttpClientConfig {

  /**
   * Pooled connections that are idle for longer than {@code maxIdleTime} are closed instead of
   * being reused. Without a limit, Reactor Netty keeps idle connections forever, and a request can
   * pick up a connection the remote side (or a NAT/firewall in between) has already closed, which
   * then fails with a connection reset.
   */
  @Bean(destroyMethod = "dispose")
  public ConnectionProvider outboundConnectionProvider(
      @Value("${ueberboese.http-client.max-idle-time:60s}") Duration maxIdleTime) {
    log.info("Outbound HTTP connection pool: max idle time {}", maxIdleTime);
    return ConnectionProvider.builder("ueberboese-outbound").maxIdleTime(maxIdleTime).build();
  }

  /**
   * Connector using the shared pool. {@code compress(true)} matches Spring's default connector: it
   * decompresses gzip responses, which upstream servers send when the client asks for them.
   */
  @Bean
  public ReactorClientHttpConnector outboundClientHttpConnector(
      ConnectionProvider outboundConnectionProvider) {
    return new ReactorClientHttpConnector(
        HttpClient.create(outboundConnectionProvider).compress(true));
  }
}
