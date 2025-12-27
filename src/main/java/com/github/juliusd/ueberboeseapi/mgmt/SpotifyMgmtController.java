package com.github.juliusd.ueberboeseapi.mgmt;

import com.github.juliusd.ueberboeseapi.generated.mgmt.SpotifyManagementApi;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ConfirmSpotifyAuth200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ErrorApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.GetSpotifyEntity200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.GetSpotifyEntityRequestApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.InitSpotifyAuth200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.ListSpotifyAccounts200ResponseApiDto;
import com.github.juliusd.ueberboeseapi.generated.mgmt.dtos.SpotifyAccountListItemApiDto;
import com.github.juliusd.ueberboeseapi.spotify.InvalidSpotifyUriException;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyAccountService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyEntityNotFoundException;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyEntityService;
import com.github.juliusd.ueberboeseapi.spotify.SpotifyManagementService;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SpotifyMgmtController implements SpotifyManagementApi {

  private final SpotifyManagementService spotifyManagementService;
  private final SpotifyAccountService spotifyAccountService;
  private final SpotifyMgmtProperties spotifyMgmtProperties;
  private final SpotifyEntityService spotifyEntityService;

  @Override
  public ResponseEntity<InitSpotifyAuth200ResponseApiDto> initSpotifyAuth() {
    log.info("Initializing Spotify OAuth flow");

    try {
      String redirectUri = spotifyMgmtProperties.redirectUri();
      String authUrl = spotifyManagementService.generateAuthorizationUrl(redirectUri);

      InitSpotifyAuth200ResponseApiDto response = new InitSpotifyAuth200ResponseApiDto();
      response.setRedirectUrl(URI.create(authUrl));

      log.info("Successfully initialized Spotify OAuth flow");
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (Exception e) {
      log.error("Failed to initialize Spotify OAuth flow: {}", e.getMessage());
      throw e;
    }
  }

  @Override
  public ResponseEntity<ConfirmSpotifyAuth200ResponseApiDto> confirmSpotifyAuth(String code) {
    log.info("Confirming Spotify OAuth authentication");

    if (code == null || code.isBlank()) {
      log.warn("Missing or empty authorization code");
      throw new IllegalArgumentException("Authorization code is required");
    }

    try {
      String redirectUri = spotifyMgmtProperties.redirectUri();
      String accountId = spotifyManagementService.exchangeCodeForTokens(code, redirectUri);

      ConfirmSpotifyAuth200ResponseApiDto response = new ConfirmSpotifyAuth200ResponseApiDto();
      response.setSuccess(true);
      response.setMessage("Spotify account connected successfully");
      response.setAccountId(accountId);

      log.info("Successfully confirmed Spotify authentication for accountId: {}", accountId);
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (SpotifyManagementService.SpotifyManagementException e) {
      log.error("Spotify authentication failed: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to confirm Spotify authentication: {}", e.getMessage());
      throw new RuntimeException("Failed to confirm Spotify authentication", e);
    }
  }

  @Override
  public ResponseEntity<ListSpotifyAccounts200ResponseApiDto> listSpotifyAccounts() {
    log.info("Listing Spotify accounts");

    try {
      List<SpotifyAccountService.SpotifyAccount> accounts = spotifyAccountService.listAllAccounts();

      List<SpotifyAccountListItemApiDto> accountDtos =
          accounts.stream()
              .map(
                  account -> {
                    SpotifyAccountListItemApiDto dto = new SpotifyAccountListItemApiDto();
                    dto.setSpotifyUserId(account.spotifyUserId());
                    dto.setDisplayName(account.displayName());
                    dto.setCreatedAt(account.createdAt());
                    return dto;
                  })
              .toList();

      ListSpotifyAccounts200ResponseApiDto response = new ListSpotifyAccounts200ResponseApiDto();
      response.setAccounts(accountDtos);

      log.info("Successfully listed {} Spotify account(s)", accountDtos.size());
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (IOException e) {
      log.error("Failed to list Spotify accounts: {}", e.getMessage());
      throw new RuntimeException("Failed to list Spotify accounts", e);
    }
  }

  @Override
  public ResponseEntity<GetSpotifyEntity200ResponseApiDto> getSpotifyEntity(
      GetSpotifyEntityRequestApiDto getSpotifyEntityRequestApiDto) {
    try {
      String uri = getSpotifyEntityRequestApiDto.getUri();
      log.info("Getting Spotify entity info for URI: {}", uri);

      var entityInfo = spotifyEntityService.getEntityInfo(uri);

      var response = new GetSpotifyEntity200ResponseApiDto();
      response.setName(entityInfo.name());
      response.setImageUrl(entityInfo.imageUrl());

      log.info(
          "Successfully retrieved entity info - name: {}, imageUrl: {}",
          entityInfo.name(),
          entityInfo.imageUrl());
      return ResponseEntity.ok().header("Content-Type", "application/json").body(response);

    } catch (InvalidSpotifyUriException | SpotifyEntityNotFoundException e) {
      log.warn("Failed to get entity info: {}", e.getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to get Spotify entity info: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to retrieve Spotify entity information", e);
    }
  }

  /** Exception handler for IllegalArgumentException - returns 400 Bad Request. */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorApiDto> handleIllegalArgumentException(IllegalArgumentException e) {
    log.warn("Bad request: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Missing parameter");
    error.setMessage(e.getMessage());

    return ResponseEntity.badRequest().header("Content-Type", "application/json").body(error);
  }

  /** Exception handler for InvalidSpotifyUriException - returns 400 Bad Request. */
  @ExceptionHandler(InvalidSpotifyUriException.class)
  public ResponseEntity<ErrorApiDto> handleInvalidSpotifyUriException(
      InvalidSpotifyUriException e) {
    log.warn("Invalid Spotify URI: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Invalid URI");
    error.setMessage(e.getMessage());

    return ResponseEntity.badRequest().header("Content-Type", "application/json").body(error);
  }

  /** Exception handler for SpotifyEntityNotFoundException - returns 404 Not Found. */
  @ExceptionHandler(SpotifyEntityNotFoundException.class)
  public ResponseEntity<ErrorApiDto> handleSpotifyEntityNotFoundException(
      SpotifyEntityNotFoundException e) {
    log.warn("Spotify entity not found: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Not found");
    error.setMessage(e.getMessage());

    return ResponseEntity.status(404).header("Content-Type", "application/json").body(error);
  }

  /** Exception handler for SpotifyManagementException - returns 401 Unauthorized. */
  @ExceptionHandler(SpotifyManagementService.SpotifyManagementException.class)
  public ResponseEntity<ErrorApiDto> handleSpotifyManagementException(
      SpotifyManagementService.SpotifyManagementException e) {
    log.error("Spotify authentication error: {}", e.getMessage());

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Authentication failed");
    error.setMessage(e.getMessage());

    return ResponseEntity.status(401).header("Content-Type", "application/json").body(error);
  }

  /** Generic exception handler - returns 500 Internal Server Error. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorApiDto> handleException(Exception e) {
    log.error("Internal server error: {}", e.getMessage(), e);

    ErrorApiDto error = new ErrorApiDto();
    error.setError("Internal server error");
    error.setMessage("Failed to process request");

    return ResponseEntity.status(500).header("Content-Type", "application/json").body(error);
  }
}
