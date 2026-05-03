import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer className="bg-gray-800 text-gray-300 mt-16">
      <div className="max-w-screen-xl mx-auto px-5 pt-12 pb-6 grid grid-cols-4 gap-8">
        {/* Brand */}
        <div>
          <div className="flex items-center gap-2.5 mb-4">
            <div className="w-9 h-9 rounded-full flex items-center justify-center" style={{ background: '#e91e8c' }}>
              <svg width="22" height="22" viewBox="0 0 22 22"><circle cx="8" cy="9" r="4" fill="#fff" /><circle cx="14" cy="14" r="4" fill="#fff" opacity=".6" /></svg>
            </div>
            <div>
              <div className="font-extrabold text-sm text-white">n11 TalentHub</div>
              <div className="text-[10px] text-gray-400">Alışverişin Yeni Adresi</div>
            </div>
          </div>
          <p className="text-xs text-gray-400 leading-relaxed">N11 TalentHub Bootcamp kapsamında geliştirilmiş Spring Boot + React e-ticaret projesi.</p>
        </div>

        {[
          { title: 'Kategoriler', links: [['Elektronik', '/products?category=Elektronik'], ['Bilgisayar', '/products?category=Bilgisayar'], ['Tablet', '/products?category=Tablet'], ['Moda', '/products?category=Moda']] },
          { title: 'Kurumsal', links: [['Hakkımızda', '#'], ['Kariyer', '#'], ['Basın', '#'], ['Sürdürülebilirlik', '#']] },
          { title: 'Destek', links: [['Yardım Merkezi', '#'], ['İade & Değişim', '#'], ['Kargo Takip', '#'], ['İletişim', '#']] },
        ].map(col => (
          <div key={col.title}>
            <div className="font-bold text-sm text-white mb-3.5">{col.title}</div>
            <div className="flex flex-col gap-2">
              {col.links.map(([label, href]) => (
                <Link key={label} to={href} className="text-xs text-gray-400 hover:text-brand transition-colors">{label}</Link>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="max-w-screen-xl mx-auto px-5 py-4 border-t border-gray-700 flex justify-between items-center text-[11px] text-gray-500">
        <span>© 2026 N11 TalentHub. Tüm hakları saklıdır.</span>
        <span>Spring Boot Microservices • React • TalentHub Bootcamp</span>
      </div>
    </footer>
  );
}
