export default function Spinner({ size = 36, borderWidth = 3 }) {
  return (
    <div
      className="spinner"
      style={{ width: size, height: size, borderWidth }}
    />
  );
}
