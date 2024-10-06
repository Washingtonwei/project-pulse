describe('Dashboard Menu Test with Login', () => {
  beforeEach(() => {
    // Use the custom login command to log in before each test
    cy.login('john', '123456')

    // Visit the dashboard page after logging in
    cy.visit('/home')
  })

  it('Should display submenus when clicking "Weekly Activity Reports"', () => {
    // Step 1: Find and click the "Weekly Activity Reports" menu item
    cy.contains('Weekly Activity Reports')
      .should('be.visible') // Ensure the menu item is visible
      .click() // Click to expand the submenus

    // Step 2: Verify that the submenus "My Activities" and "Team\'s Activities" appear
    cy.contains('My Activities').should('be.visible') // Ensure "My Activities" submenu is visible

    cy.contains("Team's Activities").should('be.visible') // Ensure "Team's Activities" submenu is visible
  })

  it('Should navigate to "My Activities" when submenu is clicked', () => {
    // Step 1: Click the "Weekly Activity Reports" menu item to reveal the submenus
    cy.contains('Weekly Activity Reports').click()

    // Step 2: Click "My Activities" and verify the navigation to the correct page
    cy.contains('My Activities').click()

    // Step 3: Assert the new URL or page content to ensure correct navigation
    cy.url().should('include', '/my-activities') // Adjust the URL check as needed
    cy.contains('My Weekly Activities').should('be.visible') // Adjust the page content check
  })

  it('Should navigate to "Team\'s Activities" when submenu is clicked', () => {
    // Step 1: Click the "Weekly Activity Reports" menu item to reveal the submenus
    cy.contains('Weekly Activity Reports').click()

    // Step 2: Click "Team\'s Activities" and verify the navigation to the correct page
    cy.contains("Team's Activities").click()

    // Step 3: Assert the new URL or page content to ensure correct navigation
    cy.url().should('include', '/team-activities') // Adjust the URL check as needed
    cy.contains("Team's Weekly Activities").should('be.visible') // Adjust the page content check
  })
})
