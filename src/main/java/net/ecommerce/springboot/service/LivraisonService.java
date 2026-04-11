package net.ecommerce.springboot.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.ClientLivraisonQrDTO;
import net.ecommerce.springboot.dto.CommandeSuiviDTO;
import net.ecommerce.springboot.dto.LivraisonDTO;
import net.ecommerce.springboot.dto.LivraisonLivreurDTO;
import net.ecommerce.springboot.dto.LivraisonPositionDTO;
import net.ecommerce.springboot.dto.LivreurDashboardDTO;
import net.ecommerce.springboot.dto.LivreurEnginPatchDTO;
import net.ecommerce.springboot.dto.LivreurMesLivraisonsDTO;
import net.ecommerce.springboot.dto.LivreurNavigationDTO;
import net.ecommerce.springboot.dto.SuiviEtapeDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.LivraisonStatut;
import net.ecommerce.springboot.model.LivreurLivraisonIgnore;
import net.ecommerce.springboot.model.TypeEnginLivreur;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.LivraisonRepository;
import net.ecommerce.springboot.repository.LivreurLivraisonIgnoreRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;
import net.ecommerce.springboot.util.DeliveryTokens;
import net.ecommerce.springboot.util.NavigationUrls;
import net.ecommerce.springboot.util.VendorOrderReferenceCodec;

@Service
public class LivraisonService {

	private static final int LIVREUR_POSITION_MAX_AGE_MINUTES = 45;
	private static final int MES_LIVRAISONS_PAGE_SIZE = 200;

	private final LivraisonRepository livraisonRepository;
	private final UserRepository userRepository;
	private final LivreurLivraisonIgnoreRepository livreurLivraisonIgnoreRepository;
	private final EcomTransactionRepository ecomTransactionRepository;
	private final QrPngService qrPngService;

	public LivraisonService(LivraisonRepository livraisonRepository, UserRepository userRepository,
			LivreurLivraisonIgnoreRepository livreurLivraisonIgnoreRepository,
			EcomTransactionRepository ecomTransactionRepository, QrPngService qrPngService) {
		this.livraisonRepository = livraisonRepository;
		this.userRepository = userRepository;
		this.livreurLivraisonIgnoreRepository = livreurLivraisonIgnoreRepository;
		this.ecomTransactionRepository = ecomTransactionRepository;
		this.qrPngService = qrPngService;
	}

	@Transactional
	public Livraison createPendingForTransaction(EcomTransaction transaction) {
		Livraison l = new Livraison();
		l.setTransaction(transaction);
		l.setStatut(LivraisonStatut.EN_ATTENTE);
		l.setDatecreation(LocalDateTime.now());
		l.setClientDeliveryToken(DeliveryTokens.newClientDeliveryToken());
		l.setVendorPickupCode(newUniqueVendorPickupCode());
		Livraison saved = livraisonRepository.save(l);
		transaction.setLivraison(saved);
		return saved;
	}

	private String newUniqueVendorPickupCode() {
		for (int i = 0; i < 40; i++) {
			String c = DeliveryTokens.newVendorPickupCode();
			if (!livraisonRepository.existsByVendorPickupCode(c)) {
				return c;
			}
		}
		throw new IllegalStateException("Impossible de générer un code vendeur unique.");
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
		d.setDernieresCourses(recent.stream().map(l -> toLivreurDtoWithNav(l, livreur)).toList());
		return d;
	}

