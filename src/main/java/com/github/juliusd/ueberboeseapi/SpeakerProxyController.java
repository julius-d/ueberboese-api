package com.github.juliusd.ueberboeseapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

/**
 * Thin HTTP proxy to the speaker's local API (port 8090). Lets the browser-based preset UI call
 * speaker endpoints (POST /key, POST /storePreset, POST /removePreset) without CORS issues, since
 * the speaker only allows requests from its own origin.
 */
@RestController
@RequestMapping("/speaker-proxy")
@Slf4j
public class SpeakerProxyController {

  private static final String TUNEIN_SEARCH_URL =
      "http://opml.radiotime.com/Search.ashx?query=%s&formats=mp3&render=json";
  private static final String TUNEIN_BROWSE_URL = "http://opml.radiotime.com/?render=json";
  private static final String TUNEIN_USER_AGENT =
      "Mozilla/5.0 (compatible; SoundTouch/27.0; +https://github.com/julius-d/ueberboese-api)";

  private static final String TUNEIN_USER_AGENT =
      "Mozilla/5.0 (compatible; SoundTouch/27.0; +https://github.com/julius-d/ueberboese-api)";

  private final RestClient restClient = RestClient.create();

  @GetMapping("/image")
  public ResponseEntity<byte[]> proxyImage(@RequestParam String url) {
    try {
      if (!url.startsWith("http://cdn-profiles.tunein.com")
          && !url.startsWith("https://cdn-profiles.tunein.com")
          && !url.startsWith("http://cdn-radiotime-logos.tunein.com")
          && !url.startsWith("https://cdn-radiotime-logos.tunein.com")
          && !url.startsWith("http://cdn-albums.tunein.com")
          && !url.startsWith("https://cdn-albums.tunein.com")) {
        return ResponseEntity.badRequest().build();
      }
      byte[] image =
          restClient
              .get()
              .uri(url)
              .header("User-Agent", TUNEIN_USER_AGENT)
              .retrieve()
              .body(byte[].class);
      String ct = url.endsWith(".png") || url.contains(".png?") ? "image/png" : "image/jpeg";
      return ResponseEntity.ok()
          .header("Content-Type", ct)
          .header("Cache-Control", "public, max-age=86400")
          .body(image);
    } catch (Exception e) {
      log.debug("Image proxy error for {}: {}", url, e.getMessage());
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/speaker-info/{ip}")
  public ResponseEntity<String> speakerInfo(@PathVariable String ip) {
    try {
      String xml =
          restClient.get().uri("http://" + ip + ":8090/info").retrieve().body(String.class);
      return ResponseEntity.ok().header("Content-Type", "application/xml").body(xml);
    } catch (Exception e) {
      log.warn("Could not fetch /info from {}: {}", ip, e.getMessage());
      return ResponseEntity.status(502).body("<error>" + e.getMessage() + "</error>");
    }
  }

  @GetMapping("/tunein-search")
  public ResponseEntity<byte[]> tuneInSearch(@RequestParam String q) {
    try {
      String url =
          String.format(
              TUNEIN_SEARCH_URL,
              java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
      log.info("TuneIn search: {}", url);
      byte[] json =
          restClient
              .get()
              .uri(url)
              .header("User-Agent", TUNEIN_USER_AGENT)
              .retrieve()
              .body(byte[].class);
      return ResponseEntity.ok().header("Content-Type", "application/json").body(json);
    } catch (Exception e) {
      log.warn("TuneIn search error: {}", e.getMessage());
      return ResponseEntity.status(502).body("[]".getBytes());
    }
  }

  @GetMapping("/tunein-browse")
  public ResponseEntity<byte[]> tuneInBrowse(@RequestParam(defaultValue = "") String url) {
    try {
      String target = url.isEmpty() ? TUNEIN_BROWSE_URL : url;
      if (!target.startsWith("http://opml.radiotime.com")
          && !target.startsWith("https://opml.radiotime.com")) {
        return ResponseEntity.badRequest().body("[]".getBytes());
      }
      byte[] json =
          restClient
              .get()
              .uri(target + (target.contains("?") ? "&" : "?") + "render=json")
              .header("User-Agent", TUNEIN_USER_AGENT)
              .retrieve()
              .body(byte[].class);
      return ResponseEntity.ok().header("Content-Type", "application/json").body(json);
    } catch (Exception e) {
      log.warn("TuneIn browse error: {}", e.getMessage());
      return ResponseEntity.status(502).body("[]".getBytes());
    }
  }

  @PostMapping("/{ip}/key")
  public ResponseEntity<String> key(@PathVariable String ip, @RequestBody String body) {
    return forward(ip, "/key", body);
  }

  @PostMapping("/{ip}/storePreset")
  public ResponseEntity<String> storePreset(@PathVariable String ip, @RequestBody String body) {
    return forward(ip, "/storePreset", body);
  }

  @PostMapping("/{ip}/removePreset")
  public ResponseEntity<String> removePreset(@PathVariable String ip, @RequestBody String body) {
    return forward(ip, "/removePreset", body);
  }

  @PostMapping("/{ip}/select")
  public ResponseEntity<String> select(@PathVariable String ip, @RequestBody String body) {
    return forward(ip, "/select", body);
  }

  private ResponseEntity<String> forward(String ip, String path, String body) {
    log.info("Speaker proxy: POST {} to {}", path, ip);
    try {
      String response =
          restClient
              .post()
              .uri("http://" + ip + ":8090" + path)
              .header("Content-Type", "application/xml")
              .body(body)
              .retrieve()
              .body(String.class);
      return ResponseEntity.ok().header("Content-Type", "application/xml").body(response);
    } catch (Exception e) {
      log.warn("Speaker proxy error for {}{}: {}", ip, path, e.getMessage());
      return ResponseEntity.status(502).body("<error>" + e.getMessage() + "</error>");
    }
  }
}
