import { FC, useMemo } from 'react'
import {
  Drawer,
  DrawerBody,
  DrawerFooter,
  DrawerHeader,
  DrawerOverlay,
  DrawerContent,
  DrawerCloseButton,
  Flex,
  Text,
  Image,
  HStack,
} from '@chakra-ui/react'
import { useTranslation } from 'react-i18next'
import { useParams } from 'react-router-dom'
import { IChangeEvent } from '@rjsf/core'
import { RJSFSchema } from '@rjsf/utils'
import Form from '@rjsf/chakra-ui'
import validator from '@rjsf/validator-ajv8'

import { ApiError, Adapter, ProtocolAdapter } from '@/api/__generated__'
import { useGetAdapterTypes } from '@/api/hooks/useProtocolAdapters/useGetAdapterTypes.tsx'
import { useListProtocolAdapters } from '@/api/hooks/useProtocolAdapters/useListProtocolAdapters.tsx'

import ButtonCTA from '@/components/Chakra/ButtonCTA.tsx'
import LoaderSpinner from '@/components/Chakra/LoaderSpinner.tsx'

import { ObjectFieldTemplate } from '@/modules/ProtocolAdapters/components/adapters/ObjectFieldTemplate.tsx'
import useGetUiSchema from '@/modules/ProtocolAdapters/hooks/useGetUISchema.ts'

interface AdapterInstanceDrawerProps {
  adapterType?: string
  isNewAdapter?: boolean
  isOpen: boolean
  isSubmitting: boolean
  error?: ApiError | null
  onClose: () => void
  onSubmit: (data: Adapter) => void
  onDelete?: () => void
}

const AdapterInstanceDrawer: FC<AdapterInstanceDrawerProps> = ({
  adapterType,
  isNewAdapter = false,
  isOpen,
  onClose,
  onSubmit,
}) => {
  const { t } = useTranslation()
  const { data } = useGetAdapterTypes()
  const { data: allAdapters } = useListProtocolAdapters()
  const { adapterId } = useParams()

  const { schema, name, logo } = useMemo(() => {
    const adapter: ProtocolAdapter | undefined = data?.items?.find((e) => e.id === adapterType)
    const { configSchema } = adapter || {}
    return { schema: configSchema, name: adapter?.name, logo: adapter?.logoUrl }
  }, [data, adapterType])

  const defaultValues = useMemo(() => {
    if (isNewAdapter || !adapterId) return undefined
    const { config } = allAdapters?.find((e) => e.id === adapterId) || {}
    return config
  }, [allAdapters, adapterId, isNewAdapter])
  const uiSchema = useGetUiSchema(isNewAdapter)

  const onValidate = (data: IChangeEvent<Adapter, RJSFSchema>) => {
    if (data.formData) onSubmit(data.formData)
  }

  return (
    <Drawer closeOnOverlayClick={false} size={'lg'} isOpen={isOpen} placement="right" onClose={onClose}>
      <DrawerOverlay />
      <DrawerContent aria-label={t('protocolAdapter.drawer.label') as string}>
        {!schema && <LoaderSpinner />}
        {schema && (
          <>
            <DrawerCloseButton />
            <DrawerHeader id={'adapter-instance-header'} borderBottomWidth="1px">
              <Text>
                {isNewAdapter ? t('protocolAdapter.drawer.title.create') : t('protocolAdapter.drawer.title.update')}
              </Text>
              <HStack>
                <Image boxSize="30px" objectFit="scale-down" src={logo} aria-label={name} />
                <Text fontSize={'md'} fontWeight={'500'}>
                  {name}
                </Text>
              </HStack>
            </DrawerHeader>
            <DrawerBody>
              {schema && (
                <>
                  <Form
                    id="adapter-instance-form"
                    schema={schema}
                    uiSchema={uiSchema}
                    templates={{ ObjectFieldTemplate }}
                    liveValidate
                    onSubmit={onValidate}
                    validator={validator}
                    showErrorList={'bottom'}
                    onError={(errors) => console.log('XXXXXXX', errors)}
                    formData={defaultValues}
                  />
                </>
              )}
            </DrawerBody>

            <DrawerFooter borderTopWidth="1px">
              <Flex flexGrow={1} justifyContent={'flex-end'}>
                <ButtonCTA type="submit" form="adapter-instance-form">
                  {isNewAdapter ? t('protocolAdapter.action.create') : t('protocolAdapter.action.update')}
                </ButtonCTA>
              </Flex>
            </DrawerFooter>
          </>
        )}
      </DrawerContent>
    </Drawer>
  )
}

export default AdapterInstanceDrawer
