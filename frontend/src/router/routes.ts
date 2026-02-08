import type { RouteRecordRaw } from 'vue-router'

export const routes = [
  {
    path: '/login',
    component: () => import('@/pages/Login.vue'),
    name: 'login',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/register',
    component: () => import('@/pages/UserRegistration.vue'),
    name: 'register',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/forget-password',
    component: () => import('@/pages/ForgetPassword.vue'),
    name: 'forget-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/reset-password',
    component: () => import('@/pages/ResetPassword.vue'),
    name: 'reset-password',
    meta: { requiresAuth: false, visitorOnly: true }
  },
  {
    path: '/',
    component: () => import('@/pages/Dashboard.vue'),
    name: 'dashboard',
    redirect: '/home',
    children: [
      {
        path: '/home',
        component: () => import('@/pages/Home.vue'),
        name: 'home',
        meta: {
          title: 'Home',
          icon: 'HomeFilled',
          isMenuItem: true,
          requiresAuth: true
        }
      },
      {
        path: '/activities',
        name: 'activities',
        meta: {
          title: 'Weekly Activity Reports',
          icon: 'Memo',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student']
        },
        redirect: '/activities/my-activities',
        children: [
          {
            path: '/activities/my-activities',
            component: () => import('@/pages/activities/MyActivities.vue'),
            name: 'my-activities',
            meta: {
              title: 'My Activities',
              icon: 'Document',
              isMenuItem: true,
              requiresAuth: true
            }
          },
          {
            path: '/activities/team-activities',
            component: () => import('@/pages/activities/TeamsActivities.vue'),
            name: 'team-activities',
            meta: {
              title: "Team's Activities",
              icon: 'DocumentCopy',
              isMenuItem: true,
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: '/evaluations',
        name: 'evaluations',
        meta: {
          title: 'Peer Evaluations',
          icon: 'DataAnalysis',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student']
        },
        redirect: '/evaluations/my-evaluations',
        children: [
          {
            path: '/evaluations/my-evaluations',
            component: () => import('@/pages/evaluations/MyEvaluations.vue'),
            name: 'my-evaluations',
            meta: {
              title: 'My Evaluations',
              icon: 'GoldMedal',
              isMenuItem: true,
              requiresAuth: true
            }
          },
          {
            path: '/evaluations/submit-evaluations',
            component: () => import('@/pages/evaluations/SubmitTeamsEvaluations.vue'),
            name: 'team-evaluations',
            meta: {
              title: 'Submit Evaluations',
              icon: 'Trophy',
              isMenuItem: true,
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: '/ram',
        name: 'ram',
        meta: {
          title: 'Requirements',
          icon: 'Document',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['student']
        },
        redirect: '/ram/documents',
        children: [
          {
            path: '/ram/documents',
            component: () => import('@/pages/ram/RamDocuments.vue'),
            name: 'ram-documents',
            meta: {
              title: 'Documents',
              icon: 'Files',
              isMenuItem: true,
              requiresAuth: true,
              requiresPermissions: ['student']
            }
          },
          {
            path: '/ram/documents/:documentId',
            component: () => import('@/pages/ram/RamDocumentEditor.vue'),
            name: 'ram-document-editor',
            meta: {
              title: 'Document Editor',
              icon: 'EditPen',
              isMenuItem: false,
              requiresAuth: true,
              requiresPermissions: ['student']
            }
          },
          {
            path: '/ram/glossary/:documentId',
            component: () => import('@/pages/ram/RamGlossary.vue'),
            name: 'ram-glossary',
            meta: {
              title: 'Glossary',
              icon: 'Collection',
              isMenuItem: false,
              requiresAuth: true,
              requiresPermissions: ['student']
            }
          },
          {
            path: '/ram/use-cases/:documentId',
            component: () => import('@/pages/ram/RamUseCases.vue'),
            name: 'ram-use-cases',
            meta: {
              title: 'Use Cases',
              icon: 'Tickets',
              isMenuItem: false,
              requiresAuth: true,
              requiresPermissions: ['student']
            }
          }
        ]
      },
      {
        path: '/courses',
        component: () => import('@/pages/courses/Courses.vue'),
        name: 'courses',
        meta: {
          title: 'Courses',
          icon: 'OfficeBuilding',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/sections',
        component: () => import('@/pages/sections/Sections.vue'),
        name: 'sections',
        meta: {
          title: 'Sections',
          icon: 'School',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/teams',
        component: () => import('@/pages/teams/Teams.vue'),
        name: 'teams',
        meta: {
          title: 'Teams',
          icon: 'Ship',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['admin']
        }
      },
      {
        path: '/students',
        component: () => import('@/pages/students/Students.vue'),
        name: 'students',
        meta: {
          title: 'Students',
          icon: 'User',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/students/:studentId',
        component: () => import('@/pages/students/StudentPerformanceDashboard.vue'),
        name: 'student-performance-dashboard',
        meta: {
          title: 'Performance Dashboard',
          icon: 'Sunny',
          isMenuItem: false,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/section-activities',
        component: () => import('@/pages/activities/admin/SectionsActivities.vue'),
        name: 'section-activities',
        meta: {
          title: "Section's Activities",
          icon: 'DocumentCopy',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/section-evaluations',
        component: () => import('@/pages/evaluations/admin/SectionsEvaluations.vue'),
        name: 'section-evaluations',
        meta: {
          title: "Section's Evaluations",
          icon: 'DataAnalysis',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['instructor']
        }
      },
      {
        path: '/rubrics-criteria',
        name: 'rubrics-criteria',
        meta: {
          title: 'Rubrics',
          icon: 'Odometer',
          isMenuItem: true,
          requiresAuth: true,
          requiresPermissions: ['admin']
        },
        redirect: '/rubrics-criteria/rubrics',
        children: [
          {
            path: '/activities/criteria',
            component: () => import('@/pages/rubrics/Criteria.vue'),
            name: 'criteria',
            meta: {
              title: 'Criteria',
              icon: 'DocumentChecked',
              isMenuItem: true
            }
          },
          {
            path: '/rubrics-criteria/rubrics',
            component: () => import('@/pages/rubrics/Rubrics.vue'),
            name: 'rubrics',
            meta: {
              title: 'Rubrics',
              icon: 'Checked',
              isMenuItem: true
            }
          }
        ]
      },
      {
        path: '/user',
        name: 'current-user',
        meta: {
          title: 'My Profile',
          icon: 'Setting',
          isMenuItem: true,
          requiresAuth: true
        },
        redirect: '/user/profile',
        children: [
          {
            path: '/user/profile',
            component: () => import('@/pages/currentUser/UserInfo.vue'),
            name: 'user-profile',
            meta: {
              title: 'User Profile',
              icon: 'User',
              isMenuItem: true,
              requiresAuth: true
            }
          },
          {
            path: '/user/reset-password',
            component: () => import('@/pages/currentUser/UserResetPassword.vue'),
            name: 'user-reset-password',
            meta: {
              title: 'Reset Password',
              icon: 'EditPen',
              isMenuItem: true,
              requiresAuth: true
            }
          }
        ]
      }
    ]
  },
  {
    path: '/403',
    component: () => import('@/pages/errors/Forbidden.vue'),
    name: 'forbidden'
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/pages/errors/NotFound.vue'),
    name: 'not-found'
  }
] as RouteRecordRaw[]


