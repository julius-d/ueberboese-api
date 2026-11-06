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
   * Connector using the shared pool.
   *
   * <p>{@code compress(true)} matches Spring's default connector: it decompresses gzip responses,
   * which upstream servers send when the client asks for them.
   *
   * <p>{@code responseTimeout} fails a request when no response data arrives for that long. Without
   * it, a request on a silently dropped connection waits until the operating system gives up on TCP
   * retransmissions, which takes about 15 minutes on Linux.
   */
  @Bean
  public ReactorClientHttpConnector outboundClientHttpConnector(
      ConnectionProvider outboundConnectionProvider,
      @Value("${ueberboese.http-client.response-timeout:25s}") Duration responseTimeout) {
    log.info("Outbound HTTP client: response timeout {}", responseTimeout);
    return new ReactorClientHttpConnector(
        HttpClient.create(outboundConnectionProvider)
            .compress(true)
            .responseTimeout(responseTimeout));
  }
}
