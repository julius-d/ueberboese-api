package com.github.juliusd.ueberboeseapi.spotify;

import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyEntitiesClient;
import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyOAuthClient;
import com.github.juliusd.ueberboeseapi.spotify.client.SpotifyUserClient;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuration for Spotify HTTP service clients using Spring Framework 6's HTTP interface support.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SpotifyHttpServiceConfig {

  private final SpotifyApiUrlProperties spotifyApiUrlProperties;

  /**
   * Creates a RestClient configured for Spotify API calls.
   *
   * @return Configured RestClient instance
   */
  @Bean
  public RestClient spotifyRestClient() {
    URI baseUrl = spotifyApiUrlProperties.baseUrl();
    log.info("Configuring Spotify RestClient with base URL: {}", baseUrl);

    return RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Accept", "application/json")
        // Note: Do NOT set Content-Type header here - let Spring determine it based on the body
        .requestInterceptor(
            (request, body, execution) -> {
              log.debug(
                  "Spotify API Request: {} {} - Headers: {}",
                  request.getMethod(),
                  request.getURI(),
                  request.getHeaders());
              if (body != null && body.length > 0) {
                log.debug("Request body: {}", new String(body));
              }
              return execution.execute(request, body);
            })
        .defaultStatusHandler(
            HttpStatusCode::is4xxClientError,
            (request, response) -> {
              String responseBody = new String(response.getBody().readAllBytes());
              log.warn(
                  "Spotify API client error: {} {} - {} {}, Response body: {}",
                  request.getMethod(),
                  request.getURI(),
                  response.getStatusCode(),
                  response.getStatusText(),
                  responseBody);
              throw new SpotifyException(
                  "Spotify API error: "
                      + response.getStatusCode()
                      + " - "
                      + response.getStatusText());
            })
        .defaultStatusHandler(
            HttpStatusCode::is5xxServerError,
            (request, response) -> {
              String responseBody = new String(response.getBody().readAllBytes());
              log.error(
                  "Spotify API server error: {} {} - {} {}, Response body: {}",
                  request.getMethod(),
                  request.getURI(),
                  response.getStatusCode(),
                  response.getStatusText(),
                  responseBody);
              throw new SpotifyException(
                  "Spotify API error: "
                      + response.getStatusCode()
                      + " - "
                      + response.getStatusText());
            })
        .build();
  }

  /**
   * Creates a RestClient configured for Spotify API calls that require Bearer token authentication.
   *
   * @return Configured RestClient instance with Authorization header support
   */
  @Bean
  public RestClient spotifyAuthenticatedRestClient() {
    var baseUrl = spotifyApiUrlProperties.authBaseUrl();

    log.info("Configuring authenticated Spotify RestClient with base URL: {}", baseUrl);

    return RestClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Accept", "application/json")
        .defaultHeader("Content-Type", "application/json")
        .defaultStatusHandler(
            HttpStatusCode::is4xxClientError,
            (request, response) -> {
              String responseBody = new String(response.getBody().readAllBytes());
              log.warn(
                  "Spotify API client error: {} {} - {} {}, Response body: {}",
                  request.getMethod(),
                  request.getURI(),
                  response.getStatusCode(),
                  response.getStatusText(),
                  responseBody);
              throw new SpotifyException(
                  "Spotify API error: "
                      + response.getStatusCode()
                      + " - "
                      + response.getStatusText());
            })
        .defaultStatusHandler(
            HttpStatusCode::is5xxServerError,
            (request, response) -> {
              String responseBody = new String(response.getBody().readAllBytes());
              log.error(
                  "Spotify API server error: {} {} - {} {}, Response body: {}",
                  request.getMethod(),
                  request.getURI(),
                  response.getStatusCode(),
                  response.getStatusText(),
                  responseBody);
              throw new SpotifyException(
                  "Spotify API error: "
                      + response.getStatusCode()
                      + " - "
                      + response.getStatusText());
            })
        .build();
  }

  /**
   * Creates an HttpServiceProxyFactory for creating HTTP service client proxies.
   *
   * @param spotifyRestClient The RestClient to use
   * @return HttpServiceProxyFactory instance
   */
  @Bean
  public HttpServiceProxyFactory spotifyHttpServiceProxyFactory(RestClient spotifyRestClient) {
    return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(spotifyRestClient)).build();
  }

  /**
   * Creates an HttpServiceProxyFactory for authenticated HTTP service client proxies.
   *
   * @param spotifyAuthenticatedRestClient The authenticated RestClient to use
   * @return HttpServiceProxyFactory instance
   */
  @Bean
  public HttpServiceProxyFactory spotifyAuthenticatedHttpServiceProxyFactory(
      RestClient spotifyAuthenticatedRestClient) {
    return HttpServiceProxyFactory.builderFor(
            RestClientAdapter.create(spotifyAuthenticatedRestClient))
        .build();
  }

  /**
   * Creates a SpotifyOAuthClient bean for OAuth operations.
   *
   * @param spotifyHttpServiceProxyFactory HttpServiceProxyFactory
   * @return SpotifyOAuthClient instance
   */
  @Bean
  public SpotifyOAuthClient spotifyOAuthClient(
      HttpServiceProxyFactory spotifyHttpServiceProxyFactory) {
    return spotifyHttpServiceProxyFactory.createClient(SpotifyOAuthClient.class);
  }

  /**
   * Creates a SpotifyEntitiesClient bean for entity operations.
   *
   * @param spotifyAuthenticatedHttpServiceProxyFactory Authenticated HttpServiceProxyFactory
   * @return SpotifyEntitiesClient instance
   */
  @Bean
  public SpotifyEntitiesClient spotifyEntitiesClient(
      HttpServiceProxyFactory spotifyAuthenticatedHttpServiceProxyFactory) {
    return spotifyAuthenticatedHttpServiceProxyFactory.createClient(SpotifyEntitiesClient.class);
  }

  /**
   * Creates a SpotifyUserClient bean for user operations.
   *
   * @param spotifyAuthenticatedHttpServiceProxyFactory Authenticated HttpServiceProxyFactory
   * @return SpotifyUserClient instance
   */
  @Bean
  public SpotifyUserClient spotifyUserClient(
      HttpServiceProxyFactory spotifyAuthenticatedHttpServiceProxyFactory) {
    return spotifyAuthenticatedHttpServiceProxyFactory.createClient(SpotifyUserClient.class);
  }
}
