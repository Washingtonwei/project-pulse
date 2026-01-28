package team.projectpulse.ram.usecase;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import team.projectpulse.system.StatusCode;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class UseCaseControllerTest {

    @Autowired
    MockMvc mockMvc;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is on team 1 in section 2

    String studentEricToken; // Eric is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"));

    @BeforeEach
    void setUp() throws Exception {
        ResultActions resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("b.wei@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONObject json = new JSONObject(contentAsString);
        this.adminBingyangToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("t.cook@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.adminTimToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("j.smith@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentJohnToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("e.hudson@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentEricToken = "Bearer " + json.getJSONObject("data").getString("token");

        resultActions = this.mockMvc.perform(post(this.baseUrl + "/users/login").with(httpBasic("w.allen@abc.edu", "123456"))); // httpBasic() is from spring-security-test.
        mvcResult = resultActions.andDo(print()).andReturn();
        contentAsString = mvcResult.getResponse().getContentAsString();
        json = new JSONObject(contentAsString);
        this.studentWoodyToken = "Bearer " + json.getJSONObject("data").getString("token");
    }

    @Test
    void findUseCaseById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/3").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find use case successfully"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.title").value("Create a use case"))
                .andExpect(jsonPath("$.data.mainSteps", Matchers.hasSize(10)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void addUseCase() throws Exception {
        // Given
        String json = """
                {
                      "title": "Edit a section-based requirement document",
                      "description": "The Student wants to edit the content in a structured, section-based requirement document so that project requirements are recorded in the correct sections and remain current.",
                      "teamId": 1,
                      "primaryActorId": 1,
                      "secondaryActorIds": [
                          2,
                          3
                      ],
                      "trigger": "The Student indicates to edit a section-based requirement document (e.g., Vision & Scope, SRS).",
                      "preconditions": [
                          {
                              "condition": "The Student is logged into the System.",
                              "type": "PRECONDITION"
                          },
                          {
                              "condition": "The Student is a member of the team that owns the document.",
                              "type": "PRECONDITION"
                          },
                          {
                              "condition": "Document is not locked for review.",
                              "type": "PRECONDITION"
                          }
                      ],
                      "postconditions": [
                          {
                              "condition": "The updated section content is stored in the System.",
                              "type": "POSTCONDITION"
                          },
                          {
                              "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                              "type": "POSTCONDITION"
                          }
                      ],
                      "mainSteps": [
                          {
                              "actor": "Student",
                              "actionText": "The Student indicates to edit a section-based requirement document (e.g., Vision and Scope, SRS).",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student views the sections of the document through UC-: View a section-based requirement document.",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student selects a section to edit.",
                              "extensions": [
                                  {
                                      "conditionText": "Section locked by another team member",
                                      "kind": "EXCEPTION",
                                      "exit": "RESUME",
                                      "steps": [
                                          {
                                              "actor": "System",
                                              "actionText": "The System alerts the Student that another team member is currently editing this section."
                                          },
                                          {
                                              "actor": "Student",
                                              "actionText": "The Student either chooses to edit a different section or chooses to terminate the use case."
                                          }
                                      ]
                                  }
                              ]
                          },
                          {
                              "actor": "System",
                              "actionText": "The System displays the section’s guidance and examples, as well as the current content in an editor.",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student edits the section content.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System performs basic validation on the edited content and displays any warnings or errors.",
                              "extensions": [
                                  {
                                      "conditionText": "Input validation rule violation",
                                      "kind": "EXCEPTION",
                                      "exit": "RESUME",
                                      "steps": [
                                          {
                                              "actor": "System",
                                              "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."
                                          },
                                          {
                                              "actor": "Student",
                                              "actionText": "The Student corrects the mistake and returns to step 7 of the normal flow."
                                          }
                                      ]
                                  }
                              ]
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student confirms completion of the edit.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System saves the updated content, updates collaboration metadata, and confirms that the document has been updated.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System notifies relevant team members of the change, as configured.",
                              "extensions": []
                          },
                          {
                              "actor": "",
                              "actionText": "Use case ends.",
                              "extensions": []
                          }
                      ],
                      "priority": "CRITICAL",
                      "notes": ""
                }
                """;

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add use case successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.title").value("Edit a section-based requirement document"))
                .andExpect(jsonPath("$.data.mainSteps", hasSize(10)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void addUseCase_NotSameTeam() throws Exception {
        // Given
        String json = """
                {
                      "title": "Edit a section-based requirement document",
                      "description": "The Student wants to edit the content in a structured, section-based requirement document so that project requirements are recorded in the correct sections and remain current.",
                      "teamId": 1,
                      "primaryActorId": 1,
                      "secondaryActorIds": [
                          2,
                          3
                      ],
                      "trigger": "The Student indicates to edit a section-based requirement document (e.g., Vision & Scope, SRS).",
                      "preconditions": [
                          {
                              "condition": "The Student is logged into the System.",
                              "type": "PRECONDITION"
                          },
                          {
                              "condition": "The Student is a member of the team that owns the document.",
                              "type": "PRECONDITION"
                          },
                          {
                              "condition": "Document is not locked for review.",
                              "type": "PRECONDITION"
                          }
                      ],
                      "postconditions": [
                          {
                              "condition": "The updated section content is stored in the System.",
                              "type": "POSTCONDITION"
                          },
                          {
                              "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                              "type": "POSTCONDITION"
                          }
                      ],
                      "mainSteps": [
                          {
                              "actor": "Student",
                              "actionText": "The Student indicates to edit a section-based requirement document (e.g., Vision and Scope, SRS).",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student views the sections of the document through UC-: View a section-based requirement document.",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student selects a section to edit.",
                              "extensions": [
                                  {
                                      "conditionText": "Section locked by another team member",
                                      "kind": "EXCEPTION",
                                      "exit": "RESUME",
                                      "steps": [
                                          {
                                              "actor": "System",
                                              "actionText": "The System alerts the Student that another team member is currently editing this section."
                                          },
                                          {
                                              "actor": "Student",
                                              "actionText": "The Student either chooses to edit a different section or chooses to terminate the use case."
                                          }
                                      ]
                                  }
                              ]
                          },
                          {
                              "actor": "System",
                              "actionText": "The System displays the section’s guidance and examples, as well as the current content in an editor.",
                              "extensions": []
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student edits the section content.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System performs basic validation on the edited content and displays any warnings or errors.",
                              "extensions": [
                                  {
                                      "conditionText": "Input validation rule violation",
                                      "kind": "EXCEPTION",
                                      "exit": "RESUME",
                                      "steps": [
                                          {
                                              "actor": "System",
                                              "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."
                                          },
                                          {
                                              "actor": "Student",
                                              "actionText": "The Student corrects the mistake and returns to step 7 of the normal flow."
                                          }
                                      ]
                                  }
                              ]
                          },
                          {
                              "actor": "Student",
                              "actionText": "The Student confirms completion of the edit.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System saves the updated content, updates collaboration metadata, and confirms that the document has been updated.",
                              "extensions": []
                          },
                          {
                              "actor": "System",
                              "actionText": "The System notifies relevant team members of the change, as configured.",
                              "extensions": []
                          },
                          {
                              "actor": "",
                              "actionText": "Use case ends.",
                              "extensions": []
                          }
                      ],
                      "priority": "CRITICAL",
                      "notes": ""
                }
                """;

        // When and then
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUseCaseWithRewordedContent() throws Exception {
        // Given
        String json = """
                {
                     "id": 3,
                     "artifactKey": "UC-01",
                     "title": "Create a use case (UPDATED)",
                     "description": "The Student wants to create a new use case in the use case document so that a new user requirement is added in the document.",
                     "teamId": 1,
                     "primaryActorId": 1,
                     "secondaryActorIds": [
                         2
                     ],
                     "trigger": "The Student indicates to create a new use case in the use case document.",
                     "preconditions": [
                         {
                             "id": 6,
                             "condition": "Document is not locked for review.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 8,
                             "condition": "The Student is logged into the System.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 7,
                             "condition": "The Student is a member of the team that owns the use case document. (UPDATED)",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "postconditions": [
                         {
                             "id": 4,
                             "condition": "The new use case is stored in the System. (UPDATED)",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 5,
                             "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "mainSteps": [
                         {
                             "id": 1,
                             "actor": "Student",
                             "actionText": "The Student indicates to create a new use case in the use case document. (UPDATED)",
                             "extensions": []
                         },
                         {
                             "id": 2,
                             "actor": "System",
                             "actionText": "The System asks the Student to enter the details of this new use case according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 3,
                             "actor": "Student",
                             "actionText": "The Student enters the details of this new use case and confirms that she has finished.",
                             "extensions": []
                         },
                         {
                             "id": 4,
                             "actor": "System",
                             "actionText": "The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 1,
                                     "conditionText": "Input validation rule violation (UPDATED)",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 1,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error. (UPDATED)"
                                         },
                                         {
                                             "id": 2,
                                             "actor": "Student",
                                             "actionText": "The Student corrects the mistake and returns to step 4 of the normal flow."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 5,
                             "actor": "System",
                             "actionText": "The System validates that the creation of the new use case will not duplicate any existing use case according to the “Duplication detection rules” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 2,
                                     "conditionText": "The System finds possible duplicates from the existing use cases",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 3,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that the use case she is trying to create already exists in the System."
                                         },
                                         {
                                             "id": 4,
                                             "actor": "Student",
                                             "actionText": "The Student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 6,
                             "actor": "System",
                             "actionText": "The System displays the details of the new use case and asks the Student to confirm the creation.",
                             "extensions": []
                         },
                         {
                             "id": 7,
                             "actor": "Student",
                             "actionText": "The Student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).",
                             "extensions": []
                         },
                         {
                             "id": 8,
                             "actor": "System",
                             "actionText": "The System saves the new use case and informs the Student that this use case has been created.",
                             "extensions": []
                         },
                         {
                             "id": 9,
                             "actor": "System",
                             "actionText": "The System notifies relevant actors about the creation of the use case according to the “Notification” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 10,
                             "actor": "",
                             "actionText": "Use case ends.",
                             "extensions": []
                         }
                     ],
                     "priority": "CRITICAL",
                     "notes": "Updated notes"
                 }
                """;

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update use case successfully"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.title").value("Create a use case (UPDATED)"))
                .andExpect(jsonPath("$.data.mainSteps", hasSize(10)))
                .andExpect(jsonPath("$.data.mainSteps[0].actionText").value("The Student indicates to create a new use case in the use case document. (UPDATED)"))
                .andExpect(jsonPath("$.data.mainSteps[3].extensions[0].conditionText").value("Input validation rule violation (UPDATED)"));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUseCaseWithNewSteps() throws Exception {
        // Given
        String json = """
                {
                    "id": 3,
                    "title": "Create a use case",
                    "description": "The Student wants to create a new use case in the use case document so that a new user requirement is added in the document.",
                    "teamId": 1,
                    "primaryActorId": 1,
                    "secondaryActorIds": [
                        2
                    ],
                    "trigger": "The Student indicates to create a new use case in the use case document.",
                    "preconditions": [
                        {
                            "id": 8,
                            "condition": "Document is not locked for review.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 7,
                            "condition": "The Student is a member of the team that owns the use case document.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 6,
                            "condition": "The Student is logged into the System.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        }
                    ],
                    "postconditions": [
                        {
                            "id": 4,
                            "condition": "The new use case is stored in the System.",
                            "type": "POSTCONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 5,
                            "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                            "type": "POSTCONDITION",
                            "priority": null,
                            "notes": ""
                        }
                    ],
                    "mainSteps": [
                        {
                            "id": 1,
                            "actor": "Student",
                            "actionText": "The Student indicates to create a new use case in the use case document.",
                            "extensions": []
                        },
                        {
                            "id": 2,
                            "actor": "System",
                            "actionText": "The System asks the Student to enter the details of this new use case according to the “Details” defined in the Associated Information of this use case.",
                            "extensions": []
                        },
                        {
                            "id": 3,
                            "actor": "Student",
                            "actionText": "The Student enters the details of this new use case and confirms that she has finished.",
                            "extensions": []
                        },
                        {
                            "id": 4,
                            "actor": "System",
                            "actionText": "The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case.",
                            "extensions": [
                                {
                                    "id": 1,
                                    "conditionText": "Input validation rule violation",
                                    "kind": "EXCEPTION",
                                    "exit": "RESUME",
                                    "steps": [
                                        {
                                            "id": 1,
                                            "actor": "System",
                                            "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."
                                        },
                                        {
                                            "id": 2,
                                            "actor": "Student",
                                            "actionText": "The Student corrects the mistake and returns to step 4 of the normal flow."
                                        },
                                        {
                                            "actor": "Student",
                                            "actionText": "This is a new step."
                                        }
                                    ]
                                },
                                {
                                    "conditionText": "This is a new extension condition.",
                                    "kind": "EXCEPTION",
                                    "exit": "RESUME",
                                    "steps": [
                                        {
                                            "actor": "System",
                                            "actionText": "This is a new step."
                                        },
                                        {
                                            "actor": "Student",
                                            "actionText": "This is a new step."
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "id": 5,
                            "actor": "System",
                            "actionText": "The System validates that the creation of the new use case will not duplicate any existing use case according to the “Duplication detection rules” defined in the Associated Information of this use case.",
                            "extensions": [
                                {
                                    "id": 2,
                                    "conditionText": "The System finds possible duplicates from the existing use cases",
                                    "kind": "EXCEPTION",
                                    "exit": "RESUME",
                                    "steps": [
                                        {
                                            "id": 3,
                                            "actor": "System",
                                            "actionText": "The System alerts the Student that the use case she is trying to create already exists in the System."
                                        },
                                        {
                                            "id": 4,
                                            "actor": "Student",
                                            "actionText": "The Student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case."
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            "id": 6,
                            "actor": "System",
                            "actionText": "The System displays the details of the new use case and asks the Student to confirm the creation.",
                            "extensions": []
                        },
                        {
                            "id": 7,
                            "actor": "Student",
                            "actionText": "The Student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).",
                            "extensions": []
                        },
                        {
                            "id": 8,
                            "actor": "System",
                            "actionText": "The System saves the new use case and informs the Student that this use case has been created.",
                            "extensions": []
                        },
                        {
                            "id": 9,
                            "actor": "System",
                            "actionText": "The System notifies relevant actors about the creation of the use case according to the “Notification” defined in the Associated Information of this use case.",
                            "extensions": []
                        },
                        {
                            "actor": "System",
                            "actionText": "This is a new step.",
                            "extensions": []
                        },
                        {
                            "id": 10,
                            "actor": "",
                            "actionText": "Use case ends.",
                            "extensions": []
                        }
                    ],
                    "priority": "CRITICAL",
                    "notes": ""
                }
                """;

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update use case successfully"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.mainSteps", hasSize(11)))
                .andExpect(jsonPath("$.data.mainSteps[3].extensions", hasSize(2)))
                .andExpect(jsonPath("$.data.mainSteps[3].extensions[0].steps", hasSize(3)))
                .andExpect(jsonPath("$.data.mainSteps[3].extensions[0].steps[2].actionText").value("This is a new step."))
                .andExpect(jsonPath("$.data.mainSteps[3].extensions[1].conditionText").value("This is a new extension condition."));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUseCaseWithDeletedSteps() throws Exception {
        // Given
        String json = """
                {
                     "id": 3,
                     "artifactKey": "UC-01",
                     "title": "Create a use case",
                     "description": "The Student wants to create a new use case in the use case document so that a new user requirement is added in the document.",
                     "teamId": 1,
                     "primaryActorId": 1,
                     "secondaryActorIds": [
                         2
                     ],
                     "trigger": "The Student indicates to create a new use case in the use case document.",
                     "preconditions": [
                         {
                             "id": 7,
                             "condition": "The Student is a member of the team that owns the use case document.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 8,
                             "condition": "The Student is logged into the System.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 6,
                             "condition": "Document is not locked for review.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "postconditions": [
                         {
                             "id": 4,
                             "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 5,
                             "condition": "The new use case is stored in the System.",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "mainSteps": [
                         {
                             "id": 1,
                             "actor": "Student",
                             "actionText": "The Student indicates to create a new use case in the use case document.",
                             "extensions": []
                         },
                         {
                             "id": 2,
                             "actor": "System",
                             "actionText": "The System asks the Student to enter the details of this new use case according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 4,
                             "actor": "System",
                             "actionText": "The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 1,
                                     "conditionText": "Input validation rule violation",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 1,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 5,
                             "actor": "System",
                             "actionText": "The System validates that the creation of the new use case will not duplicate any existing use case according to the “Duplication detection rules” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 2,
                                     "conditionText": "The System finds possible duplicates from the existing use cases",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 3,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that the use case she is trying to create already exists in the System."
                                         },
                                         {
                                             "id": 4,
                                             "actor": "Student",
                                             "actionText": "The Student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 6,
                             "actor": "System",
                             "actionText": "The System displays the details of the new use case and asks the Student to confirm the creation.",
                             "extensions": []
                         },
                         {
                             "id": 7,
                             "actor": "Student",
                             "actionText": "The Student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).",
                             "extensions": []
                         },
                         {
                             "id": 8,
                             "actor": "System",
                             "actionText": "The System saves the new use case and informs the Student that this use case has been created.",
                             "extensions": []
                         },
                         {
                             "id": 9,
                             "actor": "System",
                             "actionText": "The System notifies relevant actors about the creation of the use case according to the “Notification” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 10,
                             "actor": "",
                             "actionText": "Use case ends.",
                             "extensions": []
                         }
                     ],
                     "priority": "CRITICAL",
                     "notes": ""
                 }
                """;

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update use case successfully"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.mainSteps", hasSize(9)))
                .andExpect(jsonPath("$.data.mainSteps[2].id").value(4))
                .andExpect(jsonPath("$.data.mainSteps[2].actionText").value("The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case."))
                .andExpect(jsonPath("$.data.mainSteps[2].extensions[0].steps", hasSize(1)));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void updateUseCaseWithDeletedSteps_NotSameTeam() throws Exception {
        // Given
        String json = """
                {
                     "id": 3,
                     "artifactKey": "UC-01",
                     "title": "Create a use case",
                     "description": "The Student wants to create a new use case in the use case document so that a new user requirement is added in the document.",
                     "teamId": 1,
                     "primaryActorId": 1,
                     "secondaryActorIds": [
                         2
                     ],
                     "trigger": "The Student indicates to create a new use case in the use case document.",
                     "preconditions": [
                         {
                             "id": 7,
                             "condition": "The Student is a member of the team that owns the use case document.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 8,
                             "condition": "The Student is logged into the System.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 6,
                             "condition": "Document is not locked for review.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "postconditions": [
                         {
                             "id": 4,
                             "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 5,
                             "condition": "The new use case is stored in the System.",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "mainSteps": [
                         {
                             "id": 1,
                             "actor": "Student",
                             "actionText": "The Student indicates to create a new use case in the use case document.",
                             "extensions": []
                         },
                         {
                             "id": 2,
                             "actor": "System",
                             "actionText": "The System asks the Student to enter the details of this new use case according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 4,
                             "actor": "System",
                             "actionText": "The System validates the Student’s inputs according to the “Details” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 1,
                                     "conditionText": "Input validation rule violation",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 1,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that an input validation rule is violated and displays the nature and location of the error."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 5,
                             "actor": "System",
                             "actionText": "The System validates that the creation of the new use case will not duplicate any existing use case according to the “Duplication detection rules” defined in the Associated Information of this use case.",
                             "extensions": [
                                 {
                                     "id": 2,
                                     "conditionText": "The System finds possible duplicates from the existing use cases",
                                     "kind": "EXCEPTION",
                                     "exit": "RESUME",
                                     "steps": [
                                         {
                                             "id": 3,
                                             "actor": "System",
                                             "actionText": "The System alerts the Student that the use case she is trying to create already exists in the System."
                                         },
                                         {
                                             "id": 4,
                                             "actor": "Student",
                                             "actionText": "The Student either chooses to correct the mistake and return to step 4 of the normal flow or chooses to terminate the use case."
                                         }
                                     ]
                                 }
                             ]
                         },
                         {
                             "id": 6,
                             "actor": "System",
                             "actionText": "The System displays the details of the new use case and asks the Student to confirm the creation.",
                             "extensions": []
                         },
                         {
                             "id": 7,
                             "actor": "Student",
                             "actionText": "The Student either confirms the creation (continues the normal flow) or chooses to modify the details (returns to step 3).",
                             "extensions": []
                         },
                         {
                             "id": 8,
                             "actor": "System",
                             "actionText": "The System saves the new use case and informs the Student that this use case has been created.",
                             "extensions": []
                         },
                         {
                             "id": 9,
                             "actor": "System",
                             "actionText": "The System notifies relevant actors about the creation of the use case according to the “Notification” defined in the Associated Information of this use case.",
                             "extensions": []
                         },
                         {
                             "id": 10,
                             "actor": "",
                             "actionText": "Use case ends.",
                             "extensions": []
                         }
                     ],
                     "priority": "CRITICAL",
                     "notes": ""
                 }
                """;

        // When and then
        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

}