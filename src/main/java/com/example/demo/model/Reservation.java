package com.example.demo.model;

import com.example.demo.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "reservation")
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "guest_count", nullable = false)
    private Long guestCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReservationStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "client_email")
    private String clientEmail;

    @Column(name = "client_lang")
    private String clientLang;

    @Column(name = "total_price")
    private Integer totalPrice;

    @Column(name = "created_at")
    private Instant createdAt;

    public Reservation(Long id, Apartment apartment, LocalDate checkInDate, LocalDate checkOutDate,
                        Long guestCount, ReservationStatus status, User user, String clientEmail,
                        String clientLang, Integer totalPrice) {
        this.id = id;
        this.apartment = apartment;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = status;
        this.user = user;
        this.clientEmail = clientEmail;
        this.clientLang = clientLang;
        this.totalPrice = totalPrice;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}