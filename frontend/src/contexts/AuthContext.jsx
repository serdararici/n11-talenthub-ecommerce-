import { createContext, useContext, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('n11_user')); } catch { return null; }
  });
  const [token, setToken] = useState(() => localStorage.getItem('n11_token') || null);

  const login = (userData, accessToken, refreshToken) => {
    setUser(userData);
    setToken(accessToken);
    localStorage.setItem('n11_user', JSON.stringify(userData));
    localStorage.setItem('n11_token', accessToken);
    if (refreshToken) localStorage.setItem('n11_refresh_token', refreshToken);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('n11_user');
    localStorage.removeItem('n11_token');
    localStorage.removeItem('n11_refresh_token');
  };

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
