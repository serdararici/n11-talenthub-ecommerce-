import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authRegister, authLogin } from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

export default function RegisterPage() {
  const navigate  = useNavigate();
  const { login } = useAuth();
  const { toast } = useToast();

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', confirmPassword: '',
  });
  const [errors,     setErrors]     = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [apiError,   setApiError]   = useState('');

  const validate = () => {
    const e = {};
    if (!form.firstName.trim())                        e.firstName       = 'Ad gerekli';
    if (!form.lastName.trim())                         e.lastName        = 'Soyad gerekli';
    if (!form.email.trim())                            e.email           = 'E-posta gerekli';
    else if (!/\S+@\S+\.\S+/.test(form.email))        e.email           = 'Geçerli bir e-posta girin';
    if (!form.password)                                e.password        = 'Şifre gerekli';
    else if (form.password.length < 8)                 e.password        = 'Şifre en az 8 karakter olmalı';
    if (!form.confirmPassword)                         e.confirmPassword = 'Şifreyi tekrar girin';
    else if (form.confirmPassword !== form.password)   e.confirmPassword = 'Şifreler eşleşmiyor';
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
      await authRegister({
        firstName: form.firstName.trim(),
        lastName:  form.lastName.trim(),
        email:     form.email.trim(),
        password:  form.password,
      });
      // auto-login after successful registration
      const data = await authLogin({ email: form.email.trim(), password: form.password });
      login(data.user || data, data.accessToken, data.refreshToken);
      toast(`Hoş geldiniz, ${form.firstName}! Hesabınız oluşturuldu.`, 'success');
      navigate('/');
    } catch (err) {
      setApiError(err.message === '__NETWORK__'
        ? 'Sunucuya bağlanılamıyor. Lütfen daha sonra tekrar deneyin.'
        : err.message || 'Kayıt oluşturulamadı');
    } finally {
      setSubmitting(false);
    }
  };

  const strength = passwordStrength(form.password);

  return (
    <div className="min-h-[70vh] flex items-center justify-center py-8">
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
            <h1 className="text-xl font-extrabold text-gray-800">Üye Ol</h1>
            <p className="text-sm text-gray-400">N11 TalentHub'a katılın, avantajlı alışveriş yapın</p>
          </div>

          {/* API error */}
          {apiError && (
            <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-2.5 text-sm">
              {apiError}
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4" noValidate>
            <div className="grid grid-cols-2 gap-3">
              <Field
                label="Ad"
                name="firstName"
                value={form.firstName}
                onChange={handleChange}
                placeholder="Adınız"
                error={errors.firstName}
                autoComplete="given-name"
              />
              <Field
                label="Soyad"
                name="lastName"
                value={form.lastName}
                onChange={handleChange}
                placeholder="Soyadınız"
                error={errors.lastName}
                autoComplete="family-name"
              />
            </div>

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

            <div>
              <Field
                label="Şifre"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="En az 6 karakter"
                error={errors.password}
                autoComplete="new-password"
              />
              {form.password && (
                <div className="mt-1.5 space-y-1">
                  <div className="flex gap-1">
                    {[0, 1, 2].map(i => (
                      <div
                        key={i}
                        className="h-1 flex-1 rounded-full transition-colors"
                        style={{ background: i < strength.score ? strength.color : '#e5e7eb' }}
                      />
                    ))}
                  </div>
                  <div className="text-[11px]" style={{ color: strength.color }}>{strength.label}</div>
                </div>
              )}
            </div>

            <Field
              label="Şifre Tekrar"
              name="confirmPassword"
              type="password"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder="Şifrenizi tekrar girin"
              error={errors.confirmPassword}
              autoComplete="new-password"
              suffix={
                form.confirmPassword && form.confirmPassword === form.password
                  ? <span className="text-green-500 text-sm">✓</span>
                  : null
              }
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
                  Hesap oluşturuluyor…
                </span>
              ) : 'Üye Ol'}
            </button>
          </form>

          {/* Footer links */}
          <div className="text-center text-sm text-gray-400">
            Zaten hesabınız var mı?{' '}
            <Link to="/login" className="font-semibold hover:underline" style={{ color: '#e91e8c' }}>
              Giriş Yap
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

function passwordStrength(pw) {
  if (!pw || pw.length < 6) return { score: 1, color: '#dc2626', label: 'Zayıf' };
  let score = 0;
  if (pw.length >= 8)          score++;
  if (/[A-Z]/.test(pw))        score++;
  if (/[0-9]/.test(pw))        score++;
  if (/[^A-Za-z0-9]/.test(pw)) score++;
  if (score <= 1) return { score: 1, color: '#dc2626', label: 'Zayıf' };
  if (score <= 2) return { score: 2, color: '#d97706', label: 'Orta'  };
  return              { score: 3, color: '#16a34a', label: 'Güçlü' };
}

function Field({ label, name, type = 'text', value, onChange, placeholder, error, autoComplete, suffix }) {
  return (
    <div>
      <label className="block text-xs font-semibold text-gray-600 mb-1">{label}</label>
      <div className="relative">
        <input
          name={name}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          autoComplete={autoComplete}
          className={`w-full border rounded-xl px-4 py-2.5 text-sm outline-none transition-colors ${
            suffix ? 'pr-8' : ''
          } ${
            error
              ? 'border-red-400 bg-red-50 focus:border-red-500'
              : 'border-gray-200 focus:border-brand'
          }`}
        />
        {suffix && (
          <div className="absolute right-3 top-1/2 -translate-y-1/2">{suffix}</div>
        )}
      </div>
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
}