	/**
	 * Courses en cours et terminées (livrées / annulées) pour l’historique livreur. Au plus 200 entrées les plus
	 * récentes par date de création de la livraison.
	 */
	@Transactional(readOnly = true)
	public LivreurMesLivraisonsDTO listMesLivraisonsPartitionnees(User livreur) {
		assertLivreur(livreur);
		List<Livraison> rows = livraisonRepository.findTop200ByLivreur_IduserOrderByDatecreationDesc(livreur.getIduser());
		List<LivraisonLivreurDTO> enCours = new ArrayList<>();
		List<LivraisonLivreurDTO> terminees = new ArrayList<>();
		for (Livraison l : rows) {
			LivraisonLivreurDTO d = toLivreurDtoWithNav(l, livreur);
			if (l.getStatut() == LivraisonStatut.EN_COURS) {
				enCours.add(d);
			} else if (l.getStatut() == LivraisonStatut.LIVREE || l.getStatut() == LivraisonStatut.ANNULEE) {
				terminees.add(d);
			}
		}
		terminees.sort(Comparator
				.comparing(LivraisonLivreurDTO::getDateLivraison, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(LivraisonLivreurDTO::getDatecreation, Comparator.nullsLast(Comparator.reverseOrder())));
		LivreurMesLivraisonsDTO out = new LivreurMesLivraisonsDTO();
		out.setEnCours(enCours);
		out.setTerminees(terminees);
		out.setLimiteChargee(MES_LIVRAISONS_PAGE_SIZE);
		return out;
	}

	@Transactional(readOnly = true)
	public List<LivraisonLivreurDTO> listDisponiblesPourLivreur(User livreur) {
		assertLivreur(livreur);
		Set<Integer> masquees = livreurLivraisonIgnoreRepository.findLivraisonIdsIgnoredByLivreur(livreur.getIduser());
		return livraisonRepository
				.findEnAttenteSansLivreurAvecDetails(LivraisonStatut.EN_ATTENTE)
				.stream()
				.filter(l -> !masquees.contains(l.getIdlivraison()))
				.map(LivraisonLivreurDTO::fromEntity)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<LivraisonDTO> listAllForAdmin() {
		return livraisonRepository.findAll().stream().sorted((a, b) -> b.getDatecreation().compareTo(a.getDatecreation()))
				.map(LivraisonDTO::fromEntity)
				.toList();
	}

	@Transactional
	public LivraisonLivreurDTO prendreEnCharge(User livreur, Integer idlivraison, TypeEnginLivreur typeEngin) {
		assertLivreur(livreur);
		if (typeEngin == null) {
			throw new IllegalArgumentException("Le type d'engin (MOTO ou VEHICULE) est obligatoire.");
		}
		Livraison l = livraisonRepository.findByIdForUpdate(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		if (l.getStatut() != LivraisonStatut.EN_ATTENTE || l.getLivreur() != null) {
			throw new IllegalStateException("Cette livraison n'est plus disponible (déjà prise par un autre livreur).");
		}
		l.setLivreur(livreur);
		l.setStatut(LivraisonStatut.EN_COURS);
		l.setTypeEnginUtilise(typeEngin);
		l.setDatePriseEnCharge(LocalDateTime.now());
		livreur.setTypeEnginLivreur(typeEngin);
		userRepository.save(livreur);
		l = livraisonRepository.save(l);
		return toLivreurDtoWithNav(l, livreur);
	}

	@Transactional
	public void ignorerOffre(User livreur, Integer idlivraison) {
		assertLivreur(livreur);
		Livraison l = livraisonRepository.findById(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		if (l.getStatut() != LivraisonStatut.EN_ATTENTE || l.getLivreur() != null) {
			throw new IllegalStateException("Cette livraison n'est plus en attente libre.");
		}
		if (livreurLivraisonIgnoreRepository.existsByLivreur_IduserAndLivraison_Idlivraison(livreur.getIduser(),
				idlivraison)) {
			return;
		}
		LivreurLivraisonIgnore row = new LivreurLivraisonIgnore();
		row.setLivreur(livreur);
		row.setLivraison(l);
		livreurLivraisonIgnoreRepository.save(row);
	}

	/**
	 * Finalisation uniquement après scan du QR présenté par le client (contenu signé par id + jeton secret).
	 */
	/**
	 * Enregistre la position GPS du livreur pour une course en cours (affichée au client dans le lien Maps).
	 */
	@Transactional
	public void publierPositionLivreur(User livreur, Integer idlivraison, LivraisonPositionDTO body) {
		assertLivreur(livreur);
		if (body == null || body.getLatitude() == null || body.getLongitude() == null) {
			throw new IllegalArgumentException("latitude et longitude requises.");
		}
		Livraison l = livraisonRepository.findByIdForUpdate(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		if (l.getStatut() != LivraisonStatut.EN_COURS || l.getLivreur() == null
				|| !l.getLivreur().getIduser().equals(livreur.getIduser())) {
			throw new IllegalStateException("Position réservée à votre livraison en cours.");
		}
		l.setLivreurLastLatitude(body.getLatitude());
		l.setLivreurLastLongitude(body.getLongitude());
		l.setLivreurPositionAt(LocalDateTime.now());
		livraisonRepository.save(l);
	}

	@Transactional
	public LivraisonLivreurDTO terminerParScanQrClient(User livreur, String rawQr) {
		assertLivreur(livreur);
		if (rawQr == null || rawQr.isBlank()) {
			throw new IllegalArgumentException("Scan du QR client obligatoire pour terminer la livraison.");
		}
		ParsedClientQr parsed = parseClientQrPayload(rawQr.trim());
		Livraison l = livraisonRepository.findByIdForUpdate(parsed.idlivraison())
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + parsed.idlivraison()));
		byte[] expected = parsed.token().getBytes(StandardCharsets.UTF_8);
		byte[] actual = l.getClientDeliveryToken() != null
				? l.getClientDeliveryToken().getBytes(StandardCharsets.UTF_8)
				: new byte[0];
		if (l.getClientDeliveryToken() == null || expected.length != actual.length
				|| !MessageDigest.isEqual(expected, actual)) {
			throw new IllegalStateException("QR client invalide.");
		}
		if (l.getStatut() != LivraisonStatut.EN_COURS || l.getLivreur() == null
				|| !l.getLivreur().getIduser().equals(livreur.getIduser())) {
			throw new IllegalStateException("Cette livraison n'est pas en cours pour vous.");
		}
		l.setStatut(LivraisonStatut.LIVREE);
		l.setDateLivraison(LocalDateTime.now());
		l = livraisonRepository.save(l);
		return LivraisonLivreurDTO.fromEntity(l);
	}

	/** Même logique que l’API GET /payments/{id}/livraison/qr (QR affiché dans Mes achats). */
	@Transactional
	public ClientLivraisonQrDTO buildClientQrPackForBuyer(User buyer, Integer idtransaction) {
		EcomTransaction t = ecomTransactionRepository.findByIdWithLivraisonAndLivreur(idtransaction)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable : " + idtransaction));
		if (t.getAcheteur() == null || !t.getAcheteur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Accès non autorisé à cette commande.");
		}
		Livraison l = t.getLivraison();
		if (l == null) {
			throw new IllegalStateException("Aucune livraison associée à cette commande.");
		}
		if (l.getStatut() == LivraisonStatut.LIVREE) {
			throw new IllegalStateException("Livraison effectuée : le QR n'est plus disponible.");
		}
		if (l.getStatut() == LivraisonStatut.ANNULEE) {
			throw new IllegalStateException("Cette livraison a été annulée.");
		}
		if (l.getClientDeliveryToken() == null || l.getClientDeliveryToken().isBlank()) {
			l.setClientDeliveryToken(DeliveryTokens.newClientDeliveryToken());
			livraisonRepository.save(l);
		}
		String payload = l.buildClientQrPayload();
		if (payload == null) {
			throw new IllegalStateException("QR indisponible pour cette livraison.");
		}
		ClientLivraisonQrDTO dto = new ClientLivraisonQrDTO();
		dto.setIdtransaction(t.getIdtransaction());
		dto.setIdlivraison(l.getIdlivraison());
		if (t.getArticle() != null) {
			dto.setArticleLibelle(t.getArticle().getLibarticle());
		}
		dto.setQuantite(t.getQuantite());
		dto.setQrPayload(payload);
		dto.setQrImagePngBase64(qrPngService.encodeQrAsPngBase64(payload));
		dto.setMessage("Présentez ce QR au livreur à la réception pour valider la livraison.");
		return dto;
	}

	@Transactional(readOnly = true)
	public CommandeSuiviDTO buildBuyerOrderTracking(User buyer, Integer idtransaction) {
		EcomTransaction t = ecomTransactionRepository.findByIdWithLivraisonAndLivreur(idtransaction)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction introuvable : " + idtransaction));
		if (t.getAcheteur() == null || !t.getAcheteur().getIduser().equals(buyer.getIduser())) {
			throw new IllegalStateException("Accès non autorisé à cette commande.");
		}
		Livraison l = t.getLivraison();
		if (l == null) {
			throw new IllegalStateException("Aucune livraison associée.");
		}
		return fillCommandeSuivi(t, l, false);
	}

	@Transactional(readOnly = true)
	public CommandeSuiviDTO buildStaffOrderTrackingByLivraisonId(Integer idlivraison) {
		Livraison l = livraisonRepository.findById(idlivraison)
				.orElseThrow(() -> new ResourceNotFoundException("Livraison introuvable : " + idlivraison));
		EcomTransaction t = l.getTransaction();
		if (t == null) {
			throw new IllegalStateException("Transaction liée introuvable.");
		}
		return fillCommandeSuivi(t, l, true);
	}

	private CommandeSuiviDTO fillCommandeSuivi(EcomTransaction t, Livraison l, boolean includeVendorSecrets) {
		CommandeSuiviDTO d = new CommandeSuiviDTO();
		d.setIdtransaction(t.getIdtransaction());
		d.setIdlivraison(l.getIdlivraison());
		d.setStatutLivraison(l.getStatut().name());
		if (t.getArticle() != null) {
			d.setArticleLibelle(t.getArticle().getLibarticle());
		}
		d.setQuantite(t.getQuantite());
		d.setMontantTotal(t.getMontantTotal());

		List<SuiviEtapeDTO> etapes = new ArrayList<>();
		etapes.add(new SuiviEtapeDTO("PAYE", "Paiement validé",
				t.getDatecreation() != null ? t.getDatecreation().toString() : null));
		etapes.add(new SuiviEtapeDTO("ATTENTE_LIVREUR", "En attente d’un livreur",
				l.getDatecreation() != null ? l.getDatecreation().toString() : null));
		if (l.getDatePriseEnCharge() != null) {
			etapes.add(new SuiviEtapeDTO("EN_COURS", "Livreur en route / en livraison", l.getDatePriseEnCharge().toString()));
		}
		if (l.getStatut() == LivraisonStatut.LIVREE && l.getDateLivraison() != null) {
			etapes.add(new SuiviEtapeDTO("LIVREE", "Livrée (QR validé)", l.getDateLivraison().toString()));
		}
		d.setEtapes(etapes);

		boolean assigne = l.getLivreur() != null;
		d.setLivreurAssigne(assigne);
		if (l.getLivreur() != null) {
			d.setLivreurPrenom(l.getLivreur().getPrenom());
			d.setLivreurNom(l.getLivreur().getNom());
		}

		User vendeur = t.getVendeur();
		User acheteur = t.getAcheteur();
		if (vendeur != null) {
			d.setLienRetraitChezVendeur(placeUrl(vendeur));
		}
		if (acheteur != null) {
			d.setLienLivraisonChezClient(buyerDeliveryPlaceUrl(t, acheteur));
		}
		if (vendeur != null && acheteur != null) {
			Double dLat = buyerDestLat(t, acheteur);
			Double dLng = buyerDestLng(t, acheteur);
			d.setLienTrajetVendeurVersClient(NavigationUrls.googleMapsDir(null, null, vendeur.getLatitude(),
					vendeur.getLongitude(), dLat, dLng));
		}

		if (assigne && l.getStatut() == LivraisonStatut.EN_COURS && l.getLivreur() != null && acheteur != null) {
			LocalDateTime now = LocalDateTime.now();
			Double oLat = null;
			Double oLng = null;
			boolean fromSharedPosition = false;
			if (l.getLivreurLastLatitude() != null && l.getLivreurLastLongitude() != null
					&& l.getLivreurPositionAt() != null
					&& !l.getLivreurPositionAt().isBefore(now.minusMinutes(LIVREUR_POSITION_MAX_AGE_MINUTES))) {
				oLat = l.getLivreurLastLatitude();
				oLng = l.getLivreurLastLongitude();
				fromSharedPosition = true;
			} else if (l.getLivreur().getLatitude() != null && l.getLivreur().getLongitude() != null) {
				oLat = l.getLivreur().getLatitude();
				oLng = l.getLivreur().getLongitude();
			}
			if (oLat != null && oLng != null) {
				String lien = null;
				Double dLat = buyerDestLat(t, acheteur);
				Double dLng = buyerDestLng(t, acheteur);
				if (dLat != null && dLng != null) {
					lien = NavigationUrls.googleMapsDir(oLat, oLng, null, null, dLat, dLng);
				} else {
					lien = NavigationUrls.googleMapsDirToPlace(oLat, oLng, acheteur.getVille());
				}
				if (lien != null && !lien.isBlank()) {
					d.setLienLivreurVersClient(lien);
					if (fromSharedPosition && l.getLivreurPositionAt() != null) {
						d.setLivreurPositionMiseAJourAt(l.getLivreurPositionAt().toString());
					}
				}
			}
		}

		// Acheteur : une fois la livraison clôturée (QR scanné → LIVREE, ou annulée), ne plus exposer aucun lien Maps
		// (fin du suivi temps réel et des itinéraires côté client).
		boolean termineePourAcheteur = !includeVendorSecrets && (l.getStatut() == LivraisonStatut.LIVREE
				|| l.getStatut() == LivraisonStatut.ANNULEE);
		if (termineePourAcheteur) {
			d.setLienRetraitChezVendeur(null);
			d.setLienLivraisonChezClient(null);
			d.setLienTrajetVendeurVersClient(null);
			d.setLienLivreurVersClient(null);
			d.setLivreurPositionMiseAJourAt(null);
			d.setNavigationDisponible(false);
		} else {
			// Toujours proposer les liens Maps (retrait / livraison / trajet) dès qu’ils sont calculables,
			// y compris en attente de livreur — sinon l’acheteur ne voit aucune carte.
			boolean hasCarte = (d.getLienRetraitChezVendeur() != null && !d.getLienRetraitChezVendeur().isBlank())
					|| (d.getLienLivraisonChezClient() != null && !d.getLienLivraisonChezClient().isBlank())
					|| (d.getLienTrajetVendeurVersClient() != null && !d.getLienTrajetVendeurVersClient().isBlank())
					|| (d.getLienLivreurVersClient() != null && !d.getLienLivreurVersClient().isBlank());
			d.setNavigationDisponible(hasCarte);
		}

		if (includeVendorSecrets && l.getVendorPickupCode() != null) {
			d.setVendorPickupCode(l.getVendorPickupCode());
			d.setVendorPackedReferenceBase64(VendorOrderReferenceCodec.encode(t, l, l.getVendorPickupCode()));
		}

		return d;
	}

	private static String placeUrl(User u) {
		if (u == null) {
			return null;
		}
		if (u.getLatitude() != null && u.getLongitude() != null) {
			return NavigationUrls.googleMapsSearchLatLng(u.getLatitude(), u.getLongitude());
		}
		return NavigationUrls.googleMapsSearchQuery(u.getVille());
	}

	private static Double buyerDestLat(EcomTransaction t, User acheteur) {
		if (t != null && t.getLivraisonLatitude() != null) {
			return t.getLivraisonLatitude();
		}
		return acheteur != null ? acheteur.getLatitude() : null;
	}

	private static Double buyerDestLng(EcomTransaction t, User acheteur) {
		if (t != null && t.getLivraisonLongitude() != null) {
			return t.getLivraisonLongitude();
		}
		return acheteur != null ? acheteur.getLongitude() : null;
	}

	/** Lien Maps vers le point de dépôt (commande ou domicile). */
	private static String buyerDeliveryPlaceUrl(EcomTransaction t, User acheteur) {
		Double la = buyerDestLat(t, acheteur);
		Double lo = buyerDestLng(t, acheteur);
		if (la != null && lo != null) {
			return NavigationUrls.googleMapsSearchLatLng(la, lo);
		}
		return placeUrl(acheteur);
	}

	private LivraisonLivreurDTO toLivreurDtoWithNav(Livraison l, User livreurConnecte) {
		LivraisonLivreurDTO d = LivraisonLivreurDTO.fromEntity(l);
		if (livreurConnecte != null && l.getStatut() == LivraisonStatut.EN_COURS && l.getLivreur() != null
				&& l.getLivreur().getIduser().equals(livreurConnecte.getIduser())) {
			EcomTransaction t = l.getTransaction();
			if (t != null && t.getVendeur() != null && t.getAcheteur() != null) {
				d.setNavigation(buildLivreurNavigation(livreurConnecte, t.getVendeur(), t.getAcheteur(), t));
			}
		}
		return d;
	}

	private LivreurNavigationDTO buildLivreurNavigation(User livreur, User vendeur, User acheteur,
			EcomTransaction t) {
		LivreurNavigationDTO n = new LivreurNavigationDTO();
		n.setEtapeRetraitVendeur(placeUrl(vendeur));
		n.setEtapeDepotClient(buyerDeliveryPlaceUrl(t, acheteur));
		Double dLat = buyerDestLat(t, acheteur);
		Double dLng = buyerDestLng(t, acheteur);
		n.setItineraireComplet(NavigationUrls.googleMapsDir(livreur.getLatitude(), livreur.getLongitude(),
				vendeur.getLatitude(), vendeur.getLongitude(), dLat, dLng));
		return n;
	}

	private record ParsedClientQr(int idlivraison, String token) {
	}

	private static ParsedClientQr parseClientQrPayload(String raw) {
		if (!raw.startsWith("ECOM;")) {
			throw new IllegalArgumentException("Format de QR non reconnu (attendu ECOM;…).");
		}
		String[] p = raw.split(";", 3);
		if (p.length != 3 || p[1].isBlank() || p[2].isBlank()) {
			throw new IllegalArgumentException("QR incomplet.");
		}
		int idlivraison;
		try {
			idlivraison = Integer.parseInt(p[1].trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Identifiant livraison invalide dans le QR.");
		}
		return new ParsedClientQr(idlivraison, p[2].trim());
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
