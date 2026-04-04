package net.ecommerce.springboot.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ecommerce.springboot.dto.AdminUserPatchDTO;
import net.ecommerce.springboot.exception.ResourceNotFoundException;
import net.ecommerce.springboot.model.Conversation;
import net.ecommerce.springboot.model.Role;
import net.ecommerce.springboot.model.User;
import net.ecommerce.springboot.repository.ArticleRepository;
import net.ecommerce.springboot.repository.ChatMessageRepository;
import net.ecommerce.springboot.repository.ComplaintRepository;
import net.ecommerce.springboot.repository.ConversationRepository;
import net.ecommerce.springboot.repository.EcomTransactionRepository;
import net.ecommerce.springboot.repository.LivraisonRepository;
import net.ecommerce.springboot.repository.PaysRepository;
import net.ecommerce.springboot.repository.RoleRepository;
import net.ecommerce.springboot.repository.TypeArticleRepository;
import net.ecommerce.springboot.repository.UserRepository;
import net.ecommerce.springboot.security.RoleNames;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final ArticleRepository articleRepository;
	private final ConversationRepository conversationRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ComplaintRepository complaintRepository;
	private final EcomTransactionRepository ecomTransactionRepository;
	private final LivraisonRepository livraisonRepository;
	private final RoleRepository roleRepository;
	private final PaysRepository paysRepository;
	private final TypeArticleRepository typeArticleRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, ArticleRepository articleRepository,
			ConversationRepository conversationRepository, ChatMessageRepository chatMessageRepository,
			ComplaintRepository complaintRepository, EcomTransactionRepository ecomTransactionRepository,
			LivraisonRepository livraisonRepository, RoleRepository roleRepository, PaysRepository paysRepository,
			TypeArticleRepository typeArticleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.articleRepository = articleRepository;
		this.conversationRepository = conversationRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.complaintRepository = complaintRepository;
		this.ecomTransactionRepository = ecomTransactionRepository;
		this.livraisonRepository = livraisonRepository;
		this.roleRepository = roleRepository;
		this.paysRepository = paysRepository;
		this.typeArticleRepository = typeArticleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User saveWithEncodedPassword(User user) {
		if (user.getPassword() != null && !isBcrypt(user.getPassword())) {
			user.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		return userRepository.save(user);
	}

	@Transactional
	public User resetPassword(Integer iduser, String newPassword) {
		User user = userRepository.findById(iduser)
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
		user.setPassword(passwordEncoder.encode(newPassword));
		return userRepository.save(user);
	}

	@Transactional
	public User adminPatchUser(User admin, Integer targetId, AdminUserPatchDTO dto) {
		User target = userRepository.findById(targetId)
				.orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + targetId));
		String ar = roleLib(admin);
		String tr = roleLib(target);
		boolean superActor = RoleNames.SUPER_ADMIN.equalsIgnoreCase(ar);
		boolean adminActor = RoleNames.ADMIN.equalsIgnoreCase(ar);
		if (!superActor && !adminActor) {
			throw new IllegalStateException("Action réservée aux administrateurs.");
		}
		if (!superActor && (RoleNames.ADMIN.equalsIgnoreCase(tr) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(tr))) {
			throw new IllegalStateException("Interdit : ce compte est administrateur.");
		}
		if (dto.getIdrole() != null) {
			Role newRole = roleRepository.findById(dto.getIdrole())
					.orElseThrow(() -> new ResourceNotFoundException("Rôle introuvable : " + dto.getIdrole()));
			String nr = newRole.getLibrole();
			if (!superActor && (RoleNames.ADMIN.equalsIgnoreCase(nr) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(nr))) {
				throw new IllegalStateException("Un admin ne peut pas attribuer ce rôle.");
			}
			target.setRole(newRole);
			if (!RoleNames.VENDEUR.equalsIgnoreCase(nr)) {
				target.setCategorieVendeur(null);
			}
			if (!RoleNames.LIVREUR.equalsIgnoreCase(nr)) {
				target.setTypeEnginLivreur(null);
			}
		}
		if (dto.getIdpays() != null) {
			target.setPays(paysRepository.findById(dto.getIdpays())
					.orElseThrow(() -> new ResourceNotFoundException("Pays introuvable : " + dto.getIdpays())));
		}
		if (dto.getIdtypeVendeur() != null) {
			if (target.getRole() == null || !RoleNames.VENDEUR.equalsIgnoreCase(target.getRole().getLibrole())) {
				throw new IllegalStateException("Catégorie vendeur réservée aux comptes vendeur.");
			}
			target.setCategorieVendeur(typeArticleRepository.findById(dto.getIdtypeVendeur()).orElseThrow(
					() -> new ResourceNotFoundException("Type d'article introuvable : " + dto.getIdtypeVendeur())));
		}
		if (dto.getTypeEnginLivreur() != null) {
			if (target.getRole() == null || !RoleNames.LIVREUR.equalsIgnoreCase(target.getRole().getLibrole())) {
				throw new IllegalStateException("Engin réservé aux comptes livreur.");
			}
			try {
				target.setTypeEnginLivreur(LivraisonService.parseTypeEngin(dto.getTypeEnginLivreur()));
			} catch (IllegalArgumentException ex) {
				throw new IllegalStateException("typeEnginLivreur invalide (MOTO ou VEHICULE).");
			}
		}
		target.setUserupdate(admin.getEmail());
		target.setDateupdate(LocalDateTime.now());
		return userRepository.save(target);
	}

	@Transactional
	public void deleteUserAsAdmin(User admin, Integer targetId) {
		User target = userRepository.findById(targetId)
				.orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
		String ar = admin.getRole() != null ? admin.getRole().getLibrole() : "";
		String tr = target.getRole() != null ? target.getRole().getLibrole() : "";
		if (RoleNames.SUPER_ADMIN.equalsIgnoreCase(tr) && !RoleNames.SUPER_ADMIN.equalsIgnoreCase(ar)) {
			throw new IllegalStateException("Seul un super-admin peut supprimer un super-admin.");
		}
		if (RoleNames.SUPER_ADMIN.equalsIgnoreCase(ar)) {
			// super-admin peut supprimer (y compris un autre super-admin si besoin métier ultérieur)
		} else if (RoleNames.ADMIN.equalsIgnoreCase(ar)) {
			if (RoleNames.ADMIN.equalsIgnoreCase(tr) || RoleNames.SUPER_ADMIN.equalsIgnoreCase(tr)) {
				throw new IllegalStateException("Un admin ne peut pas supprimer un autre admin ou super-admin.");
			}
		} else {
			throw new IllegalStateException("Action réservée aux administrateurs.");
		}
		for (Conversation c : conversationRepository.findByAcheteur_IduserOrVendeur_IduserOrderByDateupdateDesc(targetId,
				targetId)) {
			chatMessageRepository.deleteByConversation_Idconversation(c.getIdconversation());
			conversationRepository.delete(c);
		}
		complaintRepository.deleteByAuteur_Iduser(targetId);
		complaintRepository.deleteByArticle_Vendeur_Iduser(targetId);
		livraisonRepository.deleteByTransactionInvolvingUser(targetId);
		livraisonRepository.clearLivreurByUser(targetId);
		ecomTransactionRepository.deleteByAcheteur_Iduser(targetId);
		ecomTransactionRepository.deleteByVendeur_Iduser(targetId);
		articleRepository.deleteByVendeur_Iduser(targetId);
		userRepository.delete(target);
	}

	private static boolean isBcrypt(String raw) {
		return raw.startsWith("$2a$") || raw.startsWith("$2b$") || raw.startsWith("$2y$");
	}

	private static String roleLib(User u) {
		return u.getRole() == null ? "" : u.getRole().getLibrole();
	}
}
