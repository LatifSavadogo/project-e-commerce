package net.ecommerce.springboot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import net.ecommerce.springboot.dto.ComplaintDTO;
import net.ecommerce.springboot.dto.ComplaintReadPatchDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Complaint;
import net.ecommerce.springboot.repository.ComplaintRepository;

@RestController
@RequestMapping("/api/v1/admin/complaints")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminComplaintController {

	private final ComplaintRepository complaintRepository;

	public AdminComplaintController(ComplaintRepository complaintRepository) {
		this.complaintRepository = complaintRepository;
	}

	@GetMapping
	public ResponseEntity<List<ComplaintDTO>> listAll() {
		return ResponseEntity.ok(complaintRepository.findAllByOrderByDatecreationDesc().stream()
				.map(ComplaintDTO::fromEntity)
				.toList());
	}

	@PatchMapping("/{id}/lu")
	public ResponseEntity<ComplaintDTO> patchLu(@PathVariable Integer id, @Valid @RequestBody ComplaintReadPatchDTO body) {
		Complaint c = complaintRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plainte introuvable : " + id));
		c.setLu(body.isLu());
		return ResponseEntity.ok(ComplaintDTO.fromEntity(complaintRepository.save(c)));
	}
}
