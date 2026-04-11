package net.ecommerce.springboot.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import net.ecommerce.springboot.model.EcomTransaction;
import net.ecommerce.springboot.model.Livraison;
import net.ecommerce.springboot.model.PaymentMethod;

/**
 * Reçu de paiement A4 (OpenPDF) — présentation soignée, sans e-mail vendeur, avec QR livraison + QR commande.
 */
public final class PaymentReceiptPdfWriter {

	private static final Color BRAND = new Color(5, 122, 85);
	private static final Color BRAND_LIGHT = new Color(236, 253, 245);
	private static final Color HEADER_BG = new Color(248, 250, 252);
	private static final Color MUTED = new Color(100, 116, 139);
	private static final Color LINE = new Color(226, 232, 240);
	private static final Color TEXT = new Color(30, 41, 59);

	private PaymentReceiptPdfWriter() {
	}

	public static byte[] build(EcomTransaction t, String referenceExterne, byte[] qrLivraisonPng,
			String qrLivraisonNote, byte[] qrAchatPng) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = new Document(PageSize.A4, 42, 42, 48, 48);
		try {
			PdfWriter writer = PdfWriter.getInstance(doc, out);
			writer.setCloseStream(false);
			doc.open();

			final BaseFont base = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);

			Font brandTitle = new Font(base, 26f, Font.BOLD, BRAND);
			Font receiptNo = new Font(base, 11f, Font.BOLD, TEXT);
			Font headerMeta = new Font(base, 9.5f, Font.NORMAL, MUTED);
			Font sectionTitle = new Font(base, 9f, Font.BOLD, MUTED);
			Font label = new Font(base, 9f, Font.BOLD, MUTED);
			Font value = new Font(base, 10f, Font.NORMAL, TEXT);
			Font valueStrong = new Font(base, 11f, Font.BOLD, BRAND);
			Font thanks = new Font(base, 11f, Font.NORMAL, TEXT);
			Font footer = new Font(base, 8f, Font.NORMAL, MUTED);
			Font qrTitle = new Font(base, 9f, Font.BOLD, TEXT);
			Font qrHint = new Font(base, 7.5f, Font.NORMAL, MUTED);
			Font qrNote = new Font(base, 8f, Font.NORMAL, MUTED);

			// ——— Bloc en-tête (bandeau vert + fond clair)
			PdfPTable headerShell = new PdfPTable(new float[] { 5.5f, 94.5f });
			headerShell.setWidthPercentage(100);
			headerShell.setSpacingAfter(22f);

			PdfPCell stripe = new PdfPCell();
			stripe.setBackgroundColor(BRAND);
			stripe.setBorder(Rectangle.NO_BORDER);
			stripe.setMinimumHeight(72f);

			PdfPCell headInner = new PdfPCell();
			headInner.setBorder(Rectangle.NO_BORDER);
			headInner.setBackgroundColor(HEADER_BG);
			headInner.setPaddingLeft(20f);
			headInner.setPaddingRight(18f);
			headInner.setPaddingTop(16f);
			headInner.setPaddingBottom(16f);
			headInner.setVerticalAlignment(Element.ALIGN_MIDDLE);

			Paragraph pBrand = new Paragraph("ECOMARKET", brandTitle);
			pBrand.setSpacingAfter(6f);
			headInner.addElement(pBrand);

			Paragraph pSub = new Paragraph("Reçu de paiement officiel", headerMeta);
			pSub.setSpacingAfter(10f);
			headInner.addElement(pSub);

			Paragraph pNo = new Paragraph("N° " + t.getIdtransaction(), receiptNo);
			pNo.setSpacingAfter(4f);
			headInner.addElement(pNo);

			String dateStr = t.getDatecreation() != null ? t.getDatecreation().toString() : "—";
			headInner.addElement(new Paragraph("Émis le " + pdfSafe(dateStr), headerMeta));

			headerShell.addCell(stripe);
			headerShell.addCell(headInner);
			doc.add(headerShell);

