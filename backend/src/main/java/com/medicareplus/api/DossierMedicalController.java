package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.DossierMedicalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequestMapping("/api/dossiers") @RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
@Tag(name = "Medical records", description = "Role-scoped medical record and clinical history access")
public class DossierMedicalController {
    private final DossierMedicalService service;
    @Operation(summary = "Get a medical record", description = "Returns the medical record for an authorized patient.")
    @GetMapping("/patient/{patientId}") DossierMedicalView get(@PathVariable Long patientId) { return service.getByPatient(patientId); }
    @Operation(summary = "Update a medical record", description = "Updates antecedents, allergies, and clinical notes.")
    @PutMapping("/patient/{patientId}") DossierMedicalView update(@PathVariable Long patientId,@Valid @RequestBody DossierMedicalRequest request) { return service.update(patientId,request); }
    @Operation(summary = "Get chronological medical history", description = "Returns patient consultations ordered from most recent to oldest.")
    @GetMapping("/patient/{patientId}/historique") List<HistoriqueMedicalView> history(@PathVariable Long patientId) { return service.history(patientId); }
}
