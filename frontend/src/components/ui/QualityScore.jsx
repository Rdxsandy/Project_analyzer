import { PieChart, Pie, Cell } from 'recharts'

const getColor = (score) => {
  if (score >= 80) return '#22c55e'
  if (score >= 60) return '#f59e0b'
  return '#ef4444'
}

const getLabel = (score) => {
  if (score >= 80) return { text: 'Excellent', cls: 'text-green-400' }
  if (score >= 60) return { text: 'Fair', cls: 'text-amber-400' }
  return { text: 'Poor', cls: 'text-red-400' }
}

export default function QualityScore({ score, size = 'md' }) {
  if (score == null) return <span className="text-slate-500 text-sm">N/A</span>

  const sz = size === 'lg' ? { w: 160, cx: 80, inner: 52, outer: 72, text: 'text-3xl' }
                           : { w: 120, cx: 60, inner: 38, outer: 52, text: 'text-2xl' }

  const color = getColor(score)
  const label = getLabel(score)
  const data = [{ value: score }, { value: 100 - score }]

  return (
    <div className="flex flex-col items-center gap-1">
      <div className="relative flex items-center justify-center">
        <PieChart width={sz.w} height={sz.w}>
          <Pie
            data={data}
            cx={sz.cx}
            cy={sz.cx}
            innerRadius={sz.inner}
            outerRadius={sz.outer}
            startAngle={90}
            endAngle={-270}
            dataKey="value"
            strokeWidth={0}
          >
            <Cell fill={color} />
            <Cell fill="#1e293b" />
          </Pie>
        </PieChart>
        <div className="absolute flex flex-col items-center leading-none">
          <span className={`${sz.text} font-bold text-white`}>{score}</span>
          <span className="text-[10px] text-slate-500 mt-0.5">/100</span>
        </div>
      </div>
      <span className={`text-xs font-semibold ${label.cls}`}>{label.text}</span>
    </div>
  )
}
