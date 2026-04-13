/**
 * Villes par pays (référentiel statique côté client, sans API ville).
 * Clés = libellé pays normalisé (voir normalizePaysKey).
 */
const RAW: Record<string, string[]> = {
  'BURKINA FASO': [
    'Ouagadougou',
    'Bobo-Dioulasso',
    'Koudougou',
    'Banfora',
    'Ouahigouya',
    'Pouytenga',
    'Kaya',
    'Tenkodogo',
    "Fada N'Gourma",
    'Dédougou',
  ],
  'COTE D IVOIRE': [
    'Abidjan',
    'Yamoussoukro',
    'Bouaké',
    'Daloa',
    'Korhogo',
    'San-Pédro',
    'Man',
    'Divo',
    'Gagnoa',
    'Soubré',
  ],
  SENEGAL: ['Dakar', 'Thiès', 'Saint-Louis', 'Kaolack', 'Ziguinchor', 'Touba', 'Mbour', 'Rufisque'],
  MALI: ['Bamako', 'Sikasso', 'Mopti', 'Koutiala', 'Kayes', 'Ségou', 'Gao', 'Kidal'],
  NIGER: ['Niamey', 'Zinder', 'Maradi', 'Agadez', 'Tahoua', 'Dosso'],
  BENIN: ['Cotonou', 'Porto-Novo', 'Parakou', 'Djougou', 'Abomey-Calavi', 'Bohicon'],
  TOGO: ['Lomé', 'Sokodé', 'Kara', 'Atakpamé', 'Dapaong', 'Tsévié'],
  GHANA: ['Accra', 'Kumasi', 'Tamale', 'Takoradi', 'Cape Coast', 'Sunyani'],
  FRANCE: [
    'Paris',
    'Lyon',
    'Marseille',
    'Toulouse',
    'Nice',
    'Nantes',
    'Montpellier',
    'Strasbourg',
    'Bordeaux',
    'Lille',
  ],
  CAMEROUN: ['Douala', 'Yaoundé', 'Garoua', 'Bafoussam', 'Bamenda', 'Maroua'],
  'CONGO': ['Brazzaville', 'Pointe-Noire', 'Dolisie', 'Nkayi'],
  'REPUBLIQUE DEMOCRATIQUE DU CONGO': ['Kinshasa', 'Lubumbashi', 'Mbuji-Mayi', 'Kananga', 'Kisangani'],
  MAROC: ['Casablanca', 'Rabat', 'Fès', 'Marrakech', 'Tanger', 'Agadir', 'Meknès'],
  TUNISIE: ['Tunis', 'Sfax', 'Sousse', 'Kairouan', 'Bizerte', 'Gabès'],
  ALGERIE: ['Alger', 'Oran', 'Constantine', 'Annaba', 'Blida', 'Sétif'],
  CANADA: ['Montréal', 'Toronto', 'Vancouver', 'Calgary', 'Ottawa', 'Québec'],
  BELGIQUE: ['Bruxelles', 'Anvers', 'Gand', 'Charleroi', 'Liège', 'Bruges'],
  SUISSE: ['Zurich', 'Genève', 'Bâle', 'Lausanne', 'Berne', 'Winterthour'],
}

export function normalizePaysKey(libpays: string): string {
  return libpays
    .normalize('NFD')
    .replace(/\p{M}/gu, '')
    .replace(/[^a-zA-Z0-9]+/g, ' ')
    .trim()
    .toUpperCase()
    .replace(/\s+/g, ' ')
}

/** Retourne les villes connues pour ce libellé pays (API), ou [] si aucune liste dédiée. */
export function getCitiesForPaysLibelle(libpays: string | null | undefined): string[] {
  if (!libpays || !libpays.trim()) return []
  const key = normalizePaysKey(libpays)
  if (RAW[key]) return RAW[key]
  for (const [k, cities] of Object.entries(RAW)) {
    if (key.includes(k) || k.includes(key)) return cities
  }
  return []
}
