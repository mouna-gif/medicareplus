package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.NotificationRequest;

import com.medicareplus.domain.Enums.*;
import com.medicareplus.repository.RendezVousRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReminderScheduler {
    private final RendezVousRepository rendezVous;
    private final NotificationService notifications;

    @Scheduled(cron = "${APPOINTMENT_REMINDER_CRON:0 0 * * * *}")
    @Transactional(readOnly = true)
    public void sendUpcomingReminders() {
        LocalDateTime start = LocalDateTime.now().plusHours(23);
        LocalDateTime end = LocalDateTime.now().plusHours(24);
        rendezVous.findByStatutAndDateHeureBetween(StatutRendezVous.CONFIRME, start, end).forEach(rdv -> {
            String email = rdv.getPatient().getEmail();
            if (email != null && !email.isBlank()) {
                notifications.send(new NotificationRequest(rdv.getId(), email, CanalNotification.EMAIL,
                    TypeNotification.RAPPEL_RDV, "Rappel : votre rendez-vous MediCare+ est prévu le " + rdv.getDateHeure() + "."));
            }
        });
    }
}
