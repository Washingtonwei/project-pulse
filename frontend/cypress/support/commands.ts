/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

// Set CYPRESS_COMMAND_DELAY above zero for demoing to stakeholders,
// E.g. CYPRESS_COMMAND_DELAY=1000 node_modules/.bin/cypress open
const COMMAND_DELAY = 200

;['visit', 'click', 'trigger', 'type', 'clear', 'reload', 'select'].forEach((command) => {
  Cypress.Commands.overwrite(
    command as unknown as keyof Cypress.Chainable<any>,
    (originalFn, ...args) => {
      const origVal = originalFn(...args)

      return new Promise((resolve) => {
        setTimeout(() => {
          resolve(origVal)
        }, COMMAND_DELAY)
      })
    }
  )
})

Cypress.Commands.add('login', (username, password) => {
  cy.visit('/login') // Visit login page if needed

  cy.get('input[placeholder="Type your username here"]').type(username)
  cy.get('input[placeholder="Type your password here"]').type(password)

  cy.get('.button').click() // Click the login button

  // Add any additional checks to confirm successful login (e.g., URL change, token check, etc.)
  cy.url().should('include', '/home') // Ensure user is redirected to dashboard after login
})

// Cypress.Commands.add('login', (username, password) => {
//   const encodedCredentials = btoa(`${username}:${password}`)

//   // Set the Authorization header with the encoded credentials
//   cy.request({
//     method: 'POST',
//     url: 'http://127.0.0.1:80/api/v1/users/login', // Replace with your actual API endpoint
//     headers: {
//       Authorization: `Basic ${encodedCredentials}`
//     }
//   }).then((response) => {
//     const { token, userInfo } = response.body.data

//     // Set the token and userInfo in localStorage
//     window.localStorage.setItem('token', token)
//     window.localStorage.setItem('userInfo', JSON.stringify(userInfo))

//     if (userInfo.roles.includes('instructor')) {
//       const settings = {
//         defaultSectionId: userInfo.defaultSectionId || null,
//         defaultCourseId: userInfo.defaultCourseId || null
//       }
//       // Store the settings object in localStorage under the "settings" key
//       window.localStorage.setItem('settings', JSON.stringify(settings))
//     }
//   })
// })
