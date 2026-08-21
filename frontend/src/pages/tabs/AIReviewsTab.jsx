import { useQuery } from '@tanstack/react-query'
import { getScanAIReviews } from '../../api/scans'
import AIReviewCard from '../../components/scans/AIReviewCard'
import LoadingSpinner from '../../components/ui/LoadingSpinner'
import EmptyState from '../../components/ui/EmptyState'

export default function AIReviewsTab({ scanId }) {
  const { data: reviews = [], isLoading, error } = useQuery({
    queryKey: ['ai-reviews', scanId],
    queryFn: () => getScanAIReviews(scanId),
    staleTime: 60_000,
  })

  if (isLoading) return <LoadingSpinner message="Loading AI reviews…" />
  if (error) return <p className="text-red-400 text-sm">{error.message}</p>

  const confirmed = reviews.filter((r) => r.valid)
  const falsePositives = reviews.filter((r) => !r.valid)

  return (
    <div>
      {/* Stats */}
      {reviews.length > 0 && (
        <div className="flex items-center gap-6 mb-6 p-4 card bg-slate-900/50">
          <div>
            <p className="text-xs text-slate-500">Total AI Reviews</p>
            <p className="text-2xl font-bold text-white">{reviews.length}</p>
          </div>
          <div className="h-10 w-px bg-slate-700" />
          <div>
            <p className="text-xs text-slate-500">Confirmed Issues</p>
            <p className="text-2xl font-bold text-green-400">{confirmed.length}</p>
          </div>
          <div className="h-10 w-px bg-slate-700" />
          <div>
            <p className="text-xs text-slate-500">False Positives</p>
            <p className="text-2xl font-bold text-red-400">{falsePositives.length}</p>
          </div>
          <div className="ml-auto text-right">
            <p className="text-xs text-slate-500">Accuracy</p>
            <p className="text-2xl font-bold text-indigo-400">
              {reviews.length ? Math.round((confirmed.length / reviews.length) * 100) : 0}%
            </p>
          </div>
        </div>
      )}

      {reviews.length === 0 ? (
        <EmptyState
          title="No AI reviews yet"
          description="AI reviews are generated automatically after static analysis completes."
        />
      ) : (
        <div className="space-y-3">
          {reviews.map((r) => (
            <AIReviewCard key={r.id} review={r} />
          ))}
        </div>
      )}
    </div>
  )
}
