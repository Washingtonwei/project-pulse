// UC-NOT-remind-non-submitters: the reminder dialog. The two API calls are stubbed, so these tests check what the
// dialog does with each answer and what it sends; the backend's own tests cover who is listed and emailed.
// Run against the dev stack (backend + `npm run dev`) with `npm run cy:open`.

const owingStatus = {
  flag: true,
  code: 200,
  message: 'Find submission status successfully',
  data: {
    item: 'PEER_EVALUATION',
    week: '2023-W32',
    weekRange: '08-07-2023 to 08-13-2023',
    weekActive: true,
    dueDay: 'TUESDAY',
    dueTime: '23:59',
    teams: [
      {
        teamId: 2,
        teamName: 'Team2',
        students: [
          { id: 7, firstName: 'Woody', lastName: 'Allen' },
          { id: 8, firstName: 'Amanda', lastName: 'Wagner' }
        ]
      }
    ]
  }
}

const withData = (data) => ({ ...owingStatus, data: { ...owingStatus.data, ...data } })

describe('Remind students who have not submitted', () => {
  beforeEach(() => {
    cy.login('b.gates@abc.edu', '123456')
    cy.intercept('POST', '**/sections/*/reminders', {
      delay: 1000, // Long enough to click Confirm again while the request is in flight
      body: {
        flag: true,
        code: 200,
        message: 'Send reminders successfully',
        data: { item: 'PEER_EVALUATION', week: '2023-W32', sent: 2, failed: [] }
      }
    }).as('reminders')
  })

  function openAndChoosePeerEvaluation(statusBody) {
    cy.intercept('GET', '**/sections/*/submission-status*', { body: statusBody }).as('status')
    cy.visit('/section-evaluations')
    cy.get('[data-cy=remind-open]').click()
    cy.get('[data-cy=remind-item-peer-evaluation]').click()
    cy.wait('@status').its('request.query.item').should('eq', 'PEER_EVALUATION')
  }

  it('lists the owing students by team (step 4)', () => {
    openAndChoosePeerEvaluation(owingStatus)

    cy.get('[data-cy=remind-week]').should('contain', '08-07-2023 to 08-13-2023').and('contain', 'Tuesday at 11:59 PM')
    cy.get('[data-cy=remind-owing]').should('contain', 'Team2').and('contain', 'Woody Allen').and('contain', 'Amanda Wagner')
    cy.get('[data-cy=remind-confirm]').should('be.enabled')
  })

  it('sends no reminder when the instructor cancels (5a)', () => {
    openAndChoosePeerEvaluation(owingStatus)

    cy.get('[data-cy=remind-cancel]').click()

    cy.get('[data-cy=remind-dialog]').should('not.be.visible')
    cy.get('@reminders.all').should('have.length', 0)
  })

  it('sends one request however often Confirm is clicked while it is in flight (step 5)', () => {
    openAndChoosePeerEvaluation(owingStatus)

    cy.get('[data-cy=remind-confirm]').click()
    cy.get('[data-cy=remind-confirm]').should('be.disabled')
    cy.get('[data-cy=remind-confirm]').click({ force: true }) // A second click on the disabled button

    cy.wait('@reminders').its('request.body').should('deep.equal', { item: 'PEER_EVALUATION' })
    cy.get('[data-cy=remind-result]').should('contain', '2 reminders sent.')
    cy.get('@reminders.all').should('have.length', 1)
  })

  it('names the students it could not reach (6a2)', () => {
    cy.intercept('POST', '**/sections/*/reminders', {
      body: {
        flag: true,
        code: 200,
        message: 'Send reminders successfully',
        data: { item: 'PEER_EVALUATION', week: '2023-W32', sent: 1, failed: [{ id: 8, firstName: 'Amanda', lastName: 'Wagner' }] }
      }
    })
    openAndChoosePeerEvaluation(owingStatus)

    cy.get('[data-cy=remind-confirm]').click()

    cy.get('[data-cy=remind-result]').should('contain', '1 reminder sent.')
    cy.get('[data-cy=remind-failed]').should('contain', 'Amanda Wagner')
  })

  it('offers no send for an inactive week (4a)', () => {
    openAndChoosePeerEvaluation(withData({ weekActive: false }))

    cy.get('[data-cy=remind-inactive-week]').should('contain', 'Reminders are sent only for an active week.')
    cy.get('[data-cy=remind-confirm]').should('not.exist')
  })

  it('offers no send when nobody owes the item (4b)', () => {
    openAndChoosePeerEvaluation(withData({ teams: [] }))

    cy.get('[data-cy=remind-nobody-owes]').should('contain', 'Every student who can submit it has submitted it.')
    cy.get('[data-cy=remind-confirm]').should('not.exist')
  })
})
