import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { ThemeProvider } from './contexts/ThemeContext.tsx'
import { ProductProvider } from './contexts/ProductContext.tsx'
import { AuthProvider } from './contexts/AuthContext.tsx'
import { CartProvider } from './contexts/CartContext.tsx'
import { ComplaintProvider } from './contexts/ComplaintContext.tsx'
import { ChatProvider } from './contexts/ChatContext.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <CartProvider>
            <ComplaintProvider>
              <ProductProvider>
                <ChatProvider>
                  <App />
                </ChatProvider>
              </ProductProvider>
            </ComplaintProvider>
          </CartProvider>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </StrictMode>,
)
