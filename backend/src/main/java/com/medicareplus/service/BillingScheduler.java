package com.medicareplus.service;

import com.medicareplus.domain.Enums.StatutPaiement;
import com.medicareplus.repository.FactureRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingScheduler {
    private final FactureRepository factures;
    @Value("${app.billing.unpaid-after-days}") private long unpaidAfterDays;

    @Scheduled(cron = "${INVOICE_STATUS_CRON:0 15 1 * * *}")
    @Transactional
    public void markOverdueInvoices() {
        Instant threshold = Instant.now().minus(unpaidAfterDays, ChronoUnit.DAYS);
        factures.findByStatutInAndCreatedAtBefore(
            List.of(StatutPaiement.EN_ATTENTE, StatutPaiement.PARTIELLEMENT_PAYEE), threshold
        ).forEach(facture -> facture.setStatut(StatutPaiement.NON_PAYEE));
    }
}
