package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.*;
import com.medicareplus.domain.Enums.StatutPaiement;
import com.medicareplus.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class PaiementService {
    private final FactureRepository factures;
    private final PaiementRepository paiements;

    @Transactional
    public PaiementView register(Long factureId, PaiementRequest request) {
        Facture facture = factures.findByIdForUpdate(factureId).orElseThrow();
        BigDecimal dejaPaye = facture.getPaiements().stream().map(Paiement::getMontant).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal restant = facture.getTotal().subtract(dejaPaye);
        if (request.montant().compareTo(restant) > 0) throw new IllegalArgumentException("Le paiement dépasse le solde restant");
        Paiement paiement = new Paiement();
        paiement.setFacture(facture); paiement.setMontant(request.montant()); paiement.setMode(request.mode());
        paiement.setReferencePaiement(request.referencePaiement()); paiement.setDatePaiement(LocalDateTime.now());
        paiements.save(paiement);
        BigDecimal nouveauSolde = restant.subtract(request.montant());
        facture.setStatut(nouveauSolde.signum() == 0 ? StatutPaiement.PAYEE : StatutPaiement.PARTIELLEMENT_PAYEE);
        return new PaiementView(paiement.getId(), factureId, paiement.getMontant(), paiement.getMode(),
            paiement.getReferencePaiement(), paiement.getDatePaiement(), nouveauSolde, facture.getStatut());
    }
}
