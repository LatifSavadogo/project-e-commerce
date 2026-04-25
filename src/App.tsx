import './App.css'
import { Routes, Route, Link, useLocation, Navigate } from 'react-router-dom'
import Header from './components/Header'
import AdminLayout from './components/AdminLayout'
import Footer from './components/Footer'
import { useAuth, isStaffRole } from './contexts/AuthContext'
import Home from './pages/Home'
import Listings from './pages/Listings'
import Auth from './pages/Auth'
import Profile from './pages/Profile'
import ProductDetail from './pages/ProductDetail'
import Help from './pages/Help'
import Complaint from './pages/Complaint'
import AdminDashboard from './pages/AdminDashboard'
import AdminProducts from './pages/AdminProducts'
import Terms from './pages/Terms'
import Guide from './pages/Guide'
import Chat from './pages/Chat'
import VendorDashboard from './pages/VendorDashboard'
import AdminUsers from './pages/AdminUsers'
import AdminPayments from './pages/AdminPayments'
import AdminComplaints from './pages/AdminComplaints'
import AdminSettingsCatalog from './pages/AdminSettingsCatalog'
import AdminSettingsPays from './pages/AdminSettingsPays'
import AdminSettingsRoles from './pages/AdminSettingsRoles'
import AddProduct from './pages/AddProduct'
import ForgotPassword from './pages/ForgotPassword'
import ResetPassword from './pages/ResetPassword'
import Assistant from './pages/Assistant'
import CartPage from './pages/CartPage'
import LivreurLayout from './components/LivreurLayout'
import LivreurOverview from './pages/livreur/LivreurOverview'
import LivreurOffresPage from './pages/livreur/LivreurOffresPage'
import LivreurCoursesPage from './pages/livreur/LivreurCoursesPage'
import LivreurHistoriquePage from './pages/livreur/LivreurHistoriquePage'
import LivreurParametresPage from './pages/livreur/LivreurParametresPage'
import DemandeUpgrade from './pages/DemandeUpgrade'
import PaydunyaReturn from './pages/PaydunyaReturn'
import VendorCertification from './pages/VendorCertification'
import AdminRoleUpgrades from './pages/AdminRoleUpgrades'
import AdminLivraisonsPage from './pages/AdminLivraisonsPage'

function App() {
  const location = useLocation()
  const { user, authLoading } = useAuth()
  const isAdminRoute = location.pathname.startsWith('/admin')
  const isLivreurConsoleRoute = location.pathname.startsWith('/livreur')
  const isConsoleRoute = isAdminRoute || isLivreurConsoleRoute
  const staff = user != null && isStaffRole(user)
  const p = location.pathname
  /** Hors marketplace : mot de passe / reset, ou page de connexion seulement si non connecté */
  const staffAllowedPublic =
    p === '/forgot-password' || p === '/reset-password' || (p === '/auth' && user == null)

  if (!authLoading && staff && !isAdminRoute && !staffAllowedPublic) {
    return <Navigate to="/admin" replace />
  }

  const noMarketplaceChrome =
    staff && (p === '/forgot-password' || p === '/reset-password' || (p === '/auth' && user == null))
  const showSiteChrome = !isConsoleRoute && !noMarketplaceChrome

  return (
    <div className={`app-wrapper${isConsoleRoute ? ' app-wrapper--admin' : ''}`}>
      <a href="#main-content" className="skip-link">
        Aller au contenu
      </a>
      {showSiteChrome && <Header />}
      <main
        id="main-content"
        className={`main-content${isConsoleRoute ? ' main-content--admin' : ''}`}
        tabIndex={-1}
      >
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/listings" element={<Listings />} />
          <Route path="/product/:id" element={<ProductDetail />} />
          <Route path="/auth" element={<Auth />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/panier" element={<CartPage />} />
          <Route path="/help" element={<Help />} />
          <Route path="/help/assistant" element={<Assistant />} />
          <Route path="/help/complaint" element={<Complaint />} />
          <Route path="/terms" element={<Terms />} />
          <Route path="/guide" element={<Guide />} />
          <Route path="/chat" element={<Chat />} />
          <Route path="/vendor" element={<VendorDashboard />} />
          <Route path="/vendor/add-product" element={<AddProduct />} />
          <Route path="/vendor/certification" element={<VendorCertification />} />
          <Route path="/paiement/paydunya" element={<PaydunyaReturn />} />
          <Route path="/livreur" element={<LivreurLayout />}>
            <Route index element={<LivreurOverview />} />
            <Route path="offres" element={<LivreurOffresPage />} />
            <Route path="courses" element={<LivreurCoursesPage />} />
            <Route path="historique" element={<LivreurHistoriquePage />} />
            <Route path="parametres" element={<LivreurParametresPage />} />
          </Route>
          <Route path="/demande-upgrade" element={<DemandeUpgrade />} />
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<AdminDashboard />} />
            <Route path="articles" element={<AdminProducts />} />
            <Route path="products" element={<Navigate to="/admin/articles" replace />} />
            <Route path="users" element={<AdminUsers />} />
            <Route path="payments" element={<AdminPayments />} />
            <Route path="complaints" element={<AdminComplaints />} />
            <Route path="livraisons" element={<AdminLivraisonsPage />} />
            <Route path="settings/catalog" element={<AdminSettingsCatalog />} />
            <Route path="settings/pays" element={<AdminSettingsPays />} />
            <Route path="settings/roles" element={<AdminSettingsRoles />} />
            <Route path="settings/role-upgrades" element={<AdminRoleUpgrades />} />
          </Route>
          <Route
            path="*"
            element={
              <div className="container page-not-found">
                <h1>Page introuvable</h1>
                <p>La page demandée n’existe pas ou a été déplacée.</p>
                <Link to="/" className="button-primary" style={{ display: 'inline-block', padding: '12px 20px' }}>
                  Retour à l’accueil
                </Link>
              </div>
            }
          />
        </Routes>
      </main>
      {showSiteChrome && <Footer />}
    </div>
  )
}

export default App
