import { WithCSSVar } from '@chakra-ui/react'
import { Dict } from '@chakra-ui/utils'
import { XYPosition } from 'reactflow'
import { IdStubs } from '@/modules/EdgeVisualisation/types.ts'

export const MOCK_THEME: Partial<WithCSSVar<Dict>> = {
  colors: {
    status: {
      connected: {
        500: '#38A169',
      },
      disconnected: {
        500: '#718096',
      },
      connecting: {
        500: '#CBD5E0',
      },
      disconnecting: {
        500: '#CBD5E0',
      },
      error: {
        500: '#E53E3E',
      },
    },
  },
}

export const MOCK_LOCAL_STORAGE: Record<string, XYPosition> = {
  [IdStubs.EDGE_NODE]: { x: 1, y: 1 },
  [`${IdStubs.BRIDGE_NODE}#bridge-id-01`]: { x: 2, y: 2 },
  [`${IdStubs.HOST_NODE}#bridge-id-01`]: { x: 3, y: 3 },
  [`${IdStubs.LISTENER_NODE}#tcp-listener-1883`]: { x: 1, y: 1 },
  [`${IdStubs.ADAPTER_NODE}#my-id`]: { x: 1, y: 1 },
}
