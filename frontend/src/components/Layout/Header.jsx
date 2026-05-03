import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../contexts/ToastContext';

const NAV_CATEGORIES = [
  { id:'Moda',          color:'#e91e8c' },
  { id:'Elektronik',    color:'#2563eb' },
  { id:'Ev & Yaşam',    color:'#16a34a' },
  { id:'Bilgisayar',    color:'#7c3aed' },
  { id:'Tablet',        color:'#0891b2' },
  { id:'Telefon',       color:'#dc2626' },
  { id:'Spor',          color:'#d97706' },
  { id:'Kitap & Müzik', color:'#059669' },
];

export default function Header() {
  const navigate = useNavigate();
  const { user, logout, isAdmin } = useAuth();
  const { cartCount } = useCart();
  const { toast } = useToast();
  const [search, setSearch] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);

  const handleSearch = (e) => {
    e.preventDefault();
    if (search.trim()) navigate(`/products?search=${encodeURIComponent(search.trim())}`);
  };

  const handleLogout = () => {
    logout();
    toast('Çıkış yapıldı', 'success');
    navigate('/');
  };

  return (
    <header className="bg-white sticky top-0 z-50" style={{ boxShadow: '0 2px 8px rgba(0,0,0,.08)' }}>
      {/* Top bar */}
      <div style={{ background: 'linear-gradient(135deg, #e91e8c 0%, #c4177a 100%)' }}>
        <div className="max-w-screen-xl mx-auto px-5 py-2.5 flex items-center gap-4">
          {/* Logo */}
          <Link to="/" className="flex-shrink-0 flex items-center gap-2">
            <div className="w-11 h-11 rounded-full bg-white flex items-center justify-center" style={{ boxShadow: '0 2px 8px rgba(0,0,0,.2)' }}>
              <svg width="26" height="26" viewBox="0 0 26 26">
                <circle cx="13" cy="13" r="12" fill="#e91e8c" />
                <circle cx="9"  cy="10" r="4" fill="#fff" />
                <circle cx="17" cy="16" r="4" fill="#fff" opacity=".7" />
              </svg>
            </div>
            <div className="text-white leading-tight">
              <div className="font-extrabold text-base tracking-tight">n11</div>
              <div className="font-medium text-[10px] opacity-85">TalentHub</div>
            </div>
          </Link>

          {/* Search */}
          <form onSubmit={handleSearch} className="flex-1 flex max-w-2xl">
            <input
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Ürün, kategori, marka ara"
              className="flex-1 px-4 py-2.5 text-sm outline-none rounded-l-lg text-gray-700"
            />
            <button type="submit" className="px-4 bg-white rounded-r-lg border-l border-gray-200 text-base" style={{ color: '#e91e8c' }}>
              🔍
            </button>
          </form>

          {/* Right actions */}
          <div className="flex items-center gap-1.5 ml-auto">
            <Link
              to="/cart"
              className="relative text-white px-3 py-2 rounded-lg flex items-center gap-1.5 text-sm font-semibold hover:bg-white/20 transition-colors"
            >
              <span className="text-xl">🛒</span>
              <span>Sepetim</span>
              {cartCount > 0 && (
                <span className="absolute top-0.5 right-1 w-4 h-4 rounded-full text-[10px] font-extrabold flex items-center justify-center" style={{ background: '#fff', color: '#e91e8c' }}>
                  {cartCount > 99 ? '99+' : cartCount}
                </span>
              )}
            </Link>

            {user ? (
              <div className="relative" onMouseLeave={() => setMenuOpen(false)}>
                <button
                  onMouseEnter={() => setMenuOpen(true)}
                  className="text-white px-3 py-2 rounded-lg flex items-center gap-1.5 text-sm font-semibold"
                >
                  <span className="text-lg">👤</span>
                  <span className="max-w-24 truncate">{user.firstName || user.email}</span>
                  <span className="text-[10px]">▾</span>
                </button>
                {menuOpen && (
                  <div className="absolute top-full right-0 bg-white rounded-xl shadow-xl min-w-44 py-2 z-50" style={{ boxShadow: '0 8px 32px rgba(0,0,0,.15)' }}>
                    <div className="px-4 py-2 border-b border-gray-100 text-xs text-gray-400">Hoş geldin,</div>
                    <div className="px-4 py-2 font-semibold text-sm text-gray-800">{user.firstName} {user.lastName}</div>
                    <Link to="/orders" className="block px-4 py-2.5 text-sm text-gray-700 hover:bg-gray-50">📦 Siparişlerim</Link>
                    {isAdmin && <Link to="/admin/products" className="block px-4 py-2.5 text-sm text-purple-700 font-medium hover:bg-purple-50">⚙️ Admin Panel</Link>}
                    <button onClick={handleLogout} className="w-full text-left px-4 py-2.5 text-sm text-red-600 font-medium flex items-center gap-2 hover:bg-red-50">
                      → Çıkış Yap
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="flex gap-1.5">
                <Link to="/login"
                  className="text-white px-3.5 py-2 rounded-lg text-sm font-semibold border border-white/50 hover:bg-white/15 transition-colors">
                  Giriş Yap
                </Link>
                <Link to="/register"
                  className="px-3.5 py-2 rounded-lg text-sm font-semibold bg-white hover:opacity-90 transition-opacity"
                  style={{ color: '#e91e8c' }}>
                  Üye Ol
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Category nav */}
      <nav className="max-w-screen-xl mx-auto px-5 flex overflow-x-auto" style={{ scrollbarWidth: 'none' }}>
        {NAV_CATEGORIES.map(cat => (
          <button
            key={cat.id}
            onClick={() => navigate(`/products?category=${encodeURIComponent(cat.id)}`)}
            className="px-3.5 py-2.5 text-sm font-medium text-gray-600 whitespace-nowrap border-b-2 border-transparent transition-colors hover:text-brand hover:border-brand"
            style={{ '--hover-color': cat.color }}
            onMouseEnter={e => { e.currentTarget.style.color = cat.color; e.currentTarget.style.borderBottomColor = cat.color; }}
            onMouseLeave={e => { e.currentTarget.style.color = ''; e.currentTarget.style.borderBottomColor = 'transparent'; }}
          >
            {cat.id}
          </button>
        ))}
      </nav>
    </header>
  );
}
