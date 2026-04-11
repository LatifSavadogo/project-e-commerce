package net.ecommerce.springboot.service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

@Service
public class QrPngService {

	private static final int SIZE_PX = 280;

	public String encodeQrAsPngBase64(String text) {
		return Base64.getEncoder().encodeToString(encodeQrAsPngBytes(text, SIZE_PX));
	}

	/** PNG brut (ex. embarqué dans un PDF). */
	public byte[] encodeQrAsPngBytes(String text) {
		return encodeQrAsPngBytes(text, SIZE_PX);
	}

	public byte[] encodeQrAsPngBytes(String text, int sizePx) {
		if (text == null || text.isBlank()) {
			throw new IllegalArgumentException("Texte QR vide.");
		}
		if (sizePx < 64 || sizePx > 800) {
			throw new IllegalArgumentException("Taille QR invalide.");
		}
		try {
			QRCodeWriter writer = new QRCodeWriter();
			BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
			return baos.toByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("Génération du QR impossible.", e);
		}
	}
}
