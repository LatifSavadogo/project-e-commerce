package net.ecommerce.springboot.dto;

import java.util.List;

import net.ecommerce.springboot.model.Cart;

public class CartDTO {

	private Integer idcart;
	private List<CartItemDTO> items;
	private long montantTotalEstime;

	public static CartDTO fromEntity(Cart cart) {
		CartDTO d = new CartDTO();
		d.setIdcart(cart.getIdcart());
		List<CartItemDTO> itemDtos = cart.getItems().stream().map(CartItemDTO::fromEntity).toList();
		d.setItems(itemDtos);
		long sum = 0;
		for (CartItemDTO x : itemDtos) {
			int unit = x.getPrixUnitaireNegocie() != null ? x.getPrixUnitaireNegocie() : x.getPrixUnitaireCatalogue();
			sum += (long) unit * x.getQuantity();
		}
		d.setMontantTotalEstime(sum);
		return d;
	}

	public Integer getIdcart() {
		return idcart;
	}

	public void setIdcart(Integer idcart) {
		this.idcart = idcart;
	}

	public List<CartItemDTO> getItems() {
		return items;
	}

	public void setItems(List<CartItemDTO> items) {
		this.items = items;
	}

	public long getMontantTotalEstime() {
		return montantTotalEstime;
	}

	public void setMontantTotalEstime(long montantTotalEstime) {
		this.montantTotalEstime = montantTotalEstime;
	}
}
