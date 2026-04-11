package net.ecommerce.springboot.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.model.Article;
import net.ecommerce.springboot.model.ChatMessage;
import net.ecommerce.springboot.model.Complaint;
import net.ecommerce.springboot.model.Conversation;
import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ChatMessageRepository;
import net.ecommerce.springboot.repository.ComplaintRepository;
import net.ecommerce.springboot.repository.ConversationRepository;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class GdprExportService {

	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final EcomTransactionRepository transactionRepository;
	private final ComplaintRepository complaintRepository;
	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final LivraisonService livraisonService;

	public GdprExportService(UserRepository userRepository, ArticleRepository articleRepository,
			EcomTransactionRepository transactionRepository, ComplaintRepository complaintRepository,
			ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
			LivraisonService livraisonService) {
		this.userRepository = userRepository;
		this.articleRepository = articleRepository;
		this.transactionRepository = transactionRepository;
		this.complaintRepository = complaintRepository;
		this.conversationRepository = conversationRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.livraisonService = livraisonService;
	}

	@Transactional(readOnly = true)
	public Map<String, Object> buildExport(User userRef) {
		User u = userRepository.findById(userRef.getIduser())
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
		Map<String, Object> root = new LinkedHashMap<>();
		root.put("exportedAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
		root.put("notice",
				"Export RGPD : données liées à votre compte. Les références de paiement externes restent chiffrées côté serveur et ne sont pas incluses en clair.");
		root.put("profil", mapProfil(u));
		root.put("articlesPublies", mapArticles(articleRepository.findByVendeur_IduserOrderByDateupdateDesc(u.getIduser())));
		root.put("achats", mapTransactions(transactionRepository.findByAcheteur_IduserOrderByDatecreationDesc(u.getIduser()),
				"ACHETEUR"));
		root.put("ventes", mapTransactions(transactionRepository.findByVendeur_IduserOrderByDatecreationDesc(u.getIduser()),
				"VENDEUR"));
		root.put("plaintesDeposees", mapPlaintes(complaintRepository.findByAuteur_IduserOrderByDatecreationDesc(u.getIduser())));
		root.put("conversations", mapConversations(
				conversationRepository.findByAcheteur_IduserOrVendeur_IduserOrderByDateupdateDesc(u.getIduser(), u.getIduser()),
				u.getIduser()));
		if (u.getRole() != null && RoleNames.LIVREUR.equalsIgnoreCase(u.getRole().getLibrole())) {
			root.put("livraisonsEffectuees", livraisonService.listLivraisonsForExport(u));
		}
		return root;
	}

	private static Map<String, Object> mapProfil(User u) {
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("iduser", u.getIduser());
		m.put("email", u.getEmail());
		m.put("nom", u.getNom());
		m.put("prenom", u.getPrenom());
		if (u.getRole() != null) {
			m.put("role", u.getRole().getLibrole());
		}
		if (u.getPays() != null) {
			m.put("pays", u.getPays().getLibpays());
		}
		if (u.getVille() != null && !u.getVille().isBlank()) {
			m.put("ville", u.getVille());
		}
		if (u.getCategorieVendeur() != null) {
			m.put("categorieVendeur", u.getCategorieVendeur().getLibtype());
		}
		if (u.getTypeEnginLivreur() != null) {
			m.put("typeEnginLivreur", u.getTypeEnginLivreur().name());
		}
		m.put("fichierPieceIdentite", u.getCnib());
		m.put("fichierPhotoProfil", u.getPhotoProfil());
		if (u.getLatitude() != null) {
			m.put("latitude", u.getLatitude());
		}
		if (u.getLongitude() != null) {
			m.put("longitude", u.getLongitude());
		}
		m.put("dateMiseAJour", u.getDateupdate() != null ? u.getDateupdate().toString() : null);
		return m;
	}

	private static List<Map<String, Object>> mapArticles(List<Article> list) {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Article a : list) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("idarticle", a.getIdarticle());
			m.put("libelle", a.getLibarticle());
			m.put("prixunitaire", a.getPrixunitaire());
			m.put("bloque", a.isBlocked());
			m.put("avertissement", a.getWarningMessage());
			m.put("vues", a.getViewCount());
			m.put("dateMiseAJour", a.getDateupdate() != null ? a.getDateupdate().toString() : null);
			out.add(m);
		}
		return out;
	}

	private static List<Map<String, Object>> mapTransactions(List<EcomTransaction> list, String sens) {
		List<Map<String, Object>> out = new ArrayList<>();
		for (EcomTransaction t : list) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("idtransaction", t.getIdtransaction());
			m.put("sens", sens);
			m.put("datecreation", t.getDatecreation() != null ? t.getDatecreation().toString() : null);
			m.put("quantite", t.getQuantite());
			m.put("prixUnitaireSnapshot", t.getPrixUnitaireSnapshot());
			m.put("montantTotal", t.getMontantTotal());
			m.put("fraisAffiches", t.getFraisAffiches());
			m.put("moyenPaiement", t.getMoyenPaiement() != null ? t.getMoyenPaiement().name() : null);
			m.put("referenceExterneEnregistree", true);
			if (t.getArticle() != null) {
				m.put("idArticle", t.getArticle().getIdarticle());
				m.put("libelleArticle", t.getArticle().getLibarticle());
			}
			out.add(m);
		}
		return out;
	}

	private static List<Map<String, Object>> mapPlaintes(List<Complaint> list) {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Complaint c : list) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("idplainte", c.getIdplainte());
			m.put("titre", c.getTitre());
			m.put("description", c.getDescription());
			m.put("lue", c.isLu());
			m.put("datecreation", c.getDatecreation() != null ? c.getDatecreation().toString() : null);
			if (c.getArticle() != null) {
				m.put("idArticle", c.getArticle().getIdarticle());
			}
			out.add(m);
		}
		return out;
	}

	private List<Map<String, Object>> mapConversations(List<Conversation> convs, Integer myId) {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Conversation c : convs) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("idconversation", c.getIdconversation());
			m.put("datecreation", c.getDatecreation() != null ? c.getDatecreation().toString() : null);
			m.put("dateMiseAJour", c.getDateupdate() != null ? c.getDateupdate().toString() : null);
			boolean iAmBuyer = myId.equals(c.getAcheteur().getIduser());
			m.put("monRole", iAmBuyer ? "ACHETEUR" : "VENDEUR");
			User peer = iAmBuyer ? c.getVendeur() : c.getAcheteur();
			m.put("interlocuteurEmail", peer.getEmail());
			m.put("interlocuteurNom", peer.getNom() + " " + peer.getPrenom());
			if (c.getArticle() != null) {
				m.put("idArticle", c.getArticle().getIdarticle());
				m.put("libelleArticle", c.getArticle().getLibarticle());
			}
			List<ChatMessage> msgs = chatMessageRepository.findByConversation_IdconversationOrderByDateenvoiAsc(
					c.getIdconversation());
			List<Map<String, Object>> msgMaps = new ArrayList<>();
			for (ChatMessage msg : msgs) {
				Map<String, Object> mm = new LinkedHashMap<>();
				mm.put("dateenvoi", msg.getDateenvoi() != null ? msg.getDateenvoi().toString() : null);
				mm.put("auteurEstMoi", msg.getAuteur() != null && myId.equals(msg.getAuteur().getIduser()));
				mm.put("contenu", msg.getContenu());
				if (msg.getPrixPropose() != null) {
					mm.put("prixPropose", msg.getPrixPropose());
				}
				if (msg.getQuantiteProposee() != null) {
					mm.put("quantiteProposee", msg.getQuantiteProposee());
				}
				if (msg.getStatutOffre() != null) {
					mm.put("statutOffre", msg.getStatutOffre().name());
				}
				msgMaps.add(mm);
			}
			m.put("messages", msgMaps);
			out.add(m);
		}
		return out;
	}
}
