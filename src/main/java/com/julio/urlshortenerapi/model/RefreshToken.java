package com.julio.urlshortenerapi.model;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@RedisHash("refresh_token")
@Builder
@Data
public class RefreshToken implements Serializable {

  @Id
  private String id;

  private String username;
  private String device;
  private String ip;

  @TimeToLive
  private long ttl;
}
