package com.example.demo.service.impl;

import com.example.demo.dto.BookedDateRangeDTO;
import com.example.demo.dto.ReservationDTO;
import com.example.demo.enums.PriceUnit;
import com.example.demo.enums.ReservationStatus;
import com.example.demo.enums.RuleStatus;
import com.example.demo.exception.ApartmentNotFoundException;
import com.example.demo.exception.MinStayViolationException;
import com.example.demo.exception.ReservationConflictException;
import com.example.demo.exception.ReservationNotFoundException;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.model.Apartment;
import com.example.demo.model.DateRangeRule;
import com.example.demo.model.Reservation;
import com.example.demo.model.User;
import com.example.demo.repository.ApartmentRepository;
import com.example.demo.repository.DateRangeRuleRepository;
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
import java.time.temporal.ChronoUnit;
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
    private final DateRangeRuleRepository dateRangeRuleRepository;

    @Autowired
    private EmailService emailService;

    @Override
    public List<Long> findBookedApartmentIds(LocalDate startDate, LocalDate endDate) {
        return reservationRepository.findBookedApartmentIds(startDate, endDate);
    }

    @Override
    public List<BookedDateRangeDTO> getBookedDateRanges(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment not found: " + apartmentId));
        int gapDays = apartment.getGapDays() != null ? apartment.getGapDays() : 0;

        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING);
        return reservationRepository.findByApartment_IdAndStatusIn(apartmentId, activeStatuses).stream()
                .map(r -> new BookedDateRangeDTO(
                        r.getCheckInDate(),
                        r.getCheckOutDate(),
                        r.getCheckOutDate().plusDays(gapDays)
                ))
                .toList();
    }

    @Override
    public ReservationDTO createReservation(ReservationDTO reservationDTO) {
        Long apartmentId = reservationDTO.getApartmentId();
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment not found: " + apartmentId));
        int gapDays = apartment.getGapDays() != null ? apartment.getGapDays() : 0;

        LocalDate newCheckIn = reservationDTO.getCheckInDate();
        LocalDate newCheckOut = reservationDTO.getCheckOutDate();

        List<DateRangeRule> rules = dateRangeRuleRepository
                .findByApartmentAndDateRange(apartmentId, newCheckIn, newCheckOut.minusDays(1));

        validateMinStay(rules, newCheckIn, newCheckOut);

        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING);
        List<Reservation> existingReservations = reservationRepository.findByApartment_IdAndStatusIn(apartmentId, activeStatuses);

        boolean hasConflict = existingReservations.stream().anyMatch(r -> {
            LocalDate effectiveEnd = r.getCheckOutDate().plusDays(gapDays);
            return newCheckIn.isBefore(effectiveEnd) && newCheckOut.isAfter(r.getCheckInDate());
        });

        if (hasConflict) {
            throw new ReservationConflictException(
                    "Apartment " + apartmentId + " is already booked for the selected dates."
            );
        }

        Reservation reservation = ReservationMapper.mapToReservation(reservationDTO, apartmentRepository, userRepository);
        reservation.setTotalPrice(calculateTotalPrice(apartment, rules, newCheckIn, newCheckOut, reservationDTO.getGuestCount()));

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationMapper.mapToReservationDTO(savedReservation);
    }

    @Override
    public int calculatePrice(Long apartmentId, LocalDate checkIn, LocalDate checkOut, Long guestCount) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new ApartmentNotFoundException("Apartment not found: " + apartmentId));

        List<DateRangeRule> rules = dateRangeRuleRepository
                .findByApartmentAndDateRange(apartmentId, checkIn, checkOut.minusDays(1));

        return calculateTotalPrice(apartment, rules, checkIn, checkOut, guestCount);
    }

    @Override
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream().map(ReservationMapper::mapToReservationDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReservationDTO> getReservationsByApartmentAndDateRange(Long apartmentId, LocalDate from, LocalDate to) {
        List<ReservationStatus> activeStatuses = List.of(ReservationStatus.CONFIRMED, ReservationStatus.PENDING);
        return reservationRepository.findByApartmentAndDateRange(apartmentId, from, to, activeStatuses)
                .stream()
                .map(ReservationMapper::mapToReservationDTO)
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
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.PENDING);
        reservationRepository.save(reservation);

        sendPendingReservationEmail(reservation);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) throws MessagingException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.CANCELED);
        reservationRepository.save(reservation);

        sendCancelingReservationEmails(reservation);
    }

    @Override
    @Transactional
    public void confirmReservation(Long reservationId) throws MessagingException {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + reservationId));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        sendConfirmingReservationEmail(reservation);
    }

    // Safety net: apartments that don't meet minStay for the requested dates are already
    // excluded from search results (see ApartmentServiceImpl.findAvailableApartments), so under
    // normal use a guest should never reach this point with an invalid stay. This check still
    // guards against:
    //  - the apartment detail page being left open with stale search results while an admin
    //    changes/adds a minStay rule for the selected dates in the meantime;
    //  - the guest changing checkIn/checkOut in the apartment page's own calendar after the
    //    initial availability check, faster than the page can revalidate;
    //  - direct/manual calls to the create-reservation API that bypass the search UI entirely.
    private void validateMinStay(List<DateRangeRule> rules, LocalDate checkIn, LocalDate checkOut) {
        int stayNights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        rules.stream()
                .filter(r -> r.getStatus() == RuleStatus.OPEN
                        && r.getMinStay() != null
                        && !checkIn.isBefore(r.getStartDate())
                        && !checkIn.isAfter(r.getEndDate()))
                .findFirst()
                .ifPresent(rule -> {
                    if (stayNights < rule.getMinStay()) {
                        throw new MinStayViolationException(
                                "Minimum stay for the selected dates is " + rule.getMinStay() + " night(s).",
                                rule.getMinStay());
                    }
                });
    }

    private int calculateTotalPrice(Apartment apartment, List<DateRangeRule> rules, LocalDate checkIn, LocalDate checkOut, Long guestCount) {
        int basePrice = apartment.getPrice() != null ? apartment.getPrice() : 0;

        int total = 0;
        LocalDate night = checkIn;
        while (night.isBefore(checkOut)) {
            final LocalDate current = night;
            int nightPrice = rules.stream()
                    .filter(r -> r.getStatus() == RuleStatus.OPEN
                            && !current.isBefore(r.getStartDate())
                            && !current.isAfter(r.getEndDate())
                            && r.getPriceOverride() != null)
                    .findFirst()
                    .map(r -> r.getPriceUnit() == PriceUnit.PERCENT
                            ? (int) Math.round(basePrice * r.getPriceOverride() / 100.0)
                            : r.getPriceOverride())
                    .orElse(basePrice);
            nightPrice = applyGuestPricing(apartment, nightPrice, guestCount);
            total += nightPrice;
            night = night.plusDays(1);
        }
        return total;
    }

    private int applyGuestPricing(Apartment apartment, int nightPrice, Long guestCount) {
        if (guestCount == null || apartment.getCountOfSleepPlaces() == null) {
            return nightPrice;
        }

        int standardCapacity = apartment.getCountOfSleepPlaces();

        if (guestCount > standardCapacity
                && Boolean.TRUE.equals(apartment.getGuestPriceAboveEnabled())
                && apartment.getGuestPriceAboveValue() != null) {
            int delta = apartment.getGuestPriceAboveUnit() == PriceUnit.PERCENT
                    ? (int) Math.round(nightPrice * apartment.getGuestPriceAboveValue() / 100.0)
                    : apartment.getGuestPriceAboveValue();
            return nightPrice + delta;
        }

        if (guestCount < standardCapacity
                && Boolean.TRUE.equals(apartment.getGuestPriceBelowEnabled())
                && apartment.getGuestPriceBelowValue() != null) {
            int delta = apartment.getGuestPriceBelowUnit() == PriceUnit.PERCENT
                    ? (int) Math.round(nightPrice * apartment.getGuestPriceBelowValue() / 100.0)
                    : apartment.getGuestPriceBelowValue();
            return Math.max(0, nightPrice - delta);
        }

        return nightPrice;
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
