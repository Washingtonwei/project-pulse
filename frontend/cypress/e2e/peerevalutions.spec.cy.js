describe('Peer Evaluation Menu Test', () => {
  beforeEach(() => {
    // Use the custom login command to log in before each test
    cy.login('john', '123456')

    // Visit the dashboard page after logging in
    cy.visit('/home')
  })

  it('Should display submenus when clicking "Peer Evaluation"', () => {
    // Step 1: Find and click the "Peer Evaluation" menu item
    cy.contains('Peer Evaluation')
      .should('be.visible') // Ensure the menu item is visible
      .click() // Click to expand the submenus

    // Step 2: Verify that the submenus "My Evaluations" and "Submit Evaluations" appear
    cy.contains('My Evaluations').should('be.visible') // Ensure "My Evaluations" submenu is visible

    cy.contains('Submit Evaluations').should('be.visible') // Ensure "Submit Evaluations" submenu is visible
  })

  it('Should navigate to "My Evaluations" when submenu is clicked', () => {
    // Step 1: Click the "Peer Evaluation" menu item to reveal the submenus
    cy.contains('Peer Evaluation').click()

    // Step 2: Click "My Evaluations" and verify the navigation to the correct page
    cy.contains('My Evaluations').click()

    // Step 3: Assert the new URL or page content to ensure correct navigation
    cy.url().should('include', '/my-evaluations') // Adjust the URL check as needed
    cy.contains('My Peer Evaluations').should('be.visible') // Adjust the page content check
  })

  it('Should navigate to "Submit Evaluations" when submenu is clicked', () => {
    // Step 1: Click the "Peer Evaluation" menu item to reveal the submenus
    cy.contains('Peer Evaluation').click()

    // Step 2: Click "Submit Evaluations" and verify the navigation to the correct page
    cy.contains('Submit Evaluations').click()

    // Step 3: Assert the new URL or page content to ensure correct navigation
    cy.url().should('include', '/submit-evaluations') // Adjust the URL check as needed
    cy.contains('Submit Peer Evaluations').should('be.visible') // Adjust the page content check
  })
})
