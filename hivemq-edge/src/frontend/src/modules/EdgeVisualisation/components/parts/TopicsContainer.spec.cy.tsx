/// <reference types="cypress" />

import TopicsContainer from './TopicsContainer.tsx'

describe('NodeListener', () => {
  beforeEach(() => {
    cy.viewport(400, 400)
  })

  it('should render properly all topics', () => {
    cy.mountWithProviders(
      <TopicsContainer
        topics={[
          {
            topic: 'my/first/topic',
          },
          {
            topic: 'my/second/topic',
          },
        ]}
      />
    )

    cy.getByTestId('topic-wrapper').should('have.length', 2)
    cy.getByTestId('topic-wrapper').eq(0).should('contain.text', 'my/first/topic')
    cy.getByTestId('topic-wrapper').eq(1).should('contain.text', 'my/second/topic')

    cy.getByTestId('topics-show-more').should('not.exist')
  })

  it('should render properly a show more if too many topics', () => {
    cy.mountWithProviders(
      <TopicsContainer
        topics={[
          {
            topic: 'my/first/topic',
          },
          {
            topic: 'my/second/topic',
          },
          {
            topic: 'my/second/topic',
          },
        ]}
      />
    )

    cy.getByTestId('topic-wrapper').should('have.length', 2)
    cy.getByTestId('topic-wrapper').eq(0).should('contain.text', 'my/first/topic')
    cy.getByTestId('topic-wrapper').eq(1).should('contain.text', 'my/second/topic')

    cy.getByTestId('topics-show-more').should('be.visible').should('contain.text', '+1')
  })
})
