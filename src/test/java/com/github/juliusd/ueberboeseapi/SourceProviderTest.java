package com.github.juliusd.ueberboeseapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SourceProviderTest {

  @Test
  void shouldHaveCorrectNumberOfProviders() {
    assertThat(SourceProvider.values().length).isEqualTo(38);
  }

  @Test
  void shouldHaveUniqueIds() {
    Set<Integer> ids =
        Arrays.stream(SourceProvider.values())
            .map(SourceProvider::getId)
            .collect(Collectors.toSet());

    assertThat(ids.size())
        .as("All provider IDs should be unique")
        .isEqualTo(SourceProvider.values().length);
  }

  @Test
  void shouldHaveUniqueNames() {
    Set<String> names =
        Arrays.stream(SourceProvider.values())
            .map(SourceProvider::getName)
            .collect(Collectors.toSet());

    assertThat(names.size())
        .as("All provider names should be unique")
        .isEqualTo(SourceProvider.values().length);
  }

  @Test
  void shouldHaveValidDateFormat() {
    for (SourceProvider provider : SourceProvider.values()) {
      assertThat(provider.getCreatedOn())
          .as("CreatedOn should not be null for " + provider.name())
          .isNotNull();
      assertThat(provider.getUpdatedOn())
          .as("UpdatedOn should not be null for " + provider.name())
          .isNotNull();
    }
  }

  @Test
  void shouldHaveCorrectIdRange() {
    List<Integer> ids =
        Arrays.stream(SourceProvider.values()).map(SourceProvider::getId).sorted().toList();

    assertThat(ids.getFirst().intValue()).as("First ID should be 1").isEqualTo(1);
    assertThat(ids.getLast().intValue()).as("Last ID should be 38").isEqualTo(38);
  }

  @Test
  void shouldHaveSpecificProviders() {
    // Test some key providers exist
    assertThat(SourceProvider.SPOTIFY).isNotNull();
    assertThat(SourceProvider.TUNEIN).isNotNull();
  }

  @Test
  void sevenDigitalShouldHaveCorrectEnumName() {
    // Test that 7DIGITAL is properly named as SEVEN_DIGITAL (since Java enum names can't start with
    // numbers)
    SourceProvider sevenDigital = SourceProvider.SEVEN_DIGITAL;

    assertThat(sevenDigital.getId()).isEqualTo(30);
    assertThat(sevenDigital.getName()).isEqualTo("7DIGITAL");
  }
}
