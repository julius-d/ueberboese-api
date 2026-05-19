package com.github.juliusd.ueberboeseapi.bmx;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Client for interacting with TuneIn APIs to fetch station metadata and stream URLs. Uses TuneIn's
 * public OPML API.
 */
@Component
@Slf4j
public class TuneInClient {

  private final WebClient webClient;
  private final TuneInApiUrlProperties urlProperties;

  public TuneInClient(TuneInApiUrlProperties urlProperties) {
    this.webClient = WebClient.builder().build();
    this.urlProperties = urlProperties;
  }

  /**
   * Fetches station metadata from TuneIn.
   *
   * @param stationId TuneIn station ID (e.g., "s80044")
   * @return Station metadata
   */
  public StationMetadata getStationMetadata(String stationId) {
    try {
      String url = String.format(urlProperties.describeUrl(), stationId);
      log.debug("Fetching TuneIn station metadata from: {}", url);

      String xmlResponse = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();

      return parseStationMetadata(xmlResponse);
    } catch (Exception e) {
      log.error("Failed to fetch station metadata for stationId: {}", stationId, e);
      throw new RuntimeException("Failed to fetch TuneIn station metadata", e);
    }
  }

  /**
   * Fetches stream URLs for a station from TuneIn.
   *
   * @param stationId TuneIn station ID (e.g., "s80044")
   * @return List of stream URLs
   */
  public java.util.List<String> getStreamUrls(String stationId) {
    try {
      String url = String.format(urlProperties.streamUrl(), stationId);
      log.debug("Fetching TuneIn stream URLs from: {}", url);

      String response = webClient.get().uri(url).retrieve().bodyToMono(String.class).block();

      // TuneIn returns stream URLs as plain text, one per line
      if (response == null || response.isEmpty()) {
        log.warn("Empty response from TuneIn stream URL endpoint");
        return java.util.Collections.emptyList();
      }

      return java.util.Arrays.stream(response.split("\n"))
          .filter(line -> !line.trim().isEmpty())
          .map(String::trim)
          .toList();

    } catch (Exception e) {
      log.error("Failed to fetch stream URLs for stationId: {}", stationId, e);
      throw new RuntimeException("Failed to fetch TuneIn stream URLs", e);
    }
  }

  /**
   * Parses XML response from TuneIn describe endpoint.
   *
   * <p>Example XML: <opml version="1"> <head> <title>...</title> </head> <body> <outline
   * type="audio"> <station> <name>Radio TEDDY</name> <logo>http://...</logo>
   * <current_song>...</current_song> <current_artist>...</current_artist> </station> </outline>
   * </body> </opml>
   */
  private StationMetadata parseStationMetadata(String xmlResponse) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));

      // Navigate to station element: opml > body > outline > station
      NodeList bodyNodes = doc.getElementsByTagName("body");
      if (bodyNodes.getLength() == 0) {
        throw new RuntimeException("No body element in TuneIn response");
      }

      Element body = (Element) bodyNodes.item(0);
      NodeList outlineNodes = body.getElementsByTagName("outline");
      if (outlineNodes.getLength() == 0) {
        throw new RuntimeException("No outline element in TuneIn response");
      }

      Element outline = (Element) outlineNodes.item(0);
      NodeList stationNodes = outline.getElementsByTagName("station");
      if (stationNodes.getLength() == 0) {
        throw new RuntimeException("No station element in TuneIn response");
      }

      Element station = (Element) stationNodes.item(0);

      StationMetadata metadata = new StationMetadata();
      metadata.setName(getElementText(station, "name"));
      metadata.setLogo(getElementText(station, "logo"));
      metadata.setCurrentSong(getElementText(station, "current_song"));
      metadata.setCurrentArtist(getElementText(station, "current_artist"));

      log.debug(
          "Parsed station metadata: name={}, logo={}", metadata.getName(), metadata.getLogo());
      return metadata;

    } catch (Exception e) {
      log.error("Failed to parse TuneIn XML response", e);
      throw new RuntimeException("Failed to parse TuneIn XML", e);
    }
  }

  private String getElementText(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() > 0) {
      Node node = nodes.item(0);
      return node.getTextContent();
    }
    return "";
  }

  /** Station metadata DTO */
  @Data
  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class StationMetadata {
    @XmlElement private String name;
    @XmlElement private String logo;

    @XmlElement(name = "current_song")
    private String currentSong;

    @XmlElement(name = "current_artist")
    private String currentArtist;
  }
}
