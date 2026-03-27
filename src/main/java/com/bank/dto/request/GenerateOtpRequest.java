package com.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.Objects;

@Data
public class GenerateOtpRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Pattern(regexp = "^\\+\\d{12}$", message = "Mobile number must start with + and contain exactly 12 digits")
    private String mobileNumber;

    // Add equals, hashCode, toString for testability and coverage
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenerateOtpRequest that = (GenerateOtpRequest) o;
        return Objects.equals(username, that.username) && Objects.equals(mobileNumber, that.mobileNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, mobileNumber);
    }

    @Override
    public String toString() {
        return "GenerateOtpRequest{" +
                "username='" + username + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                '}';
    }
}