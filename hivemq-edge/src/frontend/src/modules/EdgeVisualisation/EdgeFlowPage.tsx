import { FC } from 'react'
import { useTranslation } from 'react-i18next'

import PageContainer from '@/components/PageContainer.tsx'

import { EdgeFlowProvider } from './hooks/FlowContext.tsx'
import ReactFlowWrapper from './components/ReactFlowWrapper.tsx'

const EdgeFlowPage: FC = () => {
  const { t } = useTranslation()

  return (
    <PageContainer title={t('welcome.title') as string} subtitle={t('welcome.description') as string}>
      <EdgeFlowProvider>
        <ReactFlowWrapper />
      </EdgeFlowProvider>
    </PageContainer>
  )
}

export default EdgeFlowPage
