import { useQuery } from '@tanstack/react-query'
import { useHttpClient } from '@/api/hooks/useHttpClient/useHttpClient.ts'
import config from '@/config'
import { QUERY_KEYS } from '@/api/utils.ts'

export const useGetAdaptersStatus = () => {
  const appClient = useHttpClient()

  return useQuery(
    [QUERY_KEYS.PROTOCOLS, 'connection.status'],
    async () => {
      const item = await appClient.protocolAdapters.status1()
      return item
    },
    {
      retry: 0,
      refetchInterval: () => {
        // return data ? 4 * 1000 : Math.max(Math.min(query.state.errorUpdateCount, 5 * 60), 4) * 1000
        return config.httpClient.pollingRefetchInterval
      },
    }
  )
}
