import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchProducts } from '../services/api';
import { CATEGORY_META } from '../services/mockData';
import ProductCard from '../components/product/ProductCard';
import SkeletonCard from '../components/ui/SkeletonCard';

export default function HomePage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchProducts({ size: 24, page: 0 })
      .then(data => setProducts(data.content || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const deals   = products.filter(p => p.originalPrice && p.originalPrice > p.price).slice(0, 8);
  const popular = products.filter(p => p.rating >= 4.7).slice(0, 8);

  return (
    <div className="space-y-10">

      {/* ── Hero ─────────────────────────────────────────────────────────── */}
      <section
        className="rounded-2xl overflow-hidden relative flex items-center min-h-56 px-8 py-10"
        style={{ background: 'linear-gradient(135deg, #e91e8c 0%, #7c3aed 60%, #2563eb 100%)' }}
      >
        {/* decorative circles */}
        <div className="absolute right-0 top-0 w-72 h-72 rounded-full opacity-10 bg-white -translate-y-1/3 translate-x-1/4" />
        <div className="absolute right-32 bottom-0 w-40 h-40 rounded-full opacity-10 bg-white translate-y-1/3" />

        <div className="relative z-10 max-w-lg">
          <div className="text-white/80 text-sm font-semibold mb-2 tracking-wide uppercase">N11 TalentHub'a Hoş Geldiniz</div>
          <h1 className="text-white font-extrabold text-3xl leading-tight mb-3">
            Milyonlarca Ürün,<br />Tek Adreste
          </h1>
          <p className="text-white/80 text-sm mb-6 leading-relaxed">
            Elektronikten modaya, ev yaşamından spora kadar her şey burada.
            Ücretsiz kargo ve güvenli alışveriş.
          </p>
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/products')}
              className="bg-white text-sm font-bold px-5 py-2.5 rounded-lg transition-opacity hover:opacity-90"
              style={{ color: '#e91e8c' }}
            >
              Alışverişe Başla
            </button>
            <button
              onClick={() => navigate('/products?category=Elektronik')}
              className="text-white text-sm font-semibold px-5 py-2.5 rounded-lg border border-white/50 hover:bg-white/15 transition-colors"
            >
              Fırsatları Gör
            </button>
          </div>
        </div>

        {/* right illustration */}
        <div className="absolute right-12 top-1/2 -translate-y-1/2 hidden md:flex gap-4 opacity-90">
          {['💻', '📱', '👗', '🏠'].map((icon, i) => (
            <div
              key={i}
              className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center text-3xl"
              style={{ transform: `translateY(${i % 2 === 0 ? '-8px' : '8px'})` }}
            >
              {icon}
            </div>
          ))}
        </div>
      </section>

      {/* ── Categories ───────────────────────────────────────────────────── */}
      <section>
        <SectionTitle>Kategoriler</SectionTitle>
        <div className="grid grid-cols-4 sm:grid-cols-8 gap-3">
          {CATEGORY_META.map(cat => (
            <button
              key={cat.id}
              onClick={() => navigate(`/products?category=${encodeURIComponent(cat.id)}`)}
              className="flex flex-col items-center gap-2 p-3 rounded-xl bg-white hover:-translate-y-0.5 hover:shadow-md transition-all duration-200 cursor-pointer"
              style={{ boxShadow: '0 1px 4px rgba(0,0,0,.06)' }}
            >
              <div
                className="w-12 h-12 rounded-full flex items-center justify-center text-2xl"
                style={{ background: cat.color + '18' }}
              >
                {cat.icon}
              </div>
              <span className="text-[11px] font-semibold text-gray-600 text-center leading-tight">{cat.id}</span>
            </button>
          ))}
        </div>
      </section>

      {/* ── Flash Deals ──────────────────────────────────────────────────── */}
      {(loading || deals.length > 0) && (
        <section>
          <div className="flex items-center justify-between mb-4">
            <SectionTitle>
              <span className="text-red-600">🔥</span> Flaş İndirimler
            </SectionTitle>
            <button
              onClick={() => navigate('/products')}
              className="text-sm font-semibold hover:underline"
              style={{ color: '#e91e8c' }}
            >
              Tümünü Gör →
            </button>
          </div>
          <ProductGrid products={deals} loading={loading} />
        </section>
      )}

      {/* ── Promo Banner ─────────────────────────────────────────────────── */}
      <section className="grid grid-cols-2 gap-4">
        <PromoBanner
          icon="💻"
          title="Bilgisayar & Tablet"
          subtitle="En yeni modeller stokta"
          color="#7c3aed"
          onClick={() => navigate('/products?category=Bilgisayar')}
        />
        <PromoBanner
          icon="👗"
          title="Moda & Giyim"
          subtitle="Sezona özel koleksiyonlar"
          color="#e91e8c"
          onClick={() => navigate('/products?category=Moda')}
        />
      </section>

      {/* ── Popular Products ─────────────────────────────────────────────── */}
      {(loading || popular.length > 0) && (
        <section>
          <div className="flex items-center justify-between mb-4">
            <SectionTitle>
              <span>⭐</span> Çok Beğenilenler
            </SectionTitle>
            <button
              onClick={() => navigate('/products')}
              className="text-sm font-semibold hover:underline"
              style={{ color: '#e91e8c' }}
            >
              Tümünü Gör →
            </button>
          </div>
          <ProductGrid products={popular} loading={loading} />
        </section>
      )}

    </div>
  );
}

function SectionTitle({ children }) {
  return (
    <h2 className="text-lg font-bold text-gray-800 mb-4 flex items-center gap-2">{children}</h2>
  );
}

function ProductGrid({ products, loading }) {
  if (loading) {
    return (
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
        {Array.from({ length: 8 }, (_, i) => <SkeletonCard key={i} />)}
      </div>
    );
  }
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4">
      {products.map(p => <ProductCard key={p.id} product={p} />)}
    </div>
  );
}

function PromoBanner({ icon, title, subtitle, color, onClick }) {
  return (
    <button
      onClick={onClick}
      className="rounded-xl p-6 flex items-center gap-4 text-left hover:opacity-95 transition-opacity w-full"
      style={{ background: color + '12', border: `1.5px solid ${color}25` }}
    >
      <div
        className="w-14 h-14 rounded-xl flex items-center justify-center text-3xl flex-shrink-0"
        style={{ background: color + '20' }}
      >
        {icon}
      </div>
      <div>
        <div className="font-bold text-gray-800 text-sm">{title}</div>
        <div className="text-xs text-gray-500 mt-0.5">{subtitle}</div>
        <div className="text-xs font-semibold mt-1.5" style={{ color }}>Keşfet →</div>
      </div>
    </button>
  );
}