			// ——— Titre de section
			Paragraph sec = new Paragraph("DÉTAIL DE LA COMMANDE", sectionTitle);
			sec.setSpacingAfter(10f);
			doc.add(sec);

			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);
			table.setWidths(new float[] { 36f, 64f });
			table.setSpacingAfter(18f);

			String articleLib = t.getArticle() != null ? t.getArticle().getLibarticle() : "—";
			String idArticle = t.getArticle() != null ? String.valueOf(t.getArticle().getIdarticle()) : "—";
			DecimalFormat money = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.FRANCE));

			addRow(table, label, value, false, "Article", articleLib + " (réf. " + idArticle + ")");
			addRow(table, label, value, false, "Quantité", String.valueOf(t.getQuantite()));
			addRow(table, label, value, false, "Prix unitaire", formatFcfa(t.getPrixUnitaireSnapshot(), money));
			addRow(table, label, value, false, "Frais affichés", formatFcfa(t.getFraisAffiches(), money));
			addRow(table, label, value, false, "Moyen de paiement", libelleMoyen(t.getMoyenPaiement()));
			addRow(table, label, value, false, "Référence de paiement", referenceExterne != null ? referenceExterne : "—");
			addRow(table, label, value, false, "Acheteur", formatBuyer(t));
			addRow(table, label, value, false, "Vendeur", formatVendorNameOnly(t));

			Livraison liv = t.getLivraison();
			if (liv != null) {
				addRow(table, label, value, false, "Livraison", "#" + liv.getIdlivraison() + " — " + liv.getStatut().name());
			}

			// Ligne total mise en avant (fond vert très léger)
			addRow(table, label, valueStrong, true, "Montant total", formatFcfa(t.getMontantTotal(), money));

			doc.add(table);

			Paragraph secQr = new Paragraph("CODES QR", sectionTitle);
			secQr.setSpacingBefore(6f);
			secQr.setSpacingAfter(10f);
			doc.add(secQr);

			addQrRow(doc, qrTitle, qrHint, qrNote, qrLivraisonPng, qrLivraisonNote, qrAchatPng);

			horizontalRule(doc);

			Paragraph th = new Paragraph("Merci pour votre confiance.", thanks);
			th.setAlignment(Element.ALIGN_CENTER);
			th.setSpacingBefore(14f);
			th.setSpacingAfter(18f);
			doc.add(th);

			Paragraph foot = new Paragraph(
					"Document généré électroniquement — Conservez ce reçu pour vos archives. "
							+ "Pour toute question, utilisez l’assistance depuis la boutique.",
					footer);
			foot.setAlignment(Element.ALIGN_CENTER);
			foot.setLeading(11f);
			doc.add(foot);
		} catch (DocumentException e) {
			throw new IOException(e);
		} finally {
			if (doc.isOpen()) {
				doc.close();
			}
		}
		return out.toByteArray();
	}

	private static void addQrRow(Document doc, Font qrTitle, Font qrHint, Font qrNote, byte[] qrLivraisonPng,
			String qrLivraisonNote, byte[] qrAchatPng) throws DocumentException {
		PdfPTable row = new PdfPTable(2);
		row.setWidthPercentage(100);
		row.setWidths(new float[] { 50f, 50f });
		row.setSpacingAfter(16f);

		row.addCell(buildQrCell("QR réception (à présenter au livreur)", qrLivraisonPng,
				qrLivraisonNote != null ? qrLivraisonNote : "Indisponible.", qrTitle, qrHint, qrNote, 132f,
				"Identique au QR Mes achats — à présenter au livreur."));
		row.addCell(buildQrCell("QR commande (référence d'achat)", qrAchatPng,
				"Référence de commande indisponible.", qrTitle, qrHint, qrNote, 118f,
				"Autre code : ne sert pas à valider la livraison."));

		doc.add(row);
	}

	private static PdfPCell buildQrCell(String title, byte[] png, String noteIfNoImage, Font titleFont, Font hintFont,
			Font noteFont, float maxQrSidePt, String hintLine) throws DocumentException {
		PdfPCell c = new PdfPCell();
		c.setBorder(Rectangle.NO_BORDER);
		// Fond blanc : meilleur contraste pour les scanners (évite le vert très clair sur le motif).
		c.setBackgroundColor(Color.WHITE);
		c.setPadding(12f);
		c.setHorizontalAlignment(Element.ALIGN_CENTER);

		Paragraph pTitle = new Paragraph(pdfSafe(title), titleFont);
		pTitle.setAlignment(Element.ALIGN_CENTER);
		pTitle.setSpacingAfter(8f);
		c.addElement(pTitle);

		if (png != null && png.length > 0) {
			try {
				Image im = Image.getInstance(png);
				// PNG source = même résolution que l’API (280 px) ; affichage suffisamment grand pour un scan fiable.
				im.scaleToFit(maxQrSidePt, maxQrSidePt);
				im.setAlignment(Image.ALIGN_CENTER);
				c.addElement(im);
				Paragraph hint = new Paragraph(pdfSafe(hintLine), hintFont);
				hint.setAlignment(Element.ALIGN_CENTER);
				hint.setSpacingBefore(6f);
				c.addElement(hint);
			} catch (Exception e) {
				Paragraph err = new Paragraph(pdfSafe(noteIfNoImage != null ? noteIfNoImage : "QR indisponible."),
						noteFont);
				err.setAlignment(Element.ALIGN_CENTER);
				c.addElement(err);
			}
		} else {
			Paragraph n = new Paragraph(pdfSafe(noteIfNoImage != null ? noteIfNoImage : "—"), noteFont);
			n.setAlignment(Element.ALIGN_CENTER);
			c.addElement(n);
		}
		return c;
	}

	private static void horizontalRule(Document doc) throws DocumentException {
		PdfPTable rule = new PdfPTable(1);
		rule.setWidthPercentage(42);
		rule.setHorizontalAlignment(Element.ALIGN_CENTER);
		PdfPCell r = new PdfPCell();
		r.setFixedHeight(1.2f);
		r.setBackgroundColor(LINE);
		r.setBorder(Rectangle.NO_BORDER);
		rule.addCell(r);
		doc.add(rule);
	}

	private static String formatBuyer(EcomTransaction t) {
		if (t.getAcheteur() == null) {
			return "—";
		}
		return formatUserWithEmail(t.getAcheteur().getEmail(), t.getAcheteur().getPrenom(), t.getAcheteur().getNom());
	}

	/** Vendeur : prénom et nom uniquement (pas d’e-mail sur le reçu). */
	private static String formatVendorNameOnly(EcomTransaction t) {
		if (t.getVendeur() == null) {
			return "—";
		}
		return formatPersonNameOnly(t.getVendeur().getPrenom(), t.getVendeur().getNom());
	}

	private static String formatPersonNameOnly(String prenom, String nom) {
		String n = "";
		if (prenom != null && !prenom.isBlank()) {
			n = prenom.trim();
		}
		if (nom != null && !nom.isBlank()) {
			n = n.isEmpty() ? nom.trim() : n + " " + nom.trim();
		}
		return n.isEmpty() ? "—" : n;
	}

	private static String formatFcfa(int amount, DecimalFormat money) {
		String n = money.format(amount).replace('\u00a0', ' ').replace('\u202f', ' ').replace('\u2007', ' ');
		return n + " FCFA";
	}

	private static String pdfSafe(String s) {
		if (s == null || s.isBlank()) {
			return "—";
		}
		StringBuilder sb = new StringBuilder(s.length());
		s.codePoints().forEach(cp -> {
			if (cp == '\n' || cp == '\r' || cp == '\t') {
				sb.append(' ');
			} else if (Character.isWhitespace(cp)) {
				sb.append(' ');
			} else if (cp >= 32 && cp != 127 && cp <= 0xFF) {
				sb.append((char) cp);
			} else if (cp > 0xFF) {
				sb.append('?');
			}
		});
		String t = sb.toString().trim().replaceAll(" +", " ");
		return t.isEmpty() ? "—" : t;
	}

	private static void addRow(PdfPTable table, Font labelFont, Font valueFont, boolean highlightTotal, String k,
			String v) {
		PdfPCell kc = new PdfPCell(new Phrase(pdfSafe(k), labelFont));
		kc.setBorder(Rectangle.NO_BORDER);
		kc.setBorderWidthBottom(highlightTotal ? 0f : 0.5f);
		kc.setBorderColorBottom(LINE);
		kc.setPaddingTop(highlightTotal ? 12f : 8f);
		kc.setPaddingBottom(highlightTotal ? 12f : 8f);
		kc.setVerticalAlignment(Element.ALIGN_MIDDLE);
		if (highlightTotal) {
			kc.setBackgroundColor(BRAND_LIGHT);
			kc.setBorderWidthTop(1.2f);
			kc.setBorderColorTop(BRAND);
			kc.setBorder(Rectangle.TOP);
		}

		PdfPCell vc = new PdfPCell(new Phrase(pdfSafe(v), valueFont));
		vc.setBorder(Rectangle.NO_BORDER);
		vc.setBorderWidthBottom(highlightTotal ? 0f : 0.5f);
		vc.setBorderColorBottom(LINE);
		vc.setPaddingTop(highlightTotal ? 12f : 8f);
		vc.setPaddingBottom(highlightTotal ? 12f : 8f);
		vc.setVerticalAlignment(Element.ALIGN_MIDDLE);
		vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
		if (highlightTotal) {
			vc.setBackgroundColor(BRAND_LIGHT);
			vc.setBorderWidthTop(1.2f);
			vc.setBorderColorTop(BRAND);
			vc.setBorder(Rectangle.TOP);
		}

		table.addCell(kc);
		table.addCell(vc);
	}

	private static String formatUserWithEmail(String email, String prenom, String nom) {
		if (email == null || email.isBlank()) {
			return formatPersonNameOnly(prenom, nom);
		}
		String n = formatPersonNameOnly(prenom, nom);
		if ("—".equals(n)) {
			return pdfSafe(email);
		}
		return n + " · " + pdfSafe(email);
	}

	private static String libelleMoyen(PaymentMethod m) {
		if (m == null) {
			return "—";
		}
		return switch (m) {
			case ORANGE_MONEY -> "Orange Money";
			case MOOV_MONEY -> "Moov Money";
			case VIREMENT -> "Virement bancaire";
			case ESPECES -> "Espèces";
		};
	}
}
