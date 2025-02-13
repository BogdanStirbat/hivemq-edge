/// <reference types="cypress" />

import { mockReactFlow } from '@/__test-utils__/react-flow/providers.tsx'
import { MOCK_NODE_ADAPTER } from '@/__test-utils__/react-flow/nodes.ts'
import { MOCK_TOPIC_REF1, MOCK_TOPIC_REF2 } from '@/__test-utils__/react-flow/topics.ts'
import { mockProtocolAdapter } from '@/api/hooks/useProtocolAdapters/__handlers__'

import NodeAdapter from './NodeAdapter.tsx'

describe('NodeAdapter', () => {
  beforeEach(() => {
    cy.viewport(400, 400)
    cy.intercept('/api/v1/management/protocol-adapters/types', { items: [mockProtocolAdapter] })
  })

  it('should render properly', () => {
    cy.mountWithProviders(mockReactFlow(<NodeAdapter {...MOCK_NODE_ADAPTER} />))

    cy.getByTestId('adapter-node-name').should('contain', 'my-id')
    cy.getByTestId('connection-status').should('contain.text', 'Connected')
    cy.getByTestId('topics-container')
      .should('be.visible')
      .should('contain.text', MOCK_TOPIC_REF1)
      .should('contain.text', MOCK_TOPIC_REF2)
  })

  it('should be accessible', () => {
    cy.injectAxe()
    cy.mountWithProviders(mockReactFlow(<NodeAdapter {...MOCK_NODE_ADAPTER} />))

    cy.checkAccessibility(undefined, {
      rules: {
        // TODO[NVL] Font too small, creating accessibility issues. Need fix
        'color-contrast': { enabled: false },
      },
    })
    cy.percySnapshot('Component: NodeAdapter')
  })
})
