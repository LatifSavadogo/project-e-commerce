package net.ecommerce.springboot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CartItem")
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer idcartitem;

	@ManyToOne
	@JoinColumn(name = "idcart", nullable = false)
	private Cart cart;

	@ManyToOne
	@JoinColumn(name = "idarticle", nullable = false)
	private Article article;

	@Column(nullable = false)
	private int quantity;

	/** Si non null : prix et quantité figés selon ce message d’accord (négociation). */
	@ManyToOne
	@JoinColumn(name = "idchatmessage_accord")
	private ChatMessage agreedMessage;

	public CartItem() {
	}

	public Integer getIdcartitem() {
		return idcartitem;
	}

	public void setIdcartitem(Integer idcartitem) {
		this.idcartitem = idcartitem;
	}

	public Cart getCart() {
		return cart;
	}

	public void setCart(Cart cart) {
		this.cart = cart;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public ChatMessage getAgreedMessage() {
		return agreedMessage;
	}

	public void setAgreedMessage(ChatMessage agreedMessage) {
		this.agreedMessage = agreedMessage;
	}
}
