package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.Enums.StatutRendezVous;
import com.medicareplus.repository.*;
import java.math.*;
import java.time.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatistiqueService {
    private final PatientRepository patients;
    private final RendezVousRepository rendezVous;
    private final FactureRepository factures;
    private final LignePrescriptionRepository lignes;

    @Transactional(readOnly = true)
    public StatistiqueView dashboard() {
        Instant monthStart = YearMonth.now().atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long totalRdv = rendezVous.count();
        long absent = rendezVous.countByStatut(StatutRendezVous.ABSENT);
        BigDecimal absenteeism = totalRdv == 0 ? BigDecimal.ZERO :
            BigDecimal.valueOf(absent).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalRdv), 2, RoundingMode.HALF_UP);
        List<MedicamentPrescriptionStat> top = lignes.mostPrescribed(PageRequest.of(0, 5)).stream()
            .map(row -> new MedicamentPrescriptionStat((String) row[0], ((Number) row[1]).longValue())).toList();
        return new StatistiqueView(patients.countCreatedSince(monthStart), totalRdv, factures.totalRevenue(), absenteeism, top);
    }
}
