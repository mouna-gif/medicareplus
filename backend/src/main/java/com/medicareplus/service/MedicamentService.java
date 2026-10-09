package com.medicareplus.service;

import static com.medicareplus.api.DomainDtos.*;

import com.medicareplus.domain.Medicament;
import com.medicareplus.repository.MedicamentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class MedicamentService {
    private final MedicamentRepository repository;
    @Transactional(readOnly = true) public List<MedicamentView> all() { return repository.findAll().stream().map(this::view).toList(); }
    @Transactional public MedicamentView create(MedicamentRequest request) { Medicament m = new Medicament(); apply(m, request); m.setActif(true); return view(repository.save(m)); }
    @Transactional public MedicamentView update(Long id, MedicamentRequest request) { Medicament m = repository.findByIdForUpdate(id).orElseThrow(); apply(m, request); return view(m); }
    private void apply(Medicament m, MedicamentRequest r) { m.setCode(r.code()); m.setNom(r.nom()); m.setDescription(r.description()); m.setPrix(r.prix()); m.setStock(r.stock()); m.setSeuilAlerte(r.seuilAlerte()); }
    private MedicamentView view(Medicament m) { return new MedicamentView(m.getId(),m.getCode(),m.getNom(),m.getDescription(),m.getPrix(),m.getStock(),m.getSeuilAlerte(),m.getStock()<=m.getSeuilAlerte(),m.isActif()); }
}
