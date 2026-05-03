import { useState, useEffect, useCallback } from 'react';
import { fetchProducts, adminCreateProduct, adminUpdateProduct, adminDeleteProduct } from '../../services/api';
import { CATEGORY_META } from '../../services/mockData';
import ProductPlaceholder from '../../components/ui/ProductPlaceholder';

const PAGE_SIZE = 15;

const EMPTY_FORM = {
  name: '', brand: '', category: '', price: '', originalPrice: '',
  stockQuantity: '', description: '', imageUrl: '', badge: '', freeShipping: false,
};

function formatPrice(n) {
  return Number(n).toLocaleString('tr-TR', { minimumFractionDigits: 2 }) + ' TL';
}

export default function AdminProductsPage() {
  const [products,   setProducts]   = useState([]);
  const [totalPages, setTotalPages] = useState(1);
  const [page,       setPage]       = useState(0);
  const [loading,    setLoading]    = useState(true);
  const [search,     setSearch]     = useState('');
  const [modalOpen,  setModalOpen]  = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [saving,     setSaving]     = useState(false);
  const [deleting,   setDeleting]   = useState(false);
  const [error,      setError]      = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchProducts({ page, size: PAGE_SIZE, search: search || undefined });
      setProducts(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (e) {
      setError(e.message || 'Ürünler yüklenemedi');
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => { load(); }, [load]);

  const openCreate = () => { setEditTarget(null); setModalOpen(true); };
  const openEdit   = (p)  => { setEditTarget(p);  setModalOpen(true); };
  const closeModal = ()   => { setModalOpen(false); setEditTarget(null); };

  const handleSave = async (formData) => {
    setSaving(true);
    setError('');
    try {
      const payload = {
        ...formData,
        price:         Number(formData.price),
        originalPrice: formData.originalPrice ? Number(formData.originalPrice) : null,
        stockQuantity: Number(formData.stockQuantity),
      };
      if (editTarget) {
        await adminUpdateProduct(editTarget.id, payload);
      } else {
        await adminCreateProduct(payload);
      }
      closeModal();
      load();
    } catch (e) {
      setError(e.message || 'Kayıt başarısız');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    setError('');
    try {
      await adminDeleteProduct(deleteTarget.id);
      setDeleteTarget(null);
      load();
    } catch (e) {
      setError(e.message || 'Silme başarısız');
    } finally {
      setDeleting(false);
    }
  };

  return (
    <div className="space-y-5">

      {/* Header */}
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-xl font-bold text-gray-800">Ürün Yönetimi</h1>
          <p className="text-xs text-gray-400 mt-0.5">Admin Panel</p>
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-2 px-4 py-2.5 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity"
          style={{ background: '#e91e8c' }}
        >
          + Yeni Ürün
        </button>
      </div>

      {/* Error banner */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-2.5 text-sm flex justify-between">
          {error}
          <button onClick={() => setError('')} className="font-bold">×</button>
        </div>
      )}

      {/* Search */}
      <div className="bg-white rounded-xl shadow-sm px-4 py-3 flex gap-3">
        <input
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(0); }}
          placeholder="Ürün adı veya marka ara…"
          className="flex-1 text-sm outline-none text-gray-700 placeholder-gray-400"
        />
        {search && (
          <button onClick={() => { setSearch(''); setPage(0); }} className="text-gray-400 hover:text-gray-600 text-lg leading-none">×</button>
        )}
      </div>

      {/* Table */}
      <div className="bg-white rounded-2xl shadow-sm overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-100 text-left">
              <th className="px-4 py-3 text-xs font-semibold text-gray-500 w-14">Görsel</th>
              <th className="px-4 py-3 text-xs font-semibold text-gray-500">Ürün</th>
              <th className="px-4 py-3 text-xs font-semibold text-gray-500">Kategori</th>
              <th className="px-4 py-3 text-xs font-semibold text-gray-500 text-right">Fiyat</th>
              <th className="px-4 py-3 text-xs font-semibold text-gray-500 text-right">Stok</th>
              <th className="px-4 py-3 text-xs font-semibold text-gray-500 text-center">İşlem</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: 8 }, (_, i) => (
                <tr key={i} className="border-b border-gray-50">
                  <td className="px-4 py-3"><div className="skeleton w-10 h-10 rounded-lg" /></td>
                  <td className="px-4 py-3">
                    <div className="skeleton h-3 w-48 rounded mb-1.5" />
                    <div className="skeleton h-2.5 w-20 rounded" />
                  </td>
                  <td className="px-4 py-3"><div className="skeleton h-3 w-20 rounded" /></td>
                  <td className="px-4 py-3 text-right"><div className="skeleton h-3 w-16 rounded ml-auto" /></td>
                  <td className="px-4 py-3 text-right"><div className="skeleton h-3 w-10 rounded ml-auto" /></td>
                  <td className="px-4 py-3"><div className="skeleton h-7 w-20 rounded-lg mx-auto" /></td>
                </tr>
              ))
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={6} className="px-4 py-16 text-center text-gray-400 text-sm">
                  {search ? `"${search}" için ürün bulunamadı` : 'Henüz ürün yok'}
                </td>
              </tr>
            ) : (
              products.map(p => (
                <tr key={p.id} className="border-b border-gray-50 hover:bg-gray-50/60 transition-colors">
                  {/* Image */}
                  <td className="px-4 py-3">
                    <div className="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center overflow-hidden flex-shrink-0">
                      {p.imageUrl
                        ? <img src={p.imageUrl} alt={p.name} className="h-full object-contain" />
                        : <ProductPlaceholder category={p.category} size={40} />
                      }
                    </div>
                  </td>

                  {/* Name + brand */}
                  <td className="px-4 py-3 max-w-xs">
                    <div className="font-medium text-gray-800 truncate">{p.name}</div>
                    {p.brand && <div className="text-xs text-gray-400">{p.brand}</div>}
                  </td>

                  {/* Category */}
                  <td className="px-4 py-3">
                    <span className="text-xs font-medium text-gray-600">{p.category}</span>
                  </td>

                  {/* Price */}
                  <td className="px-4 py-3 text-right">
                    <div className="font-semibold" style={{ color: '#e91e8c' }}>{formatPrice(p.price)}</div>
                    {p.originalPrice && p.originalPrice > p.price && (
                      <div className="text-[11px] text-gray-400 line-through">{formatPrice(p.originalPrice)}</div>
                    )}
                  </td>

                  {/* Stock */}
                  <td className="px-4 py-3 text-right">
                    <span className={`text-xs font-semibold ${p.stockQuantity > 10 ? 'text-green-600' : p.stockQuantity > 0 ? 'text-amber-600' : 'text-red-600'}`}>
                      {p.stockQuantity ?? '—'}
                    </span>
                  </td>

                  {/* Actions */}
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-center gap-2">
                      <button
                        onClick={() => openEdit(p)}
                        className="px-2.5 py-1 rounded-lg text-xs font-semibold text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors"
                      >
                        Düzenle
                      </button>
                      <button
                        onClick={() => setDeleteTarget(p)}
                        className="px-2.5 py-1 rounded-lg text-xs font-semibold text-red-600 bg-red-50 hover:bg-red-100 transition-colors"
                      >
                        Sil
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>

        {/* Pagination */}
        {!loading && totalPages > 1 && (
          <div className="px-4 py-3 border-t border-gray-100 flex justify-center gap-1.5">
            <PageBtn disabled={page === 0} onClick={() => setPage(p => p - 1)}>‹</PageBtn>
            {Array.from({ length: totalPages }, (_, i) => (
              <PageBtn key={i} active={i === page} onClick={() => setPage(i)}>{i + 1}</PageBtn>
            ))}
            <PageBtn disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>›</PageBtn>
          </div>
        )}
      </div>

      {/* ── Product modal ──────────────────────────────────────────────────── */}
      {modalOpen && (
        <ProductModal
          initial={editTarget}
          onSave={handleSave}
          onClose={closeModal}
          saving={saving}
          error={error}
        />
      )}

      {/* ── Delete confirm ─────────────────────────────────────────────────── */}
      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="bg-white rounded-2xl shadow-xl p-6 max-w-sm w-full space-y-4">
            <div className="text-lg font-bold text-gray-800">Ürünü Sil</div>
            <p className="text-sm text-gray-600">
              <span className="font-semibold">"{deleteTarget.name}"</span> ürününü silmek istediğinize emin misiniz?
              Bu işlem geri alınamaz.
            </p>
            <div className="flex gap-3 justify-end">
              <button
                onClick={() => setDeleteTarget(null)}
                disabled={deleting}
                className="px-4 py-2 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Vazgeç
              </button>
              <button
                onClick={handleDelete}
                disabled={deleting}
                className="px-4 py-2 rounded-xl text-sm font-bold text-white bg-red-600 hover:bg-red-700 transition-colors disabled:opacity-60"
              >
                {deleting ? 'Siliniyor…' : 'Evet, Sil'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function ProductModal({ initial, onSave, onClose, saving, error }) {
  const isEdit = !!initial;
  const [form, setForm] = useState(() => initial
    ? { ...EMPTY_FORM, ...initial, price: String(initial.price), originalPrice: String(initial.originalPrice || ''), stockQuantity: String(initial.stockQuantity || '') }
    : { ...EMPTY_FORM }
  );
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.name.trim())                              e.name          = 'Ürün adı gerekli';
    if (!form.category)                                 e.category      = 'Kategori seçin';
    if (!form.price || isNaN(Number(form.price)))       e.price         = 'Geçerli bir fiyat girin';
    if (!form.stockQuantity || isNaN(Number(form.stockQuantity))) e.stockQuantity = 'Geçerli stok miktarı girin';
    return e;
  };

  const handleChange = e => {
    const { name, value, type, checked } = e.target;
    setForm(p => ({ ...p, [name]: type === 'checkbox' ? checked : value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
  };

  const handleSubmit = e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }
    onSave(form);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4 overflow-y-auto">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg my-4">

        {/* Modal header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <h2 className="font-bold text-gray-800">{isEdit ? 'Ürünü Düzenle' : 'Yeni Ürün Ekle'}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 text-2xl leading-none">×</button>
        </div>

        {/* API error inside modal */}
        {error && (
          <div className="mx-6 mt-4 bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-2.5 text-sm">
            {error}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="px-6 py-4 space-y-4 max-h-[70vh] overflow-y-auto">
          <MField label="Ürün Adı *" name="name" value={form.name} onChange={handleChange} placeholder="Ürün adını girin" error={errors.name} />

          <div className="grid grid-cols-2 gap-3">
            <MField label="Marka" name="brand" value={form.brand} onChange={handleChange} placeholder="Apple, Samsung…" />
            <div>
              <label className="block text-xs font-semibold text-gray-600 mb-1">Kategori *</label>
              <select
                name="category"
                value={form.category}
                onChange={handleChange}
                className={`w-full border rounded-lg px-3 py-2 text-sm outline-none focus:border-brand ${errors.category ? 'border-red-400' : 'border-gray-200'}`}
              >
                <option value="">Seçin</option>
                {CATEGORY_META.map(c => <option key={c.id} value={c.id}>{c.id}</option>)}
              </select>
              {errors.category && <p className="text-xs text-red-500 mt-1">{errors.category}</p>}
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <MField label="Fiyat (TL) *" name="price" value={form.price} onChange={handleChange} placeholder="0.00" type="number" min="0" step="0.01" error={errors.price} />
            <MField label="Orijinal Fiyat (TL)" name="originalPrice" value={form.originalPrice} onChange={handleChange} placeholder="İndirim öncesi" type="number" min="0" step="0.01" />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <MField label="Stok Miktarı *" name="stockQuantity" value={form.stockQuantity} onChange={handleChange} placeholder="0" type="number" min="0" error={errors.stockQuantity} />
            <MField label="Rozet" name="badge" value={form.badge} onChange={handleChange} placeholder="FIRSATLAR, YENİ…" />
          </div>

          <MField label="Görsel URL" name="imageUrl" value={form.imageUrl} onChange={handleChange} placeholder="https://…" />

          <div>
            <label className="block text-xs font-semibold text-gray-600 mb-1">Açıklama</label>
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows={3}
              placeholder="Ürün açıklaması…"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm outline-none focus:border-brand resize-none"
            />
          </div>

          <label className="flex items-center gap-2 cursor-pointer text-sm text-gray-700">
            <input type="checkbox" name="freeShipping" checked={form.freeShipping} onChange={handleChange} className="accent-brand" />
            Ücretsiz Kargo
          </label>
        </form>

        {/* Modal footer */}
        <div className="flex gap-3 justify-end px-6 py-4 border-t border-gray-100">
          <button
            type="button"
            onClick={onClose}
            disabled={saving}
            className="px-4 py-2.5 rounded-xl text-sm font-semibold border border-gray-200 text-gray-600 hover:bg-gray-50 transition-colors"
          >
            İptal
          </button>
          <button
            onClick={handleSubmit}
            disabled={saving}
            className="px-5 py-2.5 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity disabled:opacity-60"
            style={{ background: '#e91e8c' }}
          >
            {saving ? 'Kaydediliyor…' : isEdit ? 'Güncelle' : 'Ekle'}
          </button>
        </div>
      </div>
    </div>
  );
}

function MField({ label, name, value, onChange, placeholder, type = 'text', error, min, step }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-gray-600 mb-1">{label}</label>
      <input
        name={name} type={type} value={value} onChange={onChange}
        placeholder={placeholder} min={min} step={step}
        className={`w-full border rounded-lg px-3 py-2 text-sm outline-none focus:border-brand ${error ? 'border-red-400 bg-red-50' : 'border-gray-200'}`}
      />
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}

function PageBtn({ children, active, disabled, onClick }) {
  return (
    <button
      onClick={onClick} disabled={disabled}
      className={`w-7 h-7 rounded-lg text-xs font-semibold transition-colors ${active ? 'text-white' : disabled ? 'text-gray-300 cursor-not-allowed bg-white' : 'text-gray-600 bg-white hover:bg-gray-100'}`}
      style={active ? { background: '#e91e8c' } : {}}
    >
      {children}
    </button>
  );
}
