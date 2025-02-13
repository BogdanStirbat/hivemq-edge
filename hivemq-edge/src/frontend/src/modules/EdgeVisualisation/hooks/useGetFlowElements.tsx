import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { Edge, Node, useEdgesState, useNodesState } from 'reactflow'
import { useTheme } from '@chakra-ui/react'

import { Adapter, Bridge } from '@/api/__generated__'
import { useListProtocolAdapters } from '@/api/hooks/useProtocolAdapters/useListProtocolAdapters.tsx'
import { useListBridges } from '@/api/hooks/useGetBridges/useListBridges.tsx'
import { useGetListeners } from '@/api/hooks/useGateway/useGetListeners.tsx'

import { createEdgeNode, createBridgeNode, createAdapterNode, createListenerNode } from '../utils/nodes-utils.ts'
import { useEdgeFlowContext } from '../hooks/useEdgeFlowContext.tsx'

const useGetFlowElements = () => {
  const { t } = useTranslation()
  const theme = useTheme()
  const { options } = useEdgeFlowContext()
  const { data: bridges } = useListBridges()
  const { data: adapters } = useListProtocolAdapters()
  const { data: listenerList } = useGetListeners()
  const [nodes, setNodes, onNodesChange] = useNodesState<Bridge | Adapter>([])
  const [edges, setEdges, onEdgesChange] = useEdgesState([])

  const { items: listeners } = listenerList || {}

  useEffect(() => {
    if (!bridges) return
    if (!adapters) return

    const nodes: Node[] = []
    const edges: Edge[] = []

    const nodeEdge = createEdgeNode(t('branding.appName'))

    listeners?.forEach((listener, nb) => {
      const { nodeListener, edgeConnector } = createListenerNode(listener, nb)

      if (options.showGateway) {
        nodes.push(nodeListener)
        edges.push(edgeConnector)
      }
    })

    bridges.forEach((bridge, incBridgeNb) => {
      const { nodeBridge, edgeConnector, nodeHost, hostConnector } = createBridgeNode(
        bridge,
        incBridgeNb,
        bridges.length,
        theme
      )
      nodes.push(nodeBridge)
      edges.push(edgeConnector)
      if (options.showHosts) {
        nodes.push(nodeHost)
        edges.push(hostConnector)
      }
    })

    adapters.forEach((adapter, incAdapterNb) => {
      const { nodeAdapter, edgeConnector } = createAdapterNode(adapter, incAdapterNb, adapters.length, theme)
      nodes.push(nodeAdapter)
      edges.push(edgeConnector)
    })

    setNodes([nodeEdge, ...nodes])
    setEdges([...edges])
  }, [bridges, adapters, listeners, setNodes, setEdges, t, options, theme])

  return { nodes, edges, onNodesChange, onEdgesChange }
}

export default useGetFlowElements
