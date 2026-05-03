import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { authLogin } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

export default function LoginPage() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const { login } = useAuth();
  const { toast } = useToast();

  const from = location.state?.from?.pathname || '/';

  const [form,       setForm]       = useState({ email: '', password: '' });
  const [errors,     setErrors]     = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [apiError,   setApiError]   = useState('');

  const validate = () => {
    const e = {};
    if (!form.email.trim())                        e.email    = 'E-posta gerekli';
    else if (!/\S+@\S+\.\S+/.test(form.email))    e.email    = 'Geçerli bir e-posta girin';
    if (!form.password)                            e.password = 'Şifre gerekli';
    return e;
  };

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(p => ({ ...p, [name]: value }));
    if (errors[name]) setErrors(p => ({ ...p, [name]: '' }));
    setApiError('');
  };

  const handleSubmit = async e => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setSubmitting(true);
    setApiError('');
    try {
      const data = await authLogin({ email: form.email, password: form.password });
      login(data.user || data, data.accessToken, data.refreshToken);
      toast(`Hoş geldiniz, ${data.user?.firstName || data.firstName || ''}!`, 'success');
      navigate(from, { replace: true });
    } catch (err) {
      setApiError(err.message === '__NETWORK__'
        ? 'Sunucuya bağlanılamıyor. Lütfen daha sonra tekrar deneyin.'
        : err.message || 'Giriş başarısız');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-[70vh] flex items-center justify-center">
      <div className="w-full max-w-md">

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-sm p-8 space-y-6">

          {/* Logo + title */}
          <div className="text-center space-y-2">
            <div
              className="w-14 h-14 rounded-full mx-auto flex items-center justify-center"
              style={{ background: 'linear-gradient(135deg, #e91e8c, #c4177a)' }}
            >
              <svg width="28" height="28" viewBox="0 0 28 28">
                <circle cx="10" cy="11" r="5" fill="#fff" />
                <circle cx="18" cy="17" r="5" fill="#fff" opacity=".65" />
              </svg>
            </div>
            <h1 className="text-xl font-extrabold text-gray-800">Giriş Yap</h1>
            <p className="text-sm text-gray-400">N11 TalentHub hesabınıza giriş yapın</p>
          </div>

          {/* API error */}
          {apiError && (
            <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-2.5 text-sm">
              {apiError}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <Field
              label="E-posta"
              name="email"
              type="email"
              value={form.email}
              onChange={handleChange}
              placeholder="ornek@email.com"
              error={errors.email}
              autoComplete="email"
            />
            <Field
              label="Şifre"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              placeholder="••••••••"
              error={errors.password}
              autoComplete="current-password"
            />

            <button
              type="submit"
              disabled={submitting}
              className="w-full py-3 rounded-xl text-sm font-bold text-white hover:opacity-90 transition-opacity disabled:opacity-60 disabled:cursor-not-allowed"
              style={{ background: 'linear-gradient(135deg, #e91e8c, #c4177a)' }}
            >
              {submitting ? (
                <span className="flex items-center justify-center gap-2">
                  <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  Giriş yapılıyor…
                </span>
              ) : 'Giriş Yap'}
            </button>
          </form>

          {/* Footer links */}
          <div className="text-center text-sm text-gray-400">
            Hesabınız yok mu?{' '}
            <Link to="/register" className="font-semibold hover:underline" style={{ color: '#e91e8c' }}>
              Üye Ol
            </Link>
          </div>
        </div>

        {/* Back to shopping */}
        <div className="text-center mt-4">
          <Link to="/" className="text-sm text-gray-400 hover:text-gray-600 transition-colors">
            ← Alışverişe devam et
          </Link>
        </div>

      </div>
    </div>
  );
}

function Field({ label, name, type, value, onChange, placeholder, error, autoComplete }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-gray-600 mb-1">{label}</label>
      <input
        name={name}
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        autoComplete={autoComplete}
        className={`w-full border rounded-xl px-4 py-2.5 text-sm outline-none transition-colors ${
          error
            ? 'border-red-400 bg-red-50 focus:border-red-500'
            : 'border-gray-200 focus:border-brand'
        }`}
      />
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}
