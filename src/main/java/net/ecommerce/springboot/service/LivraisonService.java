package net.ecommerce.springboot.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.LivraisonDTO;
import net.ecommerce.springboot.dto.LivreurDashboardDTO;
import net.ecommerce.springboot.dto.LivreurEnginPatchDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.LivraisonRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class LivraisonService {

	private final LivraisonRepository livraisonRepository;
	private final UserRepository userRepository;

	public LivraisonService(LivraisonRepository livraisonRepository, UserRepository userRepository) {
		this.livraisonRepository = livraisonRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public Livraison createPendingForTransaction(EcomTransaction transaction) {
		Livraison l = new Livraison();
		l.setTransaction(transaction);
		l.setStatut(LivraisonStatut.EN_ATTENTE);
		l.setDatecreation(LocalDateTime.now());
		Livraison saved = livraisonRepository.save(l);
		transaction.setLivraison(saved);
		return saved;
	}

	@Transactional(readOnly = true)
	public LivreurDashboardDTO buildDashboard(User livreur) {
		assertLivreur(livreur);
		Integer id = livreur.getIduser();
		LivreurDashboardDTO d = new LivreurDashboardDTO();
		d.setLivraisonsEnCours(livraisonRepository.countByLivreur_IduserAndStatut(id, LivraisonStatut.EN_COURS));
		d.setLivraisonsLivrees(livraisonRepository.countByLivreur_IduserAndStatut(id, LivraisonStatut.LIVREE));
		d.setLivraisonsLivreesMoto(
				livraisonRepository.countByLivreur_IduserAndStatutAndTypeEnginUtilise(id, LivraisonStatut.LIVREE,
						TypeEnginLivreur.MOTO));
		d.setLivraisonsLivreesVehicule(
				livraisonRepository.countByLivreur_IduserAndStatutAndTypeEnginUtilise(id, LivraisonStatut.LIVREE,
						TypeEnginLivreur.VEHICULE));
		if (livreur.getTypeEnginLivreur() != null) {
			d.setEnginProfil(livreur.getTypeEnginLivreur().name());
		}
		List<Livraison> recent = livraisonRepository.findByLivreur_IduserOrderByDatecreationDesc(id).stream()
				.limit(25)
				.toList();
		d.setDernieresCourses(recent.stream().map(LivraisonDTO::fromEntity).toList());
		return d;
	}

	@Transactional(readOnly = true)
	public List<LivraisonDTO> listDisponibles() {
		return livraisonRepository
				.findByStatutAndLivreurIsNullOrderByDatecreationAsc(LivraisonStatut.EN_ATTENTE)
				.stream()
				.map(LivraisonDTO::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivraisonDTO> listAllForAdmin() {
		return livraisonRepository.findAll().stream().sorted((a, b) -> b.getDatecreation().compareTo(a.getDatecreation()))
				.map(LivraisonDTO::fromEntity)
				.toList();
	}

	@Transactional
	public LivraisonDTO prendreEnCharge(User livreur, Integer idlivraison, TypeEnginLivreur typeEngin) {
		assertLivreur(livreur);
		if (typeEngin == null) {
			throw new IllegalArgumentException("Le type d'engin (MOTO ou VEHICULE) est obligatoire.");
		}
		Livraison l = livraisonRepository.findById(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		if (l.getStatut() != LivraisonStatut.EN_ATTENTE || l.getLivreur() != null) {
			throw new IllegalStateException("Cette livraison n'est plus disponible.");
		}
		l.setLivreur(livreur);
		l.setStatut(LivraisonStatut.EN_COURS);
		l.setTypeEnginUtilise(typeEngin);
		l.setDatePriseEnCharge(LocalDateTime.now());
		livreur.setTypeEnginLivreur(typeEngin);
		userRepository.save(livreur);
		return LivraisonDTO.fromEntity(livraisonRepository.save(l));
	}

	@Transactional
	public LivraisonDTO terminerCourse(User livreur, Integer idlivraison) {
		assertLivreur(livreur);
		Livraison l = livraisonRepository.findById(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		if (l.getStatut() != LivraisonStatut.EN_COURS || l.getLivreur() == null
				|| !l.getLivreur().getIduser().equals(livreur.getIduser())) {
			throw new IllegalStateException("Vous ne pouvez pas clôturer cette livraison.");
		}
		l.setStatut(LivraisonStatut.LIVREE);
		l.setDateLivraison(LocalDateTime.now());
		return LivraisonDTO.fromEntity(livraisonRepository.save(l));
	}

	@Transactional
	public User updateEnginProfil(User livreur, LivreurEnginPatchDTO body) {
		assertLivreur(livreur);
		if (body.getTypeEnginLivreur() == null || body.getTypeEnginLivreur().isBlank()) {
			throw new IllegalArgumentException("typeEnginLivreur requis (MOTO ou VEHICULE).");
		}
		TypeEnginLivreur eng = TypeEnginLivreur.valueOf(body.getTypeEnginLivreur().trim().toUpperCase());
		livreur.setTypeEnginLivreur(eng);
		livreur.setUserupdate(livreur.getEmail());
		livreur.setDateupdate(LocalDateTime.now());
		return userRepository.save(livreur);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> adminStats() {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("livraisonsEnAttenteAffectation", livraisonRepository.countByStatut(LivraisonStatut.EN_ATTENTE));
		m.put("livraisonsEnCours", livraisonRepository.countByStatut(LivraisonStatut.EN_COURS));
		m.put("livraisonsLivrees", livraisonRepository.countByStatut(LivraisonStatut.LIVREE));
		m.put("livraisonsAnnulees", livraisonRepository.countByStatut(LivraisonStatut.ANNULEE));
		return m;
	}

	public static void assertLivreur(User u) {
		if (u == null || u.getRole() == null || !RoleNames.LIVREUR.equalsIgnoreCase(u.getRole().getLibrole())) {
			throw new IllegalStateException("Réservé aux comptes livreur.");
		}
	}

	@Transactional(readOnly = true)
	public boolean isAssignedLivreurForTransaction(User u, Integer idtransaction) {
		if (u == null || u.getRole() == null || !RoleNames.LIVREUR.equalsIgnoreCase(u.getRole().getLibrole())) {
			return false;
		}
		return livraisonRepository.findByTransaction_Idtransaction(idtransaction)
				.map(l -> l.getLivreur() != null && l.getLivreur().getIduser().equals(u.getIduser()))
				.orElse(false);
	}

	public static TypeEnginLivreur parseTypeEngin(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		return TypeEnginLivreur.valueOf(raw.trim().toUpperCase());
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> listLivraisonsForExport(User livreur) {
		assertLivreur(livreur);
		return livraisonRepository.findByLivreur_IduserOrderByDatecreationDesc(livreur.getIduser()).stream()
				.map(this::toExportMap)
				.collect(Collectors.toList());
	}

	private Map<String, Object> toExportMap(Livraison l) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("idlivraison", l.getIdlivraison());
		m.put("statut", l.getStatut().name());
		if (l.getTypeEnginUtilise() != null) {
			m.put("typeEnginUtilise", l.getTypeEnginUtilise().name());
		}
		m.put("datecreation", l.getDatecreation() != null ? l.getDatecreation().toString() : null);
		m.put("datePriseEnCharge", l.getDatePriseEnCharge() != null ? l.getDatePriseEnCharge().toString() : null);
		m.put("dateLivraison", l.getDateLivraison() != null ? l.getDateLivraison().toString() : null);
		EcomTransaction t = l.getTransaction();
		if (t != null) {
			m.put("idtransaction", t.getIdtransaction());
			if (t.getArticle() != null) {
				m.put("article", t.getArticle().getLibarticle());
			}
		}
		return m;
	}
}
