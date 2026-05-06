package com.example.demo.service.impl;

import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.exception.ReservationNotFoundException;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.ReservationService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findBookedApartmentIds(startDate, endDate);
    }

    @Override
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        Reservation reservation = ReservationMapper.mapToReservation(reservationDTO, apartmentRepository, userRepository);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationMapper.mapToReservationDTO(savedReservation);
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationMapper::mapToReservationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(
                () -> new ReservationNotFoundException("Reservation is not exist with given id: " + id)
        );
        reservationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deletePendingReservations() {
        reservationRepository.deleteByStatus(ReservationStatus.PENDING);
    }

    @Override
    @Transactional
    public void pendingReservation(Long reservationId) throws MessagingException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        sendPendingReservationEmail(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) throws MessagingException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);

        sendCancelingReservationEmails(reservation);
    }

    @Override
    @Transactional
    public void confirmReservation(Long reservationId) throws MessagingException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        sendConfirmingReservationEmail(reservation);
    }

    private void sendPendingReservationEmail(Reservation reservation) throws MessagingException {
        String clientEmail = reservation.getClientEmail();
        List<String> managerEmails = userRepository.findByRole("admin").stream()
                .map(User::getEmail)
                .toList();

        String complexName = reservation.getApartment().getComplexName();

        Map<String, String> clientPlaceholders = new HashMap<>();
        clientPlaceholders.put("user.name", reservation.getUser().getUsername());
        clientPlaceholders.put("reservation.id", reservation.getId().toString());
        clientPlaceholders.put("apartment.name", reservation.getApartment().getName());
        clientPlaceholders.put("apartment.address", reservation.getApartment().getAddress());
        clientPlaceholders.put("apartment.complexName", complexName);
        clientPlaceholders.put("reservation.checkInDate", reservation.getCheckInDate().toString());
        clientPlaceholders.put("reservation.checkOutDate", reservation.getCheckOutDate().toString());
        clientPlaceholders.put("reservation.guestCount", String.valueOf(reservation.getGuestCount()));
        clientPlaceholders.put("lang", reservation.getClientLang());

        emailService.sendEmail(clientEmail, "reservation_pending", clientPlaceholders);

        Map<String, String> managerPlaceholders = new HashMap<>();
        managerPlaceholders.put("apartment.name", reservation.getApartment().getName());
        managerPlaceholders.put("reservation.checkInDate", reservation.getCheckInDate().toString());
        managerPlaceholders.put("reservation.checkOutDate", reservation.getCheckOutDate().toString());
        managerPlaceholders.put("reservation.guestCount", String.valueOf(reservation.getGuestCount()));
        managerPlaceholders.put("apartment.address", reservation.getApartment().getAddress());
        managerPlaceholders.put("apartment.complexName", complexName);
        managerPlaceholders.put("reservation.id", reservation.getId().toString());
        managerPlaceholders.put("user.name", reservation.getUser().getName());
        managerPlaceholders.put("user.email", reservation.getUser().getEmail());
        managerPlaceholders.put("user.phoneNumber", reservation.getUser().getPhoneNumber());
        managerPlaceholders.put("user.username", reservation.getUser().getUsername());
        managerPlaceholders.put("lang", reservation.getClientLang());

        for (String managerEmail : managerEmails) {
            emailService.sendEmail(managerEmail, "reservation_pending_admin", managerPlaceholders);
        }
    }

    private void sendCancelingReservationEmails(Reservation reservation) throws MessagingException {
        String clientEmail = reservation.getClientEmail();
        List<String> managerEmails = userRepository.findByRole("admin").stream()
                .map(User::getEmail)
                .toList();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("user.name", reservation.getUser().getName());
        placeholders.put("reservation.id", reservation.getId().toString());
        placeholders.put("apartment.name", reservation.getApartment().getName());
        placeholders.put("lang", reservation.getClientLang());

        emailService.sendEmail(clientEmail, "reservation_canceled", placeholders);

        for (String managerEmail : managerEmails) {
            emailService.sendEmail(managerEmail, "reservation_canceled_admin", placeholders);
        }
    }

    private void sendConfirmingReservationEmail(Reservation reservation) throws MessagingException {
        String clientEmail = reservation.getClientEmail();
        List<String> managerEmails = userRepository.findByRole("admin").stream()
                .map(User::getEmail)
                .toList();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("user.name", reservation.getUser().getName());
        placeholders.put("user.phoneNumber", reservation.getUser().getPhoneNumber());
        placeholders.put("reservation.id", reservation.getId().toString());
        placeholders.put("apartment.name", reservation.getApartment().getName());
        placeholders.put("apartment.address", reservation.getApartment().getAddress());
        placeholders.put("apartment.complexName", reservation.getApartment().getComplexName());
        placeholders.put("reservation.checkInDate", reservation.getCheckInDate().toString());
        placeholders.put("reservation.checkOutDate", reservation.getCheckOutDate().toString());
        placeholders.put("reservation.guestCount", String.valueOf(reservation.getGuestCount()));
        placeholders.put("lang", reservation.getClientLang());

        emailService.sendEmail(clientEmail, "reservation_confirmed", placeholders);

        for (String managerEmail : managerEmails) {
            emailService.sendEmail(managerEmail, "reservation_confirmed_admin", placeholders);
        }
    }
}
