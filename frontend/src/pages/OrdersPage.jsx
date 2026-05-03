import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { orderList } from '../services/api';

function formatPrice(n) {
  return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: 2 }) + ' TL';
}

function formatDate(str) {
  if (!str) return '';
  return new Date(str).toLocaleDateString('tr-TR', { day: '2-digit', month: 'long', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

const STATUS_META = {
  PENDING:    { label: 'Beklemede',       color: '#d97706', bg: '#fffbeb', border: '#fde68a' },
  CONFIRMED:  { label: 'Onaylandı',       color: '#2563eb', bg: '#eff6ff', border: '#bfdbfe' },
  SHIPPED:    { label: 'Kargoda',         color: '#7c3aed', bg: '#f5f3ff', border: '#ddd6fe' },
  DELIVERED:  { label: 'Teslim Edildi',   color: '#16a34a', bg: '#f0fdf4', border: '#bbf7d0' },
  CANCELLED:  { label: 'İptal Edildi',    color: '#dc2626', bg: '#fef2f2', border: '#fecaca' },
};

const STATUS_STEPS = ['PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];

export default function OrdersPage() {
  const navigate = useNavigate();
  const [orders,  setOrders]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');

  useEffect(() => {
    orderList()
      .then(data => setOrders(Array.isArray(data) ? data : (data?.content || [])))
      .catch(e => {
        if (e.message === '__NETWORK__') {
          setOrders([]);
        } else {
          setError(e.message || 'Siparişler yüklenemedi');
        }
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <OrdersSkeleton />;

  if (error) return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="text-4xl mb-3">⚠️</div>
      <div className="font-semibold text-gray-700">{error}</div>
      <button onClick={() => window.location.reload()} className="mt-4 text-sm underline" style={{ color: '#e91e8c' }}>
        Tekrar dene
      </button>
    </div>
  );

  if (orders.length === 0) return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <div className="text-7xl mb-5">📦</div>
      <h2 className="text-xl font-bold text-gray-800 mb-2">Henüz siparişiniz yok</h2>
      <p className="text-sm text-gray-400 mb-6">İlk siparişinizi vermek için alışverişe başlayın.</p>
      <button
        onClick={() => navigate('/products')}
        className="px-6 py-2.5 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity"
        style={{ background: '#e91e8c' }}
      >
        Alışverişe Başla
      </button>
    </div>
  );

  return (
    <div className="space-y-4 max-w-3xl">
      <h1 className="text-xl font-bold text-gray-800">Siparişlerim</h1>
      <p className="text-sm text-gray-400">{orders.length} sipariş bulundu</p>

      {orders.map(order => (
        <OrderCard key={order.id} order={order} />
      ))}
    </div>
  );
}

function OrderCard({ order }) {
  const [expanded, setExpanded] = useState(false);
  const status = STATUS_META[order.status] || STATUS_META.PENDING;
  const isCancelled = order.status === 'CANCELLED';
  const stepIndex   = STATUS_STEPS.indexOf(order.status);
  const items       = order.items || order.orderItems || [];

  return (
    <div className="bg-white rounded-2xl shadow-sm overflow-hidden">

      {/* Header */}
      <div className="px-5 py-4 flex items-center justify-between gap-3 flex-wrap">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2">
            <span className="font-bold text-gray-800 text-sm">
              Sipariş #{order.id || order.orderId}
            </span>
            <span
              className="text-[11px] font-bold px-2 py-0.5 rounded-full"
              style={{ color: status.color, background: status.bg, border: `1px solid ${status.border}` }}
            >
              {status.label}
            </span>
          </div>
          {order.createdAt && (
            <div className="text-xs text-gray-400">{formatDate(order.createdAt)}</div>
          )}
        </div>

        <div className="flex items-center gap-4">
          <div className="text-right">
            <div className="text-lg font-extrabold" style={{ color: '#e91e8c' }}>
              {formatPrice(order.totalAmount || order.total || 0)}
            </div>
            {items.length > 0 && (
              <div className="text-xs text-gray-400">{items.length} ürün</div>
            )}
          </div>
          <button
            onClick={() => setExpanded(v => !v)}
            className="text-sm font-semibold px-3 py-1.5 rounded-lg border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
          >
            {expanded ? 'Gizle ▲' : 'Detay ▼'}
          </button>
        </div>
      </div>

      {/* Progress tracker */}
      {!isCancelled && (
        <div className="px-5 pb-4">
          <div className="flex items-center gap-0">
            {STATUS_STEPS.map((s, i) => {
              const done   = i <= stepIndex;
              const active = i === stepIndex;
              const meta   = STATUS_META[s];
              return (
                <div key={s} className="flex items-center flex-1 last:flex-none">
                  <div className="flex flex-col items-center">
                    <div
                      className="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-bold transition-colors"
                      style={{
                        background: done ? meta.color : '#e5e7eb',
                        color: done ? '#fff' : '#9ca3af',
                        boxShadow: active ? `0 0 0 3px ${meta.color}30` : 'none',
                      }}
                    >
                      {done && !active ? '✓' : i + 1}
                    </div>
                    <div className="text-[9px] font-medium mt-1 whitespace-nowrap" style={{ color: done ? meta.color : '#9ca3af' }}>
                      {meta.label}
                    </div>
                  </div>
                  {i < STATUS_STEPS.length - 1 && (
                    <div
                      className="flex-1 h-0.5 mx-1 mb-3.5 transition-colors"
                      style={{ background: i < stepIndex ? meta.color : '#e5e7eb' }}
                    />
                  )}
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Expanded items */}
      {expanded && (
        <div className="border-t border-gray-100 px-5 py-4 space-y-3">
          {order.shippingAddress && (
            <div className="text-xs text-gray-500">
              <span className="font-semibold text-gray-600">Teslimat Adresi: </span>
              {order.shippingAddress}
            </div>
          )}

          {items.length > 0 ? (
            <div className="space-y-2">
              <div className="text-xs font-semibold text-gray-600">Ürünler</div>
              {items.map((item, idx) => (
                <div key={idx} className="flex items-center justify-between text-sm py-1.5 border-b border-gray-50 last:border-0">
                  <div className="flex items-center gap-2.5">
                    <span className="w-5 h-5 rounded bg-gray-100 text-[10px] font-bold text-gray-500 flex items-center justify-center flex-shrink-0">
                      {item.quantity}
                    </span>
                    <span className="text-gray-700">{item.productName || item.name || `Ürün #${item.productId}`}</span>
                  </div>
                  <span className="font-semibold text-gray-800">
                    {formatPrice((item.price || 0) * (item.quantity || 1))}
                  </span>
                </div>
              ))}
            </div>
          ) : (
            <div className="text-xs text-gray-400">Ürün detayı mevcut değil.</div>
          )}

          {order.paymentMethod && (
            <div className="text-xs text-gray-500">
              <span className="font-semibold text-gray-600">Ödeme Yöntemi: </span>
              {order.paymentMethod === 'card' ? 'Kredi / Banka Kartı' : 'Havale / EFT'}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function OrdersSkeleton() {
  return (
    <div className="space-y-4 max-w-3xl">
      <div className="skeleton h-6 w-40 rounded" />
      {Array.from({ length: 3 }, (_, i) => (
        <div key={i} className="bg-white rounded-2xl shadow-sm p-5 space-y-3">
          <div className="flex justify-between">
            <div className="space-y-2">
              <div className="skeleton h-4 w-36 rounded" />
              <div className="skeleton h-3 w-24 rounded" />
            </div>
            <div className="space-y-2 items-end flex flex-col">
              <div className="skeleton h-5 w-24 rounded" />
              <div className="skeleton h-8 w-20 rounded-lg" />
            </div>
          </div>
          <div className="flex gap-2 pt-1">
            {Array.from({ length: 4 }, (_, j) => (
              <div key={j} className="flex-1 flex flex-col items-center gap-1">
                <div className="skeleton w-6 h-6 rounded-full" />
                <div className="skeleton h-2 w-12 rounded" />
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
