package com.example.playmatch.mvp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequestDto {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+\\d{10,15}$", message = "Invalid phone number format. Must start with + and contain 10-15 digits")
    private String phoneNumber;
}
