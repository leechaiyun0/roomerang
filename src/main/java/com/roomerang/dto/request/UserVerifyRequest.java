package com.roomerang.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVerifyRequest {
    @NotNull
    private Long userId;

    @NotNull
    private String securityAnswer;
}
