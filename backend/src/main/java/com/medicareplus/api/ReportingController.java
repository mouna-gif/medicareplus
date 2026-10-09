package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.StatistiqueView;

import com.medicareplus.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Statistics and reports", description = "Administrator-only operational analytics and exports")
public class ReportingController {
    private final StatistiqueService statistiques;
    private final RapportService rapports;
    @Operation(summary = "Get administrator statistics", description = "Returns persisted patient, appointment, revenue, absenteeism, and prescription indicators.")
    @GetMapping("/api/statistiques") StatistiqueView statistics() { return statistiques.dashboard(); }
    @Operation(summary = "Export statistical report", description = "Generates a UTF-8 CSV report from persisted statistics.")
    @GetMapping(value="/api/rapports/statistiques.csv", produces="text/csv")
    ResponseEntity<byte[]> report() {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=statistiques-medicare.csv")
            .body(rapports.statisticalCsv());
    }
}
