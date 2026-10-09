package com.medicareplus.api;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController @RequiredArgsConstructor
@Tag(name = "Billing", description = "Invoice calculation and transactional payment registration")
public class BillingController {
    private final FactureService factures;
    private final PaiementService paiements;
    @Operation(summary = "Create an invoice", description = "Creates one invoice for a completed appointment and calculates medication, discount, VAT, and total amounts.", responses = @ApiResponse(responseCode = "201", description = "Invoice created"))
    @PostMapping("/api/factures") @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    ResponseEntity<FactureView> invoice(@Valid @RequestBody FactureRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(factures.create(request)); }
    @Operation(summary = "Register a payment", description = "Locks the invoice, validates the remaining balance, and updates payment status.", responses = @ApiResponse(responseCode = "201", description = "Payment registered"))
    @PostMapping("/api/factures/{id}/paiements") @PreAuthorize("hasAnyRole('ADMIN','SECRETAIRE')")
    ResponseEntity<PaiementView> payment(@PathVariable Long id,@Valid @RequestBody PaiementRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(paiements.register(id,request)); }
}
