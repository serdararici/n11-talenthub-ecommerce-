const COLORS = {
  Bilgisayar:    ['#6366f1','#818cf8'],
  Tablet:        ['#0891b2','#22d3ee'],
  Elektronik:    ['#2563eb','#60a5fa'],
  Telefon:       ['#dc2626','#f87171'],
  Moda:          ['#e91e8c','#f472b6'],
  'Ev & Yaşam':  ['#16a34a','#4ade80'],
  Spor:          ['#d97706','#fbbf24'],
  'Kitap & Müzik':['#059669','#34d399'],
};

export default function ProductPlaceholder({ category, size = 180 }) {
  const [c1, c2] = COLORS[category] || ['#6b7280','#9ca3af'];
  const gId = `g${c1.replace('#','')}`;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} xmlns="http://www.w3.org/2000/svg" style={{ borderRadius: 8 }}>
      <defs>
        <linearGradient id={gId} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%"   stopColor={c1} stopOpacity="0.15" />
          <stop offset="100%" stopColor={c2} stopOpacity="0.25" />
        </linearGradient>
      </defs>
      <rect width={size} height={size} fill={`url(#${gId})`} />
      {Array.from({ length: 8 }, (_, i) => (
        <line key={i} x1={i * size / 4} y1="0" x2={i * size / 4 + size} y2={size} stroke={c1} strokeWidth="1" strokeOpacity="0.12" />
      ))}
      <text x={size/2} y={size/2 - 8} textAnchor="middle" fill={c1} fontSize="11" fontFamily="monospace" fontWeight="600" opacity="0.7">[{category || 'Ürün'}]</text>
      <text x={size/2} y={size/2 + 10} textAnchor="middle" fill={c1} fontSize="9"  fontFamily="monospace" opacity="0.5">product image</text>
    </svg>
  );
}
