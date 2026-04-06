package com.codewithpcodes.salima.auth;

import com.codewithpcodes.salima.user.Gender;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private UUID userId;
    private String fullName;
    private Gender gender;
}
