package com.github.juliusd.ueberboeseapi.spotify;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotifyAccountRepository extends CrudRepository<SpotifyAccount, Long> {

  @Query("SELECT * FROM SPOTIFY_ACCOUNT ORDER BY CREATED_AT DESC")
  List<SpotifyAccount> findAllByOrderByCreatedAtDesc();

  boolean existsBySpotifyUserId(String spotifyUserId);

  Optional<SpotifyAccount> findBySpotifyUserId(String spotifyUserId);
}
