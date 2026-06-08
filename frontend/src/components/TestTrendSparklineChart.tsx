import { Line, LineChart, ReferenceArea, ResponsiveContainer, YAxis } from "recharts";
import type { TestHistoryPoint } from "../api/patients";

type Props = {
  data: TestHistoryPoint[];
  referenceMin: number | null;
  referenceMax: number | null;
};

// The recharts drawing only. Lives in its own module so it can be lazy-loaded: recharts pulls in
// d3 and would otherwise bloat the main bundle for screens that never show a sparkline.
export default function TestTrendSparklineChart({ data, referenceMin, referenceMax }: Props) {
  const showBand = referenceMin !== null && referenceMax !== null;

  return (
    <ResponsiveContainer width="100%" height={28}>
      <LineChart data={data} margin={{ top: 2, right: 2, bottom: 2, left: 2 }}>
        <YAxis hide domain={["dataMin", "dataMax"]} />
        {showBand && (
          <ReferenceArea
            y1={referenceMin!}
            y2={referenceMax!}
            fill="var(--c-normal)"
            fillOpacity={0.12}
            stroke="none"
          />
        )}
        <Line
          type="monotone"
          dataKey="value"
          stroke="currentColor"
          strokeWidth={1.8}
          dot={false}
          isAnimationActive={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
