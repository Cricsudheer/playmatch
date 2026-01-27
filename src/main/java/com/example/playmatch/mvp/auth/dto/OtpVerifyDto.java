package com.example.playmatch.mvp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+\\d{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "OTP code is required")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otpCode;
}
