import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { ADMIN_CHART_PALETTE, chartAxisStyle, chartGridColor } from './chartTheme'

type TooltipPayload = { name?: string; value?: number; payload?: { name?: string } }

function DarkTooltip({
  active,
  payload,
  label,
}: {
  active?: boolean
  payload?: TooltipPayload[]
  label?: string
}) {
  if (!active || !payload?.length) return null
  const p = payload[0]
  const name = p.payload?.name ?? p.name ?? label
  const value = p.value
  return (
    <div
      style={{
        background: 'var(--surface-elevated)',
        border: '1px solid var(--border)',
        borderRadius: 8,
        padding: '10px 14px',
        fontSize: 13,
        color: 'var(--text)',
        boxShadow: '0 8px 24px rgba(0,0,0,0.4)',
      }}
    >
      <div style={{ color: 'var(--muted)', fontSize: 11, marginBottom: 4 }}>{name}</div>
      <strong>{value != null ? value.toLocaleString('fr-FR') : '—'}</strong>
    </div>
  )
}

type Props = {
  overview: { name: string; value: number }[]
  roles: { name: string; value: number }[]
  complaints: { name: string; value: number }[]
  payments: { name: string; value: number }[]
}

export default function AdminDashboardCharts({ overview, roles, complaints, payments }: Props) {
  const pieLabel = (props: { name?: string; percent?: number }) => {
    const n = props.name ?? ''
    const pct = typeof props.percent === 'number' ? props.percent : 0
    return `${n} (${(pct * 100).toFixed(0)}%)`
  }

  return (
    <div className="admin-charts-stack">
      <div className="admin-chart-panel">
        <h3 className="admin-chart-title">Vue d’ensemble (volumes)</h3>
        <p className="admin-chart-subtitle">Indicateurs clés issus du serveur</p>
        <div className="admin-chart-area">
          {overview.length === 0 ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Chargement des statistiques…
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={overview} margin={{ top: 8, right: 16, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} vertical={false} />
                <XAxis dataKey="name" tick={chartAxisStyle} tickLine={false} axisLine={{ stroke: chartGridColor }} />
                <YAxis tick={chartAxisStyle} tickLine={false} axisLine={{ stroke: chartGridColor }} allowDecimals={false} />
                <Tooltip content={<DarkTooltip />} cursor={{ fill: 'rgba(42, 157, 143, 0.08)' }} />
                <Bar dataKey="value" fill="#2a9d8f" radius={[6, 6, 0, 0]} maxBarSize={48} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="admin-chart-panel">
        <h3 className="admin-chart-title">Répartition des rôles</h3>
        <p className="admin-chart-subtitle">Utilisateurs inscrits par rôle</p>
        <div className="admin-chart-area admin-chart-area--pie">
          {roles.length === 0 || roles.every((r) => r.value === 0) ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Aucune donnée utilisateur
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={roles}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={52}
                  outerRadius={88}
                  paddingAngle={2}
                  label={pieLabel}
                  labelLine={{ stroke: 'var(--text-faint)' }}
                >
                  {roles.map((_, i) => (
                    <Cell key={i} fill={ADMIN_CHART_PALETTE[i % ADMIN_CHART_PALETTE.length]} stroke="var(--surface)" />
                  ))}
                </Pie>
                <Tooltip content={<DarkTooltip />} />
                <Legend
                  wrapperStyle={{ fontSize: 12, color: 'var(--muted)', paddingTop: 12 }}
                  formatter={(value) => <span style={{ color: 'var(--text-secondary)' }}>{value}</span>}
                />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="admin-chart-panel">
        <h3 className="admin-chart-title">Plaintes</h3>
        <p className="admin-chart-subtitle">Lu vs non lu (liste chargée)</p>
        <div className="admin-chart-area admin-chart-area--pie">
          {complaints.every((c) => c.value === 0) ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Aucune plainte
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={complaints}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={48}
                  outerRadius={82}
                  paddingAngle={3}
                  label={pieLabel}
                >
                  {complaints.map((entry, i) => (
                    <Cell
                      key={entry.name}
                      fill={entry.name === 'Non lues' ? '#da3633' : ADMIN_CHART_PALETTE[(i + 1) % ADMIN_CHART_PALETTE.length]}
                      stroke="var(--surface)"
                    />
                  ))}
                </Pie>
                <Tooltip content={<DarkTooltip />} />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="admin-chart-panel">
        <h3 className="admin-chart-title">Paiements par moyen</h3>
        <p className="admin-chart-subtitle">Volume de transactions enregistrées</p>
        <div className="admin-chart-area">
          {payments.length === 0 ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Aucun paiement ou chargement…
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart layout="vertical" data={payments} margin={{ top: 8, right: 24, left: 8, bottom: 8 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} horizontal={false} />
                <XAxis type="number" tick={chartAxisStyle} tickLine={false} axisLine={{ stroke: chartGridColor }} allowDecimals={false} />
                <YAxis
                  type="category"
                  dataKey="name"
                  width={100}
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                />
                <Tooltip content={<DarkTooltip />} cursor={{ fill: 'rgba(42, 157, 143, 0.08)' }} />
                <Bar dataKey="value" radius={[0, 6, 6, 0]} maxBarSize={22}>
                  {payments.map((_, i) => (
                    <Cell key={i} fill={ADMIN_CHART_PALETTE[i % ADMIN_CHART_PALETTE.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  )
}
