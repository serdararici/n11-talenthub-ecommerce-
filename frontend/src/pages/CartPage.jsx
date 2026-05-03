import { useNavigate, Link } from 'react-router-dom';
import { useCart } from '../contexts/CartContext';
import { useToast } from '../contexts/ToastContext';
import { useAuth } from '../contexts/AuthContext';
import ProductPlaceholder from '../components/ui/ProductPlaceholder';

const FREE_SHIPPING_THRESHOLD = 500;

function formatPrice(n) {
  return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: 2 }) + ' TL';
}

export default function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { cart, updateCart, removeFromCart, clearCart, cartTotal } = useCart();
  const { toast } = useToast();

  const items = cart.items || [];
  const shippingCost = cartTotal >= FREE_SHIPPING_THRESHOLD || items.some(i => i.product?.freeShipping) ? 0 : 29.90;
  const grandTotal   = cartTotal + shippingCost;
  const remaining    = FREE_SHIPPING_THRESHOLD - cartTotal;

  const handleRemove = (item) => {
    removeFromCart(item.productId);
    toast(`${item.product?.name?.slice(0, 28)}… sepetten çıkarıldı`, 'info');
  };

  const handleCheckout = () => {
    if (!user) {
      navigate('/login', { state: { from: { pathname: '/checkout' } } });
    } else {
      navigate('/checkout');
    }
  };

  if (items.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-24 text-center">
        <div className="text-7xl mb-5">🛒</div>
        <h2 className="text-xl font-bold text-gray-800 mb-2">Sepetiniz boş</h2>
        <p className="text-sm text-gray-400 mb-6">Ürünleri keşfetmeye başlayın ve sepetinize ekleyin.</p>
        <button
          onClick={() => navigate('/products')}
          className="px-6 py-2.5 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity"
          style={{ background: '#e91e8c' }}
        >
          Alışverişe Başla
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h1 className="text-xl font-bold text-gray-800">Sepetim ({items.length} ürün)</h1>

      <div className="flex gap-6 items-start">

        {/* ── Item list ────────────────────────────────────────────────── */}
        <div className="flex-1 min-w-0 space-y-3">

          {/* Free shipping progress */}
          {remaining > 0 && (
            <div className="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3">
              <div className="text-xs font-semibold text-amber-700 mb-1.5">
                Ücretsiz kargo için <span className="font-extrabold">{formatPrice(remaining)}</span> daha ekleyin!
              </div>
              <div className="h-1.5 bg-amber-200 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full bg-amber-500 transition-all"
                  style={{ width: `${Math.min(100, (cartTotal / FREE_SHIPPING_THRESHOLD) * 100)}%` }}
                />
              </div>
            </div>
          )}
          {remaining <= 0 && (
            <div className="bg-green-50 border border-green-200 rounded-xl px-4 py-2.5 text-xs font-semibold text-green-700">
              ✓ Siparişinizde ücretsiz kargo kazandınız!
            </div>
          )}

          {items.map(item => (
            <CartItem
              key={item.productId}
              item={item}
              onUpdate={qty => updateCart(item.productId, qty)}
              onRemove={() => handleRemove(item)}
            />
          ))}

          <div className="flex justify-end">
            <button
              onClick={() => { clearCart(); toast('Sepet temizlendi', 'info'); }}
              className="text-xs text-red-500 hover:underline font-medium"
            >
              Sepeti Temizle
            </button>
          </div>
        </div>

        {/* ── Order summary ─────────────────────────────────────────────── */}
        <div className="w-72 flex-shrink-0 space-y-3">
          <div className="bg-white rounded-2xl shadow-sm p-5 space-y-3">
            <div className="font-bold text-gray-800 text-base">Sipariş Özeti</div>

            <div className="space-y-2 text-sm">
              <Row label="Ara Toplam"  value={formatPrice(cartTotal)} />
              <Row
                label="Kargo"
                value={shippingCost === 0 ? 'Ücretsiz' : formatPrice(shippingCost)}
                valueClass={shippingCost === 0 ? 'text-green-600 font-semibold' : ''}
              />
            </div>

            <div className="border-t border-gray-100 pt-3 flex justify-between items-center">
              <span className="font-bold text-gray-800">Toplam</span>
              <span className="text-xl font-extrabold" style={{ color: '#e91e8c' }}>
                {formatPrice(grandTotal)}
              </span>
            </div>

            <button
              onClick={handleCheckout}
              className="w-full py-3 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity"
              style={{ background: '#e91e8c' }}
            >
              Siparişi Tamamla →
            </button>

            {!user && (
              <p className="text-[11px] text-center text-gray-400">
                Devam etmek için <Link to="/login" className="underline" style={{ color: '#e91e8c' }}>giriş yapın</Link>
              </p>
            )}
          </div>

          {/* Safe shopping badges */}
          <div className="bg-white rounded-2xl shadow-sm p-4">
            <div className="font-semibold text-xs text-gray-600 mb-2">Güvenli Alışveriş</div>
            <div className="space-y-1.5">
              {[
                ['🔒', 'SSL ile şifreli ödeme'],
                ['↩️', '30 gün iade garantisi'],
                ['🚚', 'Hızlı ve güvenli kargo'],
              ].map(([icon, text]) => (
                <div key={text} className="flex items-center gap-2 text-xs text-gray-500">
                  <span>{icon}</span><span>{text}</span>
                </div>
              ))}
            </div>
          </div>

          <button
            onClick={() => navigate('/products')}
            className="w-full py-2.5 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
          >
            ← Alışverişe Devam Et
          </button>
        </div>

      </div>
    </div>
  );
}

