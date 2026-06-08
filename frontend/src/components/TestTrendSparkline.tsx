import { lazy, Suspense } from "react";
import { useQuery } from "@tanstack/react-query";
import { Minus, TrendingDown, TrendingUp } from "lucide-react";
import { getTestHistory } from "../api/patients";
import "./TestTrendSparkline.css";

// Lazy so recharts (and its d3 dependencies) only load on the detail screen, not in the main bundle.
const SparklineChart = lazy(() => import("./TestTrendSparklineChart"));

type Props = {
  patientId: string;
  testCode: string;
  referenceMin: number | null;
  referenceMax: number | null;
};

// This wrapper owns the data + trend decision (no recharts import); it renders the chart only when
// there are at least two numeric points to connect.
export function TestTrendSparkline({ patientId, testCode, referenceMin, referenceMax }: Props) {
  const { data, isPending, isError } = useQuery({
    queryKey: ["testHistory", patientId, testCode],
    queryFn: () => getTestHistory(patientId, testCode),
    staleTime: 60_000,
  });

  if (isPending) {
    return <span className="sparkline-placeholder" aria-hidden="true" />;
  }

  if (isError || !data || data.length < 2) {
    // Not enough history to draw a trend; the value/range cells already carry the point-in-time info.
    return <span className="sparkline-empty">—</span>;
  }

  const delta = data[data.length - 1].value - data[0].value;
  const direction = delta > 0 ? "up" : delta < 0 ? "down" : "flat";
  const TrendIcon = direction === "up" ? TrendingUp : direction === "down" ? TrendingDown : Minus;

  return (
    <div className={`sparkline sparkline--${direction}`}>
      <div className="sparkline-chart" aria-hidden="true">
        <Suspense fallback={<span className="sparkline-placeholder" />}>
          <SparklineChart data={data} referenceMin={referenceMin} referenceMax={referenceMax} />
        </Suspense>
      </div>
      <span className="sparkline-trend" title={`${data.length} ölçüm`}>
        <TrendIcon size={14} aria-hidden="true" />
        <span className="sr-only">
          Trend: {direction === "up" ? "yükseliyor" : direction === "down" ? "düşüyor" : "sabit"}
        </span>
      </span>
    </div>
  );
}
