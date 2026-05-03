import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { fetchProduct } from '../services/api';
import { useCart } from '../contexts/CartContext';
import { useToast } from '../contexts/ToastContext';
import ProductPlaceholder from '../components/ui/ProductPlaceholder';
import Stars from '../components/ui/Stars';

function formatPrice(n) {
  return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: 2 }) + ' TL';
}

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart, wishlist, toggleWishlist } = useCart();
  const { toast } = useToast();

  const [product,  setProduct]  = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');
  const [qty,      setQty]      = useState(1);

  useEffect(() => {
    setLoading(true);
    setError('');
    setQty(1);
    fetchProduct(id)
      .then(setProduct)
      .catch(e => setError(e.message || 'Ürün yüklenemedi'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <DetailSkeleton />;

  if (error) return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="text-5xl mb-4">😕</div>
      <div className="font-semibold text-gray-700 text-lg mb-1">{error}</div>
      <button
        onClick={() => navigate(-1)}
        className="mt-4 px-5 py-2 rounded-lg text-sm font-semibold text-white"
        style={{ background: '#e91e8c' }}
      >
        Geri Dön
      </button>
    </div>
  );

  if (!product) return null;

  const discount = product.originalPrice && product.originalPrice > product.price
    ? Math.round((1 - product.price / product.originalPrice) * 100)
    : 0;
  const isWished = wishlist.includes(product.id);
  const inStock  = !product.stockQuantity || product.stockQuantity > 0;

  const handleAddToCart = () => {
    for (let i = 0; i < qty; i++) addToCart(product);
    toast(`${product.name.slice(0, 30)}… sepete eklendi`, 'success');
  };

  return (
    <div className="space-y-6">

      {/* Breadcrumb */}
      <nav className="text-xs text-gray-400 flex items-center gap-1.5 flex-wrap">
        <Link to="/" className="hover:text-brand transition-colors">Ana Sayfa</Link>
        <span>›</span>
        <Link
          to={`/products?category=${encodeURIComponent(product.category)}`}
          className="hover:text-brand transition-colors"
        >
          {product.category}
        </Link>
        <span>›</span>
        <span className="text-gray-600 truncate max-w-xs">{product.name}</span>
      </nav>

      {/* Main card */}
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div className="grid md:grid-cols-2 gap-0">

          {/* Image panel */}
          <div className="bg-gray-50 flex items-center justify-center p-10 relative min-h-80">
            {/* Badges */}
            {product.badge && (
              <div className="absolute top-4 left-4 text-white text-[10px] font-extrabold px-2 py-1 rounded uppercase tracking-wide" style={{ background: '#e91e8c' }}>
                {product.badge}
              </div>
            )}
            {discount > 0 && (
              <div className="absolute text-white text-xs font-bold px-2 py-1 rounded bg-red-600"
                style={{ top: product.badge ? 36 : 16, left: 16 }}>
                %{discount} İndirim
              </div>
            )}
            {/* Wishlist */}
            <button
              onClick={() => toggleWishlist(product.id)}
              className="absolute top-4 right-4 w-9 h-9 rounded-full bg-white shadow flex items-center justify-center text-xl transition-transform hover:scale-110"
            >
              {isWished ? '❤️' : '🤍'}
            </button>

            {product.imageUrl
              ? <img src={product.imageUrl} alt={product.name} className="max-h-72 object-contain" />
              : <ProductPlaceholder category={product.category} size={240} />
            }
          </div>

          {/* Info panel */}
          <div className="p-7 flex flex-col gap-4">
            {product.brand && (
              <div className="text-xs font-semibold text-gray-400 uppercase tracking-widest">{product.brand}</div>
            )}

            <h1 className="text-xl font-bold text-gray-900 leading-snug">{product.name}</h1>

            {/* Rating */}
            {product.rating != null && (
              <div className="flex items-center gap-2">
                <Stars rating={product.rating} />
                <span className="text-sm font-semibold text-gray-700">{product.rating}</span>
                <span className="text-sm text-gray-400">({(product.reviewCount || 0).toLocaleString('tr-TR')} değerlendirme)</span>
              </div>
            )}

            {/* Price */}
            <div className="flex items-end gap-3">
              <div className="text-3xl font-extrabold" style={{ color: '#e91e8c' }}>
                {formatPrice(product.price)}
              </div>
              {discount > 0 && (
                <div className="text-base text-gray-400 line-through mb-1">
                  {formatPrice(product.originalPrice)}
                </div>
              )}
              {discount > 0 && (
                <div className="mb-1 bg-red-100 text-red-700 text-xs font-bold px-2 py-0.5 rounded">
                  %{discount}
                </div>
              )}
            </div>

            {/* Badges row */}
            <div className="flex flex-wrap gap-2">
              {product.freeShipping && (
                <span className="bg-green-50 text-green-700 border border-green-200 text-xs font-semibold px-2.5 py-1 rounded-full">
                  ✓ Ücretsiz Kargo
                </span>
              )}
              <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${
                inStock
                  ? 'bg-blue-50 text-blue-700 border border-blue-200'
                  : 'bg-red-50 text-red-700 border border-red-200'
              }`}>
                {inStock
                  ? product.stockQuantity
                    ? `Stokta ${product.stockQuantity} adet`
                    : 'Stokta Var'
                  : 'Stokta Yok'}
              </span>
            </div>

            <div className="border-t border-gray-100 my-1" />

            {/* Quantity + Cart */}
            <div className="flex items-center gap-3">
              <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
                <button
                  onClick={() => setQty(q => Math.max(1, q - 1))}
                  className="w-9 h-9 text-lg font-bold text-gray-600 hover:bg-gray-50 transition-colors"
                >−</button>
                <span className="w-9 text-center text-sm font-semibold text-gray-800">{qty}</span>
                <button
                  onClick={() => setQty(q => Math.min(product.stockQuantity || 99, q + 1))}
                  className="w-9 h-9 text-lg font-bold text-gray-600 hover:bg-gray-50 transition-colors"
                >+</button>
              </div>

              <button
                onClick={handleAddToCart}
                disabled={!inStock}
                className="flex-1 py-2.5 rounded-xl text-sm font-bold text-white transition-opacity disabled:opacity-50 disabled:cursor-not-allowed hover:opacity-90"
                style={{ background: inStock ? '#e91e8c' : '#9ca3af' }}
              >
                🛒 Sepete Ekle
              </button>

              <button
                onClick={() => { handleAddToCart(); navigate('/cart'); }}
                disabled={!inStock}
                className="flex-1 py-2.5 rounded-xl text-sm font-bold border-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed hover:bg-pink-50"
                style={{ color: '#e91e8c', borderColor: '#e91e8c' }}
              >
                Hemen Al
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Description */}
      {product.description && (
        <div className="bg-white rounded-2xl shadow-sm p-6">
          <h2 className="font-bold text-gray-800 text-base mb-3">Ürün Açıklaması</h2>
          <p className="text-sm text-gray-600 leading-relaxed">{product.description}</p>
        </div>
      )}

      {/* Details table */}
      <div className="bg-white rounded-2xl shadow-sm p-6">
        <h2 className="font-bold text-gray-800 text-base mb-3">Ürün Bilgileri</h2>
        <table className="w-full text-sm">
          <tbody>
            {[
              ['Kategori',   product.category],
              product.brand  && ['Marka', product.brand],
              ['Kargo',      product.freeShipping ? 'Ücretsiz Kargo' : 'Standart Kargo'],
              product.stockQuantity != null && ['Stok', product.stockQuantity > 0 ? `${product.stockQuantity} adet` : 'Tükendi'],
            ].filter(Boolean).map(([label, value]) => (
              <tr key={label} className="border-b border-gray-50 last:border-0">
                <td className="py-2.5 pr-4 font-medium text-gray-500 w-36">{label}</td>
                <td className="py-2.5 text-gray-800">{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </div>
  );
}

function DetailSkeleton() {
  return (
    <div className="space-y-6">
      <div className="skeleton h-4 w-64 rounded" />
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <div className="grid md:grid-cols-2">
          <div className="skeleton min-h-80" />
          <div className="p-7 flex flex-col gap-4">
            <div className="skeleton h-3 w-20 rounded" />
            <div className="skeleton h-6 w-full rounded" />
            <div className="skeleton h-5 w-3/4 rounded" />
            <div className="skeleton h-4 w-1/3 rounded" />
            <div className="skeleton h-8 w-1/2 rounded" />
            <div className="flex gap-2">
              <div className="skeleton h-6 w-28 rounded-full" />
              <div className="skeleton h-6 w-24 rounded-full" />
            </div>
            <div className="flex gap-3 mt-4">
              <div className="skeleton h-10 w-28 rounded-lg" />
              <div className="skeleton h-10 flex-1 rounded-xl" />
              <div className="skeleton h-10 flex-1 rounded-xl" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
