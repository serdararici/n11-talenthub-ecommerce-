import axios from 'axios';
import { MOCK_PRODUCTS, MOCK_CATEGORIES } from './mockData';

const AUTH_URL   = import.meta.env.VITE_AUTH_URL   || 'http://localhost:8081';
const PRODUCT_URL= import.meta.env.VITE_PRODUCT_URL|| 'http://localhost:8082';
const CART_URL   = import.meta.env.VITE_CART_URL   || 'http://localhost:8083';
const ORDER_URL  = import.meta.env.VITE_ORDER_URL  || 'http://localhost:8084';

function createInstance(baseURL) {
  const instance = axios.create({ baseURL, timeout: 8000 });
  instance.interceptors.request.use(config => {
    const token = localStorage.getItem('n11_token');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
  });
  instance.interceptors.response.use(
    res => res.data.data,
    err => {
      if (err.response) {
        const msg = err.response.data?.message || 'Bir hata oluştu';
        return Promise.reject(new Error(msg));
      }
      return Promise.reject(new Error('__NETWORK__'));
    }
  );
  return instance;
}

const authApi    = createInstance(AUTH_URL);
const productApi = createInstance(PRODUCT_URL);
const cartApi    = createInstance(CART_URL);
const orderApi   = createInstance(ORDER_URL);

const isNetwork = e => e?.message === '__NETWORK__';

// ── Auth ──────────────────────────────────────────────────────────────────
export const authLogin = data => authApi.post('/api/auth/login', data);
export const authRegister = data => authApi.post('/api/auth/register', data);
export const authRefresh = token => authApi.post('/api/auth/refresh', { refreshToken: token });

// ── Products ─────────────────────────────────────────────────────────────
export async function fetchProducts(params = {}) {
  try {
    return await productApi.get('/api/products', { params });
  } catch (e) {
    if (!isNetwork(e)) throw e;
    let items = [...MOCK_PRODUCTS];
    if (params.category) items = items.filter(p => p.category === params.category);
    if (params.search)   items = items.filter(p => p.name.toLowerCase().includes(params.search.toLowerCase()) || (p.brand||'').toLowerCase().includes(params.search.toLowerCase()));
    const page = params.page || 0;
    const size = params.size || 10;
    const content = items.slice(page * size, (page + 1) * size);
    return { content, pageNumber: page, pageSize: size, totalElements: items.length, totalPages: Math.ceil(items.length / size), last: (page + 1) * size >= items.length };
  }
}

export async function fetchProduct(id) {
  try {
    return await productApi.get(`/api/products/${id}`);
  } catch (e) {
    if (!isNetwork(e)) throw e;
    const p = MOCK_PRODUCTS.find(x => x.id === Number(id));
    if (!p) throw new Error('Ürün bulunamadı');
    return p;
  }
}

export async function fetchCategories() {
  try {
    return await productApi.get('/api/products/categories');
  } catch (e) {
    if (!isNetwork(e)) throw e;
    return MOCK_CATEGORIES;
  }
}

export const adminCreateProduct = data => productApi.post('/api/products', data);
export const adminUpdateProduct = (id, data) => productApi.put(`/api/products/${id}`, data);
export const adminDeleteProduct = id => productApi.delete(`/api/products/${id}`);

// ── Cart ─────────────────────────────────────────────────────────────────
export const cartGet    = ()                        => cartApi.get('/api/cart');
export const cartAdd    = data                      => cartApi.post('/api/cart/items', data);
export const cartUpdate = (productId, data)         => cartApi.put(`/api/cart/items/${productId}`, data);
export const cartRemove = productId                 => cartApi.delete(`/api/cart/items/${productId}`);
export const cartClear  = ()                        => cartApi.delete('/api/cart');

// ── Orders ───────────────────────────────────────────────────────────────
export const orderCreate      = data => orderApi.post('/api/orders', data);
export const orderList        = ()   => orderApi.get('/api/orders');
export const orderGet         = id   => orderApi.get(`/api/orders/${id}`);
export const orderUpdateStatus= (id, status) => orderApi.put(`/api/orders/${id}/status`, { status });