function CartItem({ item, onUpdate, onRemove }) {
  const { product, quantity } = item;
  if (!product) return null;

  const subtotal = product.price * quantity;

  return (
    <div className="bg-white rounded-2xl shadow-sm p-4 flex gap-4">
      {/* Image */}
      <Link to={`/products/${product.id}`} className="flex-shrink-0 w-20 h-20 bg-gray-50 rounded-xl flex items-center justify-center overflow-hidden">
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.name} className="h-full object-contain" />
          : <ProductPlaceholder category={product.category} size={80} />
        }
      </Link>

      {/* Info */}
      <div className="flex-1 min-w-0">
        {product.brand && <div className="text-[11px] text-gray-400 font-medium">{product.brand}</div>}
        <Link
          to={`/products/${product.id}`}
          className="text-sm font-semibold text-gray-800 leading-snug line-clamp-2 hover:text-brand transition-colors"
        >
          {product.name}
        </Link>
        {product.freeShipping && (
          <div className="text-[10px] font-semibold text-green-600 mt-0.5">Ücretsiz Kargo</div>
        )}
      </div>

      {/* Right: qty + price + remove */}
      <div className="flex flex-col items-end justify-between flex-shrink-0">
        <button
          onClick={onRemove}
          className="text-gray-300 hover:text-red-500 transition-colors text-lg leading-none"
          title="Kaldır"
        >
          ×
        </button>

        <div className="text-right">
          <div className="text-base font-extrabold" style={{ color: '#e91e8c' }}>
            {formatPrice(subtotal)}
          </div>
          {quantity > 1 && (
            <div className="text-[11px] text-gray-400">{formatPrice(product.price)} / adet</div>
          )}
        </div>

        {/* Qty controls */}
        <div className="flex items-center border border-gray-200 rounded-lg overflow-hidden">
          <button
            onClick={() => onUpdate(quantity - 1)}
            className="w-7 h-7 text-gray-500 hover:bg-gray-50 font-bold text-base transition-colors"
          >−</button>
          <span className="w-7 text-center text-xs font-semibold text-gray-800">{quantity}</span>
          <button
            onClick={() => onUpdate(quantity + 1)}
            className="w-7 h-7 text-gray-500 hover:bg-gray-50 font-bold text-base transition-colors"
          >+</button>
        </div>
      </div>
    </div>
  );
}

function Row({ label, value, valueClass = '' }) {
  return (
    <div className="flex justify-between text-gray-600">
      <span>{label}</span>
      <span className={`font-medium text-gray-800 ${valueClass}`}>{value}</span>
    </div>
  );
}
