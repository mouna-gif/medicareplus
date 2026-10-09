package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.*;
import com.medicareplus.repository.*;
import java.math.*;
import java.time.Year;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class FactureService {
    private final FactureRepository factures;
    private final RendezVousRepository rendezVous;
    private final PrescriptionRepository prescriptions;
    @Value("${app.billing.default-consultation-amount}") private BigDecimal defaultConsultationAmount;
    @Transactional
    public FactureView create(FactureRequest request) {
        RendezVous rdv = rendezVous.findById(request.rendezVousId()).orElseThrow();
        if (rdv.getStatut() != StatutRendezVous.TERMINE) throw new IllegalArgumentException("Une facture ne peut être créée que pour un rendez-vous terminé");
        if (factures.existsByRendezVousId(rdv.getId())) throw new IllegalArgumentException("Une facture existe déjà pour ce rendez-vous");
        BigDecimal medicationAmount = prescriptions.findValidatedByRendezVousId(rdv.getId())
            .map(p -> p.getLignes().stream().map(line -> line.getMedicament().getPrix().multiply(BigDecimal.valueOf(line.getQuantite())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)).orElse(BigDecimal.ZERO);
        BigDecimal brut = defaultConsultationAmount.add(medicationAmount);
        if (request.remise().compareTo(brut.multiply(new BigDecimal("0.30"))) > 0) {
            throw new IllegalArgumentException("La remise ne peut pas dépasser 30 % du sous-total");
        }
        BigDecimal sousTotal = brut.subtract(request.remise());
        BigDecimal total = sousTotal.multiply(BigDecimal.ONE.add(request.tauxTva().movePointLeft(2))).setScale(2, RoundingMode.HALF_UP);
        Facture f = new Facture();
        f.setNumero("FAC-"+Year.now().getValue()+"-"+ThreadLocalRandom.current().nextInt(100000,999999));
        f.setRendezVous(rdv); f.setMontantConsultation(defaultConsultationAmount); f.setMontantMedicaments(medicationAmount);
        f.setRemise(request.remise()); f.setTauxTva(request.tauxTva()); f.setTotal(total); f.setStatut(StatutPaiement.EN_ATTENTE);
        factures.save(f);
        return new FactureView(f.getId(),f.getNumero(),rdv.getId(),f.getMontantConsultation(),f.getMontantMedicaments(),f.getRemise(),f.getTauxTva(),f.getTotal(),f.getStatut());
    }
}
