package com.github.juliusd.ueberboeseapi;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverters;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class XmlMessageConverterConfig implements WebMvcConfigurer {

  @Override
  public void configureMessageConverters(HttpMessageConverters.ServerBuilder builder) {
    var xmlConverter = new MappingJackson2XmlHttpMessageConverter(customXmlMapper());

    // Configure to ONLY handle XML media types (don't interfere with JSON)
    List<MediaType> xmlMediaTypes = new ArrayList<>();
    xmlMediaTypes.add(MediaType.APPLICATION_XML);
    xmlMediaTypes.add(MediaType.TEXT_XML);
    xmlMediaTypes.add(MediaType.parseMediaType("application/vnd.bose.streaming-v1.2+xml"));
    xmlConverter.setSupportedMediaTypes(xmlMediaTypes);

    JsonMapper jsonMapper =
        JsonMapper.builderWithJackson2Defaults()
            .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
            .changeDefaultPropertyInclusion(incl -> incl.withContentInclusion(Include.NON_NULL))
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();
    var jsonConverter = new JacksonJsonHttpMessageConverter(jsonMapper);
    // Configure JSON converter to also handle text/json media type (used by Bose devices)
    List<MediaType> jsonMediaTypes = new ArrayList<>();
    jsonMediaTypes.add(MediaType.APPLICATION_JSON);
    jsonMediaTypes.add(MediaType.parseMediaType("text/json"));
    jsonConverter.setSupportedMediaTypes(jsonMediaTypes);

    // Add byte array, JSON, and XML converters
    builder
        .addCustomConverter(new ByteArrayHttpMessageConverter())
        .addCustomConverter(jsonConverter)
        .addCustomConverter(xmlConverter);
  }

  @Bean(name = "customXmlMapper")
  public XmlMapper customXmlMapper() {
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
    xmlMapper.enable(ToXmlGenerator.Feature.WRITE_STANDALONE_YES_TO_XML_DECLARATION);
    xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    xmlMapper.registerModule(getCustomOffsetDateTimeModule());
    return xmlMapper;
  }

  private static @NonNull SimpleModule getCustomOffsetDateTimeModule() {
    // Create a custom module with OffsetDateTime serializer and deserializer
    // We don't use JavaTimeModule to avoid conflicts with our custom date format
    SimpleModule customModule = new SimpleModule("CustomOffsetDateTimeModule");

    // Custom serializer: outputs dates with +00:00 instead of Z
    customModule.addSerializer(
        OffsetDateTime.class,
        new JsonSerializer<>() {
          @Override
          public void serialize(
              OffsetDateTime value, JsonGenerator gen, SerializerProvider serializers)
              throws IOException {
            // Format the date with explicit +00:00 timezone format instead of Z
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            String formattedDate = value.format(formatter);
            String timezone =
                value.getOffset().toString().equals("Z") ? "+00:00" : value.getOffset().toString();
            gen.writeString(formattedDate + timezone);
          }
        });

    // Custom deserializer: parses dates with both +00:00 and Z formats
    customModule.addDeserializer(
        OffsetDateTime.class,
        new JsonDeserializer<>() {
          @Override
          public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt)
              throws IOException {
            String dateStr = p.getText();
            return OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
          }
        });
    return customModule;
  }
}
