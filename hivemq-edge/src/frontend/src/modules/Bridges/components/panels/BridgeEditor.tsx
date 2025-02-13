import { FC, ReactNode, useEffect } from 'react'
import { useDisclosure } from '@chakra-ui/react'
import { useNavigate, useParams } from 'react-router-dom'
import { SubmitHandler } from 'react-hook-form'
import { useTranslation } from 'react-i18next'

import { ApiError, Bridge } from '@/api/__generated__'
import { useListBridges } from '@/api/hooks/useGetBridges/useListBridges.tsx'
import { useCreateBridge } from '@/api/hooks/useGetBridges/useCreateBridge.tsx'
import { useUpdateBridge } from '@/api/hooks/useGetBridges/useUpdateBridge.tsx'
import { useDeleteBridge } from '@/api/hooks/useGetBridges/useDeleteBridge.tsx'

import { useEdgeToast } from '@/hooks/useEdgeToast/useEdgeToast.tsx'

import ConfirmationDialog from '@/components/Modal/ConfirmationDialog.tsx'
import BridgeMainDrawer from '@/modules/Bridges/components/panels/BridgeMainDrawer.tsx'
import { bridgeInitialState, useBridgeSetup } from '@/modules/Bridges/hooks/useBridgeConfig.tsx'

interface BridgeEditorProps {
  isNew?: boolean
  children?: ReactNode
}

const BridgeEditor: FC<BridgeEditorProps> = ({ children }) => {
  const { t } = useTranslation()
  const { successToast, errorToast } = useEdgeToast()

  const { isOpen, onOpen, onClose } = useDisclosure()
  const { bridgeId } = useParams()
  const { bridge, setBridge } = useBridgeSetup()
  const { data } = useListBridges()
  const navigate = useNavigate()
  const createBridge = useCreateBridge()
  const updateBridge = useUpdateBridge()
  const deleteBridge = useDeleteBridge()
  const { isOpen: isConfirmDeleteOpen, onOpen: onConfirmDeleteOpen, onClose: onConfirmDeleteClose } = useDisclosure()

  useEffect(() => {
    if (bridgeId) {
      const b = data?.find((e) => e.id === bridgeId)
      if (b) {
        setBridge(b)
      } else {
        // TODO[NVL] handle error for the edge cases of not finding the right datapoint
        setBridge(bridgeInitialState)
      }
    } else {
      setBridge(bridgeInitialState)
    }
    onOpen()
  }, [bridgeId, bridge, data, setBridge, onOpen])

  const handleEditorOnClose = () => {
    onClose()
    navigate('/mqtt-bridges')
  }

  const handleEditorOnSubmit: SubmitHandler<Bridge> = (data) => {
    if (bridgeId) {
      if (bridgeId === data.id) {
        updateBridge
          .mutateAsync({ name: bridgeId, requestBody: data })
          .then(() => {
            successToast({
              title: t('bridge.toast.update.title'),
              description: t('bridge.toast.update.description'),
            })
          })
          .catch((err: ApiError) =>
            errorToast(
              {
                title: t('bridge.toast.update.title'),
                description: t('bridge.toast.update.error'),
              },
              err
            )
          )
      }
    } else {
      createBridge
        .mutateAsync(data)
        .then(() => {
          successToast({
            title: t('bridge.toast.create.title'),
            description: t('bridge.toast.create.description'),
          })
        })
        .catch((err: ApiError) =>
          errorToast(
            {
              title: t('bridge.toast.create.title'),
              description: t('bridge.toast.create.error'),
            },
            err
          )
        )
    }

    handleEditorOnClose()
  }

  const handleEditorOnDelete = () => {
    onClose()
    onConfirmDeleteOpen()
  }

  const handleConfirmOnClose = () => {
    onConfirmDeleteClose()
    navigate('/mqtt-bridges')
  }

  const handleConfirmOnSubmit = () => {
    if (bridgeId)
      deleteBridge.mutateAsync(bridgeId).then(() => {
        successToast({
          title: t('bridge.toast.delete.title'),
          description: t('bridge.toast.delete.description'),
        })
      })
  }

  return (
    <div>
      <BridgeMainDrawer
        isNewBridge={bridgeId === undefined}
        isOpen={isOpen}
        onClose={handleEditorOnClose}
        onSubmit={handleEditorOnSubmit}
        onDelete={handleEditorOnDelete}
        isSubmitting={createBridge.isLoading || updateBridge.isLoading}
        error={createBridge.error || updateBridge.error}
      />
      {children}
      <ConfirmationDialog
        isOpen={isConfirmDeleteOpen}
        onClose={handleConfirmOnClose}
        onSubmit={handleConfirmOnSubmit}
        message={t('modals.generics.confirmation')}
        header={t('modals.deleteBridgeDialog.header')}
      />
    </div>
  )
}

export default BridgeEditor
