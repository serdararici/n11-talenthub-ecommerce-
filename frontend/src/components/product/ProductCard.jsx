import { useNavigate } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { useToast } from '../../contexts/ToastContext';
import ProductPlaceholder from '../ui/ProductPlaceholder';
import Stars from '../ui/Stars';

function formatPrice(n) {
  return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: 2 }) + ' TL';
}

export default function ProductCard({ product }) {
  const navigate = useNavigate();
  const { addToCart, wishlist, toggleWishlist } = useCart();
  const { toast } = useToast();
  const isWished = wishlist.includes(product.id);
  const discount = product.originalPrice && product.originalPrice > product.price
    ? Math.round((1 - product.price / product.originalPrice) * 100)
    : 0;

  const handleAdd = (e) => {
    e.stopPropagation();
    addToCart(product);
    toast(`${product.name.slice(0, 28)}… sepete eklendi`, 'success');
  };

  return (
    <div
      className="bg-white rounded-xl shadow-sm cursor-pointer overflow-hidden flex flex-col relative transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg"
      onClick={() => navigate(`/products/${product.id}`)}
    >
      {/* Wishlist */}
      <button
        onClick={e => { e.stopPropagation(); toggleWishlist(product.id); }}
        className="absolute top-2 right-2 z-10 w-7 h-7 rounded-full bg-white shadow flex items-center justify-center text-base transition-transform hover:scale-110"
      >
        {isWished ? '❤️' : '🤍'}
      </button>

      {/* Promo badge */}
      {product.badge && (
        <div className="absolute top-2 left-2 z-10 text-white text-[9px] font-extrabold px-1.5 py-0.5 rounded uppercase tracking-wide" style={{ background: '#e91e8c' }}>
          {product.badge}
        </div>
      )}

      {/* Discount badge */}
      {discount > 0 && (
        <div className="absolute z-10 text-white text-[10px] font-bold px-1.5 py-0.5 rounded bg-red-600" style={{ top: product.badge ? 26 : 8, left: 8 }}>
          %{discount}
        </div>
      )}

      {/* Image */}
      <div className="bg-gray-50 flex items-center justify-center p-4 h-44">
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.name} className="h-full object-contain" />
          : <ProductPlaceholder category={product.category} size={148} />
        }
      </div>

      {/* Free shipping */}
      {product.freeShipping && (
        <div className="bg-green-50 border-t border-green-100 py-1 text-center text-[10px] font-semibold text-green-700">
          ÜCRETSİZ KARGO
        </div>
      )}

      {/* Info */}
      <div className="p-3 flex flex-col gap-1.5 flex-1">
        {product.brand && <div className="text-xs text-gray-500 font-medium">{product.brand}</div>}
        <div className="text-sm text-gray-800 leading-snug line-clamp-2">{product.name}</div>
        {product.rating != null && (
          <div className="flex items-center gap-1">
            <Stars rating={product.rating} />
            <span className="text-[11px] text-gray-400">({(product.reviewCount || 0).toLocaleString('tr-TR')})</span>
          </div>
        )}
        <div className="mt-auto pt-1.5 flex items-end justify-between">
          <div>
            {discount > 0 && (
              <div className="text-[11px] text-gray-400 line-through">{formatPrice(product.originalPrice)}</div>
            )}
            <div className="text-base font-bold" style={{ color: '#e91e8c' }}>{formatPrice(product.price)}</div>
          </div>
          <button
            onClick={handleAdd}
            className="w-8 h-8 rounded-lg text-white text-xl font-bold flex items-center justify-center transition-colors"
            style={{ background: '#e91e8c' }}
            onMouseEnter={e => e.currentTarget.style.background='#c4177a'}
            onMouseLeave={e => e.currentTarget.style.background='#e91e8c'}
          >
            +
          </button>
        </div>
      </div>
    </div>
  );
}
