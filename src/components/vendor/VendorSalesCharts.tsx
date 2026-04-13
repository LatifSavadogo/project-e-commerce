import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { ADMIN_CHART_PALETTE, chartAxisStyle, chartGridColor } from '../admin/chartTheme'
import type { VendorSalesDashboard } from '../../services/paymentApi'

const PAY_LABELS: Record<string, string> = {
  ORANGE_MONEY: 'Orange Money',
  MOOV_MONEY: 'Moov Money',
  VIREMENT: 'Virement',
  ESPECES: 'Espèces',
}

function formatFcfa(n: number) {
  return `${n.toLocaleString('fr-FR')} FCFA`
}

function compactAxisFcfa(v: number) {
  if (v >= 1_000_000) return `${(v / 1_000_000).toFixed(1)}M`
  if (v >= 1000) return `${(v / 1000).toFixed(0)}k`
  return String(v)
}

const tooltipBox = {
  backgroundColor: 'var(--surface-elevated)',
  border: '1px solid var(--border)',
  borderRadius: 8,
  fontSize: 13,
  color: 'var(--text)',
}

type Props = {
  data: VendorSalesDashboard
}

export default function VendorSalesCharts({ data }: Props) {
  const lineData = data.revenueByDay.map((p) => ({
    ...p,
    dayLabel: new Date(p.date + 'T12:00:00').toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
    }),
  }))

  const methodChart = data.byPaymentMethod.map((m) => ({
    name: PAY_LABELS[m.method] || m.method,
    transactions: m.transactionCount,
    revenue: m.revenue,
  }))

  const topArticlesChart = data.topArticles.map((a) => ({
    name:
      (a.libelle && a.libelle.length > 28 ? `${a.libelle.slice(0, 26)}…` : a.libelle) ||
      `Article #${a.idArticle}`,
    revenue: a.revenue,
    quantity: a.quantitySold,
  }))

  const hasSales = data.transactionCount > 0

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 420px), 1fr))',
        gap: 20,
      }}
    >
      <div
        style={{
          background: 'var(--input-bg)',
          border: '1px solid var(--border)',
          borderRadius: 10,
          padding: 16,
          minHeight: 320,
        }}
      >
        <h3 style={{ margin: '0 0 4px', fontSize: '1.05em' }}>Chiffre d’affaires (90 jours)</h3>
        <p style={{ margin: '0 0 12px', fontSize: '0.82em', color: 'var(--muted)' }}>
          Courbe quotidienne des ventes enregistrées
        </p>
        <div style={{ width: '100%', height: 260 }}>
          {!hasSales ? (
            <p className="meta" style={{ padding: 48, textAlign: 'center' }}>
              Aucune donnée sur la période
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={lineData} margin={{ top: 8, right: 12, left: 4, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} vertical={false} />
                <XAxis
                  dataKey="dayLabel"
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  interval="preserveStartEnd"
                  minTickGap={28}
                />
                <YAxis
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  tickFormatter={compactAxisFcfa}
                />
                <Tooltip
                  contentStyle={tooltipBox}
                  labelFormatter={(_, payload) => {
                    const row = payload?.[0]?.payload as { date?: string } | undefined
                    return row?.date
                      ? new Date(row.date + 'T12:00:00').toLocaleDateString('fr-FR', {
                          weekday: 'long',
                          day: 'numeric',
                          month: 'long',
                        })
                      : ''
                  }}
                  formatter={(value) => [formatFcfa(Number(value ?? 0)), 'CA']}
                />
                <Line
                  type="monotone"
                  dataKey="revenue"
                  name="CA"
                  stroke="#2a9d8f"
                  strokeWidth={2}
                  dot={false}
                  activeDot={{ r: 5, fill: '#2a9d8f' }}
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div
        style={{
          background: 'var(--input-bg)',
          border: '1px solid var(--border)',
          borderRadius: 10,
          padding: 16,
          minHeight: 320,
        }}
      >
        <h3 style={{ margin: '0 0 4px', fontSize: '1.05em' }}>Commandes par jour (90 jours)</h3>
        <p style={{ margin: '0 0 12px', fontSize: '0.82em', color: 'var(--muted)' }}>
          Volume de transactions
        </p>
        <div style={{ width: '100%', height: 260 }}>
          {!hasSales ? (
            <p className="meta" style={{ padding: 48, textAlign: 'center' }}>
              Aucune donnée sur la période
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={lineData} margin={{ top: 8, right: 12, left: 4, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} vertical={false} />
                <XAxis
                  dataKey="dayLabel"
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  interval="preserveStartEnd"
                  minTickGap={28}
                />
                <YAxis
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={tooltipBox}
                  formatter={(value) => [String(value ?? 0), 'Commandes']}
                />
                <Bar dataKey="orderCount" fill="#264653" radius={[4, 4, 0, 0]} maxBarSize={32} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div
        style={{
          background: 'var(--input-bg)',
          border: '1px solid var(--border)',
          borderRadius: 10,
          padding: 16,
          minHeight: 300,
        }}
      >
        <h3 style={{ margin: '0 0 4px', fontSize: '1.05em' }}>CA par moyen de paiement</h3>
        <p style={{ margin: '0 0 12px', fontSize: '0.82em', color: 'var(--muted)' }}>
          Performance par canal
        </p>
        <div style={{ width: '100%', height: 240 }}>
          {methodChart.length === 0 ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Aucune donnée
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                layout="vertical"
                data={methodChart}
                margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} horizontal={false} />
                <XAxis
                  type="number"
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  tickFormatter={compactAxisFcfa}
                />
                <YAxis
                  type="category"
                  dataKey="name"
                  width={110}
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                />
                <Tooltip
                  contentStyle={tooltipBox}
                  formatter={(value, name) =>
                    name === 'revenue'
                      ? [formatFcfa(Number(value ?? 0)), 'CA']
                      : [String(value ?? 0), 'Transactions']
                  }
                />
                <Bar dataKey="revenue" radius={[0, 6, 6, 0]} maxBarSize={26}>
                  {methodChart.map((_, i) => (
                    <Cell key={i} fill={ADMIN_CHART_PALETTE[i % ADMIN_CHART_PALETTE.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div
        style={{
          background: 'var(--input-bg)',
          border: '1px solid var(--border)',
          borderRadius: 10,
          padding: 16,
          minHeight: 300,
        }}
      >
        <h3 style={{ margin: '0 0 4px', fontSize: '1.05em' }}>Top articles</h3>
        <p style={{ margin: '0 0 12px', fontSize: '0.82em', color: 'var(--muted)' }}>
          Articles les plus rentables (CA cumulé)
        </p>
        <div style={{ width: '100%', height: 240 }}>
          {topArticlesChart.length === 0 ? (
            <p className="meta" style={{ padding: 40, textAlign: 'center' }}>
              Aucune vente à afficher
            </p>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                layout="vertical"
                data={topArticlesChart}
                margin={{ top: 8, right: 24, left: 8, bottom: 8 }}
              >
                <CartesianGrid strokeDasharray="3 3" stroke={chartGridColor} horizontal={false} />
                <XAxis
                  type="number"
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                  tickFormatter={compactAxisFcfa}
                />
                <YAxis
                  type="category"
                  dataKey="name"
                  width={130}
                  tick={chartAxisStyle}
                  tickLine={false}
                  axisLine={{ stroke: chartGridColor }}
                />
                <Tooltip
                  contentStyle={tooltipBox}
                  formatter={(value, name) =>
                    name === 'revenue'
                      ? [formatFcfa(Number(value ?? 0)), 'CA']
                      : [String(value ?? 0), 'Quantité']
                  }
                />
                <Bar dataKey="revenue" radius={[0, 6, 6, 0]} maxBarSize={22}>
                  {topArticlesChart.map((_, i) => (
                    <Cell key={i} fill={ADMIN_CHART_PALETTE[(i + 2) % ADMIN_CHART_PALETTE.length]} />
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
