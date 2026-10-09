package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.*;
import com.medicareplus.repository.*;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.data.domain.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationLogRepository logs;
    private final RendezVousRepository rendezVous;
    private final JavaMailSender mailSender;
    private final RestClient.Builder restClient;
    @Value("${app.notifications.from-email}") private String fromEmail;
    @Value("${app.notifications.sms-url}") private String smsUrl;
    @Value("${app.notifications.sms-token}") private String smsToken;

    @Async
    @Retryable(retryFor = RuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    @Transactional
    public void send(NotificationRequest request) {
        if (request.canal() == CanalNotification.EMAIL) sendEmail(request);
        else sendSms(request);
        saveLog(request, StatutEnvoi.ENVOYE, 1, null);
    }

    @Recover
    @Transactional
    public void recover(RuntimeException failure, NotificationRequest request) {
        saveLog(request, StatutEnvoi.ECHEC, 3, failure.getMessage());
    }

    @Transactional(readOnly = true)
    public Page<NotificationView> history(Pageable pageable) {
        return logs.findAllByOrderByCreatedAtDesc(pageable).map(log -> new NotificationView(
            log.getId(), log.getDestinataire(), log.getCanal(), log.getType(), log.getStatut(),
            log.getNombreTentatives(), log.getErreur(), log.getDateEnvoi()
        ));
    }

    private void sendEmail(NotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail); message.setTo(request.destinataire());
        message.setSubject(subject(request.type())); message.setText(request.contenu());
        mailSender.send(message);
    }

    private void sendSms(NotificationRequest request) {
        if (smsUrl == null || smsUrl.isBlank()) throw new IllegalStateException("SMS_API_URL is not configured");
        restClient.build().post().uri(smsUrl)
            .header("Authorization", "Bearer " + smsToken)
            .body(Map.of("to", request.destinataire(), "message", request.contenu()))
            .retrieve().toBodilessEntity();
    }

    private void saveLog(NotificationRequest request, StatutEnvoi status, int attempts, String error) {
        NotificationLog log = new NotificationLog();
        if (request.rendezVousId()!=null) log.setRendezVous(rendezVous.findById(request.rendezVousId()).orElseThrow());
        log.setDestinataire(request.destinataire()); log.setCanal(request.canal()); log.setType(request.type());
        log.setContenu(request.contenu()); log.setStatut(status); log.setNombreTentatives(attempts);
        log.setErreur(error == null ? null : error.substring(0, Math.min(error.length(), 1000)));
        log.setDateEnvoi(status == StatutEnvoi.ENVOYE ? LocalDateTime.now() : null);
        logs.save(log);
    }

    private String subject(TypeNotification type) {
        return switch (type) {
            case RAPPEL_RDV -> "Rappel de votre rendez-vous MediCare+";
            case NOUVEAU_RDV -> "Nouveau rendez-vous MediCare+";
            case CONFIRMATION -> "Confirmation de rendez-vous MediCare+";
            case ANNULATION -> "Annulation de rendez-vous MediCare+";
        };
    }
}
