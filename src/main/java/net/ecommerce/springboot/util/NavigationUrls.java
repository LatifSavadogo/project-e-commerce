package net.ecommerce.springboot.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Liens Google Maps pour itinéraires (mobile ouvre souvent l’app).
 */
public final class NavigationUrls {

	private NavigationUrls() {
	}

	/**
	 * Itinéraire avec destination texte (ville, adresse) lorsque les coordonnées du destinataire ne sont pas en base.
	 */
	public static String googleMapsDirToPlace(Double originLat, Double originLng, String destinationQuery) {
		if (destinationQuery == null || destinationQuery.isBlank()) {
			return null;
		}
		StringBuilder sb = new StringBuilder("https://www.google.com/maps/dir/?api=1");
		if (originLat != null && originLng != null) {
			sb.append("&origin=").append(round6(originLat)).append(",").append(round6(originLng));
		}
		sb.append("&destination=").append(URLEncoder.encode(destinationQuery.trim(), StandardCharsets.UTF_8));
		return sb.toString();
	}

	public static String googleMapsDir(Double originLat, Double originLng, Double waypointLat, Double waypointLng,
			Double destLat, Double destLng) {
		StringBuilder sb = new StringBuilder("https://www.google.com/maps/dir/?api=1");
		if (originLat != null && originLng != null) {
			sb.append("&origin=").append(round6(originLat)).append(",").append(round6(originLng));
		}
		if (waypointLat != null && waypointLng != null) {
			sb.append("&waypoints=").append(round6(waypointLat)).append(",").append(round6(waypointLng));
		}
		if (destLat != null && destLng != null) {
			sb.append("&destination=").append(round6(destLat)).append(",").append(round6(destLng));
		}
		return sb.toString();
	}

	public static String googleMapsSearchQuery(String query) {
		if (query == null || query.isBlank()) {
			return "https://www.google.com/maps";
		}
		return "https://www.google.com/maps/search/?api=1&query="
				+ URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
	}

	public static String googleMapsSearchLatLng(Double lat, Double lng) {
		if (lat == null || lng == null) {
			return "https://www.google.com/maps";
		}
		String q = round6(lat) + "," + round6(lng);
		return "https://www.google.com/maps/search/?api=1&query=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
	}

	/** Arrondi pour URLs stables. */
	private static String round6(double v) {
		return String.format(Locale.US, "%.6f", v);
	}
}
