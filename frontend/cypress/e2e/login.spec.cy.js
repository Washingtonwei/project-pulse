describe('Login Page', () => {
  beforeEach(() => {
    // Visit the login page before each test
    cy.visit('/')
  })

  it('displays the login page correctly', () => {
    // Check if the login page is rendered
    cy.contains('h1', 'Login').should('be.visible')
  })

  it('shows error messages when fields are empty', () => {
    // Try to submit the form without entering anything
    cy.get('.button').click()

    // Check for validation error messages
    cy.contains('Username cannot be empty.').should('be.visible')
    cy.contains('Password cannot be empty.').should('be.visible')
  })

  it('logs in successfully with correct credentials', () => {
    // Fill out the username and password fields
    cy.get('input[placeholder="Type your username here"]').type('john')
    cy.get('input[placeholder="Type your password here"]').type('123456')

    // Submit the form
    cy.get('.button').click()

    // Check for a successful login message
    cy.contains('Login successfully!').should('be.visible')

    // Verify that the user is redirected to the correct page (e.g., home page)
    cy.url().should('include', '/home')
  })

  it('handles "Forget password" click', () => {
    // Click the "Forget password?" link
    cy.contains('Forget password?').click()

    // Check that the user is redirected to the forget-password page
    cy.url().should('include', '/forget-password')
  })
})
