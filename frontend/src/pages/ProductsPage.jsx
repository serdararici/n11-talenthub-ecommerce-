import { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { fetchProducts } from '../services/api';
import { CATEGORY_META } from '../services/mockData';
import ProductCard from '../components/product/ProductCard';
import SkeletonCard from '../components/ui/SkeletonCard';

const PAGE_SIZE = 12;

const SORT_OPTIONS = [
  { value: 'default',    label: 'Önerilen'          },
  { value: 'price_asc',  label: 'Fiyat: Düşükten Yükseğe' },
  { value: 'price_desc', label: 'Fiyat: Yüksekten Düşüğe' },
  { value: 'rating',     label: 'En Çok Beğenilen'  },
  { value: 'discount',   label: 'En Fazla İndirim'   },
];

function applySort(items, sort) {
  const arr = [...items];
  if (sort === 'price_asc')  return arr.sort((a, b) => a.price - b.price);
  if (sort === 'price_desc') return arr.sort((a, b) => b.price - a.price);
  if (sort === 'rating')     return arr.sort((a, b) => (b.rating || 0) - (a.rating || 0));
  if (sort === 'discount') {
    return arr.sort((a, b) => {
      const da = a.originalPrice ? (1 - a.price / a.originalPrice) : 0;
      const db = b.originalPrice ? (1 - b.price / b.originalPrice) : 0;
      return db - da;
    });
  }
  return arr;
}

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();

  const categoryParam = searchParams.get('category') || '';
  const searchParam   = searchParams.get('search')   || '';
  const pageParam     = Number(searchParams.get('page') || 0);

  const [products,    setProducts]    = useState([]);
  const [totalPages,  setTotalPages]  = useState(1);
  const [totalItems,  setTotalItems]  = useState(0);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState('');
  const [sort,        setSort]        = useState('default');
  const [priceMax,    setPriceMax]    = useState('');
  const [onlyDiscount,setOnlyDiscount]= useState(false);
  const [onlyFreeShip,setOnlyFreeShip]= useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchProducts({
        category: categoryParam || undefined,
        search:   searchParam   || undefined,
        page:     pageParam,
        size:     PAGE_SIZE,
      });
      let items = data.content || [];
      if (priceMax)      items = items.filter(p => p.price <= Number(priceMax));
      if (onlyDiscount)  items = items.filter(p => p.originalPrice && p.originalPrice > p.price);
      if (onlyFreeShip)  items = items.filter(p => p.freeShipping);
      setProducts(applySort(items, sort));
      setTotalPages(data.totalPages || 1);
      setTotalItems(data.totalElements || items.length);
    } catch (e) {
      setError(e.message || 'Ürünler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [categoryParam, searchParam, pageParam, sort, priceMax, onlyDiscount, onlyFreeShip]);

  useEffect(() => { load(); }, [load]);

  const setPage = (p) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', p);
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const setCategory = (cat) => {
    const next = new URLSearchParams();
    if (cat) next.set('category', cat);
    if (searchParam) next.set('search', searchParam);
    setSearchParams(next);
  };

  const clearFilters = () => {
    setPriceMax('');
    setOnlyDiscount(false);
    setOnlyFreeShip(false);
    setSort('default');
  };

  const hasActiveFilter = priceMax || onlyDiscount || onlyFreeShip;

  const pageTitle = categoryParam
    ? categoryParam
    : searchParam
      ? `"${searchParam}" için sonuçlar`
      : 'Tüm Ürünler';

  return (
    <div className="flex gap-6">

      {/* ── Sidebar ──────────────────────────────────────────────────────── */}
      <aside className="w-52 flex-shrink-0 space-y-5">

        {/* Categories */}
        <div className="bg-white rounded-xl p-4 shadow-sm">
          <div className="font-bold text-sm text-gray-800 mb-3">Kategoriler</div>
          <button
            onClick={() => setCategory('')}
            className={`w-full text-left text-sm px-2.5 py-1.5 rounded-lg mb-1 transition-colors ${
              !categoryParam ? 'font-semibold' : 'text-gray-600 hover:bg-gray-50'
            }`}
            style={!categoryParam ? { color: '#e91e8c', background: '#fff5fb' } : {}}
          >
            Tümü
          </button>
          {CATEGORY_META.map(cat => (
            <button
              key={cat.id}
              onClick={() => setCategory(cat.id)}
              className={`w-full text-left text-sm px-2.5 py-1.5 rounded-lg mb-0.5 flex items-center gap-2 transition-colors ${
                categoryParam === cat.id ? 'font-semibold' : 'text-gray-600 hover:bg-gray-50'
              }`}
              style={categoryParam === cat.id ? { color: cat.color, background: cat.color + '12' } : {}}
            >
              <span>{cat.icon}</span>
              <span>{cat.id}</span>
            </button>
          ))}
        </div>

        {/* Filters */}
        <div className="bg-white rounded-xl p-4 shadow-sm space-y-4">
          <div className="flex items-center justify-between">
            <div className="font-bold text-sm text-gray-800">Filtreler</div>
            {hasActiveFilter && (
              <button onClick={clearFilters} className="text-[11px] text-red-500 hover:underline">Temizle</button>
            )}
          </div>

          {/* Max price */}
          <div>
            <div className="text-xs font-semibold text-gray-600 mb-1.5">Maks. Fiyat (TL)</div>
            <input
              type="number"
              value={priceMax}
              onChange={e => { setPriceMax(e.target.value); setPage(0); }}
              placeholder="örn. 5000"
              className="w-full border border-gray-200 rounded-lg px-3 py-1.5 text-sm outline-none focus:border-brand"
            />
          </div>

          {/* Checkboxes */}
          <label className="flex items-center gap-2 cursor-pointer text-sm text-gray-700">
            <input
              type="checkbox"
              checked={onlyDiscount}
              onChange={e => { setOnlyDiscount(e.target.checked); setPage(0); }}
              className="accent-brand"
            />
            Sadece İndirimli
          </label>
          <label className="flex items-center gap-2 cursor-pointer text-sm text-gray-700">
            <input
              type="checkbox"
              checked={onlyFreeShip}
              onChange={e => { setOnlyFreeShip(e.target.checked); setPage(0); }}
              className="accent-brand"
            />
            Ücretsiz Kargo
          </label>
        </div>
      </aside>

      {/* ── Main ─────────────────────────────────────────────────────────── */}
      <div className="flex-1 min-w-0">

        {/* Toolbar */}
        <div className="flex items-center justify-between mb-4 flex-wrap gap-2">
          <div>
            <h1 className="font-bold text-gray-800 text-lg">{pageTitle}</h1>
            {!loading && !error && (
              <div className="text-xs text-gray-400 mt-0.5">{totalItems} ürün bulundu</div>
            )}
          </div>
          <select
            value={sort}
            onChange={e => setSort(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-1.5 text-sm text-gray-700 outline-none focus:border-brand"
          >
            {SORT_OPTIONS.map(o => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm mb-4">
            {error}
            <button onClick={load} className="ml-2 underline font-medium">Tekrar dene</button>
          </div>
        )}

        {/* Grid */}
        {loading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {Array.from({ length: PAGE_SIZE }, (_, i) => <SkeletonCard key={i} />)}
          </div>
        ) : products.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20 text-center">
            <div className="text-5xl mb-4">🔍</div>
            <div className="font-semibold text-gray-700 text-lg mb-1">Ürün bulunamadı</div>
            <div className="text-sm text-gray-400 mb-5">Farklı filtreler veya arama terimi deneyin</div>
            <button
              onClick={() => { clearFilters(); setCategory(''); navigate('/products'); }}
              className="px-4 py-2 rounded-lg text-sm font-semibold text-white"
              style={{ background: '#e91e8c' }}
            >
              Filtreleri Temizle
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {products.map(p => <ProductCard key={p.id} product={p} />)}
          </div>
        )}

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="flex justify-center items-center gap-1.5 mt-8">
            <PageButton disabled={pageParam === 0} onClick={() => setPage(pageParam - 1)}>‹</PageButton>
            {Array.from({ length: totalPages }, (_, i) => (
              <PageButton
                key={i}
                active={i === pageParam}
                onClick={() => setPage(i)}
              >
                {i + 1}
              </PageButton>
            ))}
            <PageButton disabled={pageParam >= totalPages - 1} onClick={() => setPage(pageParam + 1)}>›</PageButton>
          </div>
        )}
      </div>
    </div>
  );
}

function PageButton({ children, active, disabled, onClick }) {
  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`w-8 h-8 rounded-lg text-sm font-semibold transition-colors ${
        active
          ? 'text-white'
          : disabled
            ? 'text-gray-300 cursor-not-allowed bg-white'
            : 'text-gray-600 bg-white hover:bg-gray-50'
      }`}
      style={active ? { background: '#e91e8c' } : {}}
    >
      {children}
    </button>
  );
}
