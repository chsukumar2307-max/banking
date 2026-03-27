package com.bank.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Objects;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_details") // Removed unique constraint
public class OtpDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String username;

	@Column(name = "mobile_number", nullable = false, length = 15)
	private String mobileNumber;

	@Column(length = 10)
	private String otp;

	@Column(name = "expiry_time", nullable = false)
	private Instant expiryTime;

	@Column(nullable = false)
	private boolean verified;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OtpDetails that = (OtpDetails) o;
        return id != null && id.equals(that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(mobileNumber, that.mobileNumber) &&
                Objects.equals(otp, that.otp) &&
                Objects.equals(expiryTime, that.expiryTime) &&
                verified == that.verified &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, mobileNumber, otp, expiryTime, verified, createdAt);
    }

    @Override
    public String toString() {
        return "OtpDetails{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", otp='" + otp + '\'' +
                ", expiryTime=" + expiryTime +
                ", verified=" + verified +
                ", createdAt=" + createdAt +
                '}';
    }
}
