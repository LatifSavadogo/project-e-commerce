import { Check } from 'lucide-react'

export default function Terms() {
  return (
    <div className="container" style={{ paddingTop: 20, paddingBottom: 40, maxWidth: 800 }}>
      <h1>Conditions Générales d'Utilisation</h1>
      <p style={{ color: 'var(--muted)', marginBottom: 32 }}>
        Dernière mise à jour : {new Date().toLocaleDateString('fr-FR', { year: 'numeric', month: 'long', day: 'numeric' })}
      </p>

      <section style={{ marginBottom: 32 }}>
        <h2>1. Préambule</h2>
        <p>
          Bienvenue sur <strong>Ecomarket</strong>, une plateforme de commerce en ligne dédiée à la vente 
          et à l'achat d'articles entre particuliers et professionnels. En utilisant notre plateforme, 
          vous acceptez les présentes Conditions Générales d'Utilisation (CGU) dans leur intégralité.
        </p>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>2. Protection et Sécurité des Données</h2>
        <p style={{ fontWeight: 600, color: '#2a9d8f' }}>
          La sécurité de vos données personnelles est notre PREMIÈRE PRIORITÉ.
        </p>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>2.1 Engagement de Sécurité</h3>
        <ul>
          <li>Toutes vos données personnelles sont stockées de manière sécurisée et chiffrée</li>
          <li>Nous ne partageons JAMAIS vos informations avec des tiers sans votre consentement explicite</li>
          <li>Vos documents d'identité (CNI, passeport) sont utilisés uniquement pour la vérification et sont protégés par un cryptage de niveau bancaire</li>
          <li>Nous utilisons des protocoles de sécurité HTTPS pour toutes les communications</li>
          <li>Authentification sécurisée pour protéger votre compte</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>2.2 Vos Droits sur vos Données</h3>
        <ul>
          <li><strong>Droit d'accès</strong> : Vous pouvez consulter toutes vos données à tout moment</li>
          <li><strong>Droit de rectification</strong> : Vous pouvez modifier vos informations personnelles</li>
          <li><strong>Droit à l'oubli</strong> : Vous pouvez demander la suppression de votre compte et de toutes vos données</li>
          <li><strong>Droit d'opposition</strong> : Vous pouvez vous opposer au traitement de vos données</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>2.3 Conservation des Données</h3>
        <p>
          Vos données personnelles sont conservées uniquement pendant la durée nécessaire aux finalités 
          pour lesquelles elles ont été collectées, conformément à la réglementation en vigueur.
        </p>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>3. Inscription et Compte Utilisateur</h2>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>3.1 Conditions d'Inscription</h3>
        <ul>
          <li>Vous devez être majeur(e) pour créer un compte sur Ecomarket</li>
          <li>Les informations fournies doivent être exactes, complètes et à jour</li>
          <li>Vous devez fournir une pièce d'identité valide (CNI ou passeport) pour vérification</li>
          <li>Un seul compte par personne est autorisé</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>3.2 Types de Comptes</h3>
        <ul>
          <li><strong>Acheteur</strong> : Peut parcourir et acheter des articles sur la plateforme</li>
          <li><strong>Vendeur</strong> : Peut publier des annonces et vendre des articles</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>3.3 Sécurité du Compte</h3>
        <ul>
          <li>Vous êtes responsable de la confidentialité de votre mot de passe</li>
          <li>Vous devez nous informer immédiatement de toute utilisation non autorisée de votre compte</li>
          <li>Ecomarket ne peut être tenu responsable des pertes résultant d'une utilisation non autorisée de votre compte</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>4. Utilisation de la Plateforme</h2>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>4.1 Règles de Conduite</h3>
        <p>En utilisant Ecomarket, vous vous engagez à :</p>
        <ul>
          <li>Respecter les lois et règlements en vigueur</li>
          <li>Ne pas publier de contenu illégal, frauduleux ou offensant</li>
          <li>Fournir des descriptions honnêtes et précises de vos articles</li>
          <li>Respecter les autres utilisateurs et maintenir une communication courtoise</li>
          <li>Ne pas tenter de contourner les systèmes de sécurité de la plateforme</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>4.2 Contenu Interdit</h3>
        <p>Il est strictement interdit de publier :</p>
        <ul>
          <li>Articles contrefaits ou volés</li>
          <li>Produits dangereux ou illégaux</li>
          <li>Contenu à caractère pornographique, violent ou discriminatoire</li>
          <li>Informations fausses ou trompeuses</li>
          <li>Spam ou contenu publicitaire non autorisé</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>5. Publications et Annonces</h2>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>5.1 Responsabilité des Vendeurs</h3>
        <ul>
          <li>Les vendeurs sont entièrement responsables de l'exactitude des informations de leurs annonces</li>
          <li>Les photos doivent représenter fidèlement l'article mis en vente</li>
          <li>Le prix indiqué doit être le prix réel de vente</li>
          <li>L'état de l'article doit être décrit de manière honnête</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>5.2 Modération</h3>
        <ul>
          <li>Ecomarket se réserve le droit de modérer, bloquer ou supprimer toute publication non conforme</li>
          <li>Les administrateurs peuvent ajouter des avertissements sur les publications suspectes</li>
          <li>Les publications peuvent être signalées par les utilisateurs</li>
          <li>Les vendeurs seront notifiés en cas de modération de leurs annonces</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>6. Transactions et Paiements</h2>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>6.1 Sécurité des Transactions</h3>
        <ul>
          <li>Toutes les transactions sont sécurisées</li>
          <li>Nous recommandons fortement d'utiliser des modes de paiement sécurisés</li>
          <li>Ne communiquez jamais vos informations bancaires par messagerie</li>
        </ul>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>6.2 Litiges</h3>
        <p>
          En cas de litige entre acheteur et vendeur, contactez notre équipe d'assistance via 
          la section "Aide" → "Envoyer une plainte". Nous nous efforcerons de résoudre les conflits 
          de manière équitable.
        </p>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>7. Propriété Intellectuelle</h2>
        <ul>
          <li>Tous les contenus de la plateforme Ecomarket (logo, design, code) sont protégés par les droits d'auteur</li>
          <li>Les utilisateurs conservent les droits sur le contenu qu'ils publient</li>
          <li>En publiant du contenu, vous accordez à Ecomarket une licence d'utilisation pour afficher ce contenu sur la plateforme</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>8. Limitation de Responsabilité</h2>
        <ul>
          <li>Ecomarket agit en tant qu'intermédiaire entre acheteurs et vendeurs</li>
          <li>Nous ne sommes pas responsables de la qualité, de la légalité ou de la sécurité des articles proposés</li>
          <li>Les transactions se font sous la responsabilité des utilisateurs</li>
          <li>Nous ne garantissons pas la disponibilité continue de la plateforme</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>9. Suspension et Résiliation</h2>
        
        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>9.1 Par l'Utilisateur</h3>
        <p>
          Vous pouvez résilier votre compte à tout moment en nous contactant via la section "Aide". 
          Vos données seront supprimées conformément à notre politique de confidentialité.
        </p>

        <h3 style={{ fontSize: '1.1em', marginTop: 16 }}>9.2 Par Ecomarket</h3>
        <p>Nous nous réservons le droit de suspendre ou supprimer un compte en cas de :</p>
        <ul>
          <li>Violation des présentes CGU</li>
          <li>Activité frauduleuse ou illégale</li>
          <li>Comportement abusif envers d'autres utilisateurs</li>
          <li>Publication répétée de contenu interdit</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>10. Modifications des CGU</h2>
        <p>
          Ecomarket se réserve le droit de modifier les présentes CGU à tout moment. 
          Les utilisateurs seront informés des modifications importantes par email ou notification 
          sur la plateforme. L'utilisation continue de nos services après modification vaut acceptation 
          des nouvelles conditions.
        </p>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>11. Contact et Assistance</h2>
        <p>
          Pour toute question concernant ces Conditions Générales d'Utilisation, ou pour exercer 
          vos droits relatifs à vos données personnelles, vous pouvez nous contacter via :
        </p>
        <ul>
          <li>La section "Aide" de la plateforme</li>
          <li>En envoyant une plainte/question aux administrateurs (LATIF & PARE)</li>
          <li>Les messages sont traités dans les plus brefs délais</li>
        </ul>
      </section>

      <section style={{ marginBottom: 32 }}>
        <h2>12. Droit Applicable</h2>
        <p>
          Les présentes Conditions Générales d'Utilisation sont régies par le droit en vigueur. 
          Tout litige relatif à l'interprétation ou à l'exécution des présentes sera soumis aux 
          tribunaux compétents, après tentative de résolution amiable.
        </p>
      </section>

      <div style={{ 
        background: 'var(--surface)', 
        border: '2px solid var(--accent)',
        borderRadius: 12,
        padding: 20,
        marginTop: 40
      }}>
        <p style={{ fontWeight: 600, color: 'var(--accent)', marginBottom: 12, display: 'flex', alignItems: 'center', gap: 8 }}>
          <Check size={20} strokeWidth={2} aria-hidden />
          Engagement Ecomarket
        </p>
        <p style={{ marginBottom: 8 }}>
          Nous nous engageons à fournir une plateforme sécurisée, transparente et équitable pour tous nos utilisateurs. 
          La protection de vos données et de votre vie privée est au cœur de nos priorités.
        </p>
        <p style={{ margin: 0, fontSize: '0.9em', color: 'var(--muted)' }}>
          En cas de questions ou de préoccupations, notre équipe d'administration est à votre disposition.
        </p>
      </div>
    </div>
  )
}
