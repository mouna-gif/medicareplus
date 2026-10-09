package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RapportService {
    private final StatistiqueService statistiques;

    public byte[] statisticalCsv() {
        StatistiqueView s = statistiques.dashboard();
        StringBuilder csv = new StringBuilder("indicateur,valeur\n")
            .append("patients_ce_mois,").append(s.patientsCeMois()).append('\n')
            .append("rendez_vous_total,").append(s.rendezVousTotal()).append('\n')
            .append("revenus,").append(s.revenus()).append('\n')
            .append("taux_absenteisme,").append(s.tauxAbsenteisme()).append('\n')
            .append("\nmedicament,quantite_prescrite\n");
        s.medicamentsLesPlusPrescrits().forEach(item -> csv.append('"').append(item.medicament().replace("\"","\"\""))
            .append("\",").append(item.quantite()).append('\n'));
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
