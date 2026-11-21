package team.projectpulse.ram.usecase;

import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import jakarta.persistence.EntityManager;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.ram.requirement.ArtifactLink;
import team.projectpulse.ram.requirement.ArtifactLinkRepository;
import team.projectpulse.ram.requirement.ArtifactLinkType;
import team.projectpulse.system.StatusCode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UseCaseControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UseCaseRepository useCaseRepository;

    @Autowired
    ArtifactLinkRepository artifactLinkRepository;

    @Autowired
    EntityManager entityManager;

    String adminBingyangToken; // Bingyang is the instructor of section 2

    String adminTimToken; // Tim is the instructor of section 3

    String studentJohnToken; // John is on team 1 in section 2

    String studentEricToken; // Eric is on team 1 in section 2

    String studentWoodyToken; // Woody is on team 2 in section 2

    @Value("${api.endpoint.base-url}")
    String baseUrl;

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

    void addUseCase() throws Exception {
        // Given
        String json = """
                {
                      "title": "Edit a section-based requirement document",
                      "description": "The Student wants to edit the content in a structured, section-based requirement document so that project requirements are recorded in the correct sections and remain current.",
                      "teamId": 1,
                      "primaryActorId": 1,
                      "secondaryActorIds": [
                          2
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

    void addUseCase_NotSameTeam() throws Exception {
        // Given
        String json = """
                {
                      "title": "Edit a section-based requirement document",
                      "description": "The Student wants to edit the content in a structured, section-based requirement document so that project requirements are recorded in the correct sections and remain current.",
                      "teamId": 1,
                      "primaryActorId": 1,
                      "secondaryActorIds": [
                          2
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
    // Rewords the title, steps and an extension. The conditions go back with their stored wording, since a use case
    // never rewords a condition (BR-shared-condition-wording); UseCaseConditionIntegrationTest shows that a changed
    // wording sent with a condition is ignored.
    void updateUseCaseWithRewordedContent() throws Exception {
        lockUseCase(1, 3, this.studentEricToken);
        int version = fetchUseCaseVersion(1, 3, this.studentEricToken);
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
                             "id": 4,
                             "condition": "The Student is logged into the System.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 5,
                             "condition": "The Student is a member of the team that owns the use case document.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         }
                     ],
                     "postconditions": [
                         {
                             "id": 7,
                             "condition": "The new use case is stored in the System.",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 8,
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
        json = addVersionToUseCasePayload(json, version);

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

    void updateUseCaseWithNewSteps() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);
        int version = fetchUseCaseVersion(1, 3, this.studentJohnToken);
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
                            "id": 6,
                            "condition": "Document is not locked for review.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 5,
                            "condition": "The Student is a member of the team that owns the use case document.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 4,
                            "condition": "The Student is logged into the System.",
                            "type": "PRECONDITION",
                            "priority": null,
                            "notes": ""
                        }
                    ],
                    "postconditions": [
                        {
                            "id": 7,
                            "condition": "The new use case is stored in the System.",
                            "type": "POSTCONDITION",
                            "priority": null,
                            "notes": ""
                        },
                        {
                            "id": 8,
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
        json = addVersionToUseCasePayload(json, version);

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

    void updateUseCaseWithDeletedSteps() throws Exception {
        lockUseCase(1, 3, this.adminBingyangToken);
        int version = fetchUseCaseVersion(1, 3, this.adminBingyangToken);
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
                             "id": 5,
                             "condition": "The Student is a member of the team that owns the use case document.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 4,
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
                             "id": 8,
                             "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 7,
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
        json = addVersionToUseCasePayload(json, version);

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
                             "id": 5,
                             "condition": "The Student is a member of the team that owns the use case document.",
                             "type": "PRECONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 4,
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
                             "id": 8,
                             "condition": "The System records collaboration metadata (e.g., last-modified timestamp and editor identity).",
                             "type": "POSTCONDITION",
                             "priority": null,
                             "notes": ""
                         },
                         {
                             "id": 7,
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

    @Test
    void getUseCaseLockStatus() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find lock status successfully"))
                .andExpect(jsonPath("$.data.useCaseId").value(3));
    }

    @Test

    void lockUseCase() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);

        this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.useCaseId").value(3));
    }

    @Test

    void lockUseCase_AlreadyLocked() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);

        String json = """
                {
                    "reason": "Locking use case for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.LOCKED));
    }

    @Test

    void unlockUseCase() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);

        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Unlock use case successfully"));
    }

    @Test

    void unlockUseCase_DifferentOwner() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);

        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("Only the lock owner or an instructor can unlock this use case."));
    }

    @Test

    void unlockUseCase_Instructor() throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);

        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Unlock use case successfully"));
    }

    @Test
    void getUseCaseLockStatus_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void lockUseCase_NotSameTeam() throws Exception {
        String json = """
                {
                    "reason": "Locking use case for editing"
                }
                """;

        this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void unlockUseCase_NotSameTeam() throws Exception {
        this.mockMvc.perform(delete(this.baseUrl + "/teams/1/use-cases/3/lock")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void findUseCaseById_OtherTeamsUseCaseThroughOwnTeamUrl() throws Exception {
        // Woody is a student on team 2; use case 3 belongs to team 1. The URL names team 2,
        // so the membership guard passes and only service-layer scoping stops this.
        this.mockMvc.perform(get(this.baseUrl + "/teams/2/use-cases/3")
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

    @Test
    void addUseCase_PrimaryActorFromAnotherTeam() throws Exception {
        // The actor id arrives in the request BODY, so neither the route rule nor the
        // team-scoped lookup on the use case itself stops this. Only re-resolving the actor
        // against the caller's team does. Pairs with addUseCase_PrimaryActorFromOwnTeam
        // below, which sends the same payload with a team 1 actor and must succeed.
        long foreignActorId = fetchTeam2StakeholderId();

        String json = """
                {
                      "title": "Cross-team actor probe",
                      "description": "A use case naming another team's stakeholder as its primary actor.",
                      "teamId": 1,
                      "primaryActorId": %d,
                      "secondaryActorIds": [],
                      "trigger": "The student references another team's artifact.",
                      "preconditions": [],
                      "postconditions": [],
                      "mainSteps": []
                }
                """.formatted(foreignActorId);

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

    @Test
    void addUseCase_PrimaryActorFromOwnTeam() throws Exception {
        // Positive control for addUseCase_PrimaryActorFromAnotherTeam: identical payload,
        // an actor that does belong to team 1. Proves the 404 above comes from the team
        // check and not from the minimal payload.
        String json = """
                {
                      "title": "Own-team actor control",
                      "description": "The same minimal use case, naming a team 1 stakeholder.",
                      "teamId": 1,
                      "primaryActorId": 1,
                      "secondaryActorIds": [],
                      "trigger": "The student references an artifact on their own team.",
                      "preconditions": [],
                      "postconditions": [],
                      "mainSteps": []
                }
                """;

        this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS));
    }

    // ---- Actors: BR-use-case-actor-types and BR-team-scoped-access ----

    @Test
    void addUseCase_WithAnExternalSystemAsSecondaryActor() throws Exception {
        // Main flow of UC-DOC-create-use-case with both kinds of actor: a stakeholder as primary actor and an
        // external system as a secondary actor.
        JSONObject externalSystem = fetchFirstArtifact(1, "EXTERNAL_SYSTEM", this.studentJohnToken);
        assertThat(externalSystem.getString("artifactKey")).isEqualTo("SYS-1");
        long externalSystemId = externalSystem.getLong("id");

        JSONObject response = postUseCase(minimalUseCase(1L, new JSONArray().put(externalSystemId)), this.studentJohnToken);

        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        assertThat(response.getJSONObject("data").getLong("primaryActorId")).isEqualTo(1L);
        JSONArray secondaryActorIds = response.getJSONObject("data").getJSONArray("secondaryActorIds");
        assertThat(secondaryActorIds.length()).isEqualTo(1);
        assertThat(secondaryActorIds.getLong(0)).isEqualTo(externalSystemId);

        // The actors are stored as links from the use case's own artifact, with the route's team.
        long useCaseId = response.getJSONObject("data").getLong("id");
        this.entityManager.flush();
        this.entityManager.clear();
        List<ArtifactLink> links = this.artifactLinkRepository.findBySourceArtifactId(useCaseId, 1);
        assertThat(links).extracting(link -> link.getType(), link -> link.getTargetArtifact().getId(), link -> link.getTeam().getTeamId())
                .containsExactlyInAnyOrder(tuple(ArtifactLinkType.PRIMARY_ACTOR, 1L, 1), tuple(ArtifactLinkType.SECONDARY_ACTOR, externalSystemId, 1));
    }

    @Test
    void addUseCase_PrimaryActorAlsoSecondary() throws Exception {
        // Extension 4a: BR-use-case-actor-types forbids one actor in both roles. Nothing is stored.
        long useCasesBefore = this.useCaseRepository.count();
        long linksBefore = this.artifactLinkRepository.count();

        JSONObject response = postUseCase(minimalUseCase(1L, new JSONArray().put(2L).put(1L)), this.studentJohnToken);

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Actor 1 cannot be both the primary actor and a secondary actor.");
        assertThat(this.useCaseRepository.count()).isEqualTo(useCasesBefore);
        assertThat(this.artifactLinkRepository.count()).isEqualTo(linksBefore);
    }

    @Test
    void updateUseCase_PrimaryActorAlsoSecondary() throws Exception {
        // Extension 7a: the same refusal on edit; the stored actors and the version are unchanged.
        assertActorEditRefused(payload -> payload.put("secondaryActorIds", new JSONArray().put(2L).put(payload.getLong("primaryActorId"))),
                StatusCode.INVALID_ARGUMENT, "Actor 1 cannot be both the primary actor and a secondary actor.");
    }

    @Test
    void updateUseCase_ActorsChanged_NoArtifactVersionMoves() throws Exception {
        // A link is not part of either artifact's content, so naming or dropping an actor must not make a teammate's
        // open edit of that actor stale (DI-concurrency-control, UC-ART-edit-artifact 10a). UC-1 (use case 3, its
        // artifact 3) drops STK-2 (artifact 2) and adds the external system SYS-1.
        long externalSystemId = fetchFirstArtifact(1, "EXTERNAL_SYSTEM", this.studentJohnToken).getLong("id");
        Integer droppedBefore = artifactVersion(2L);
        Integer addedBefore = artifactVersion(externalSystemId);
        Integer useCaseArtifactBefore = artifactVersion(3L);

        lockUseCase(1, 3, this.studentJohnToken);
        JSONObject payload = fetchUseCase(1, 3, this.studentJohnToken);
        payload.put("secondaryActorIds", new JSONArray().put(externalSystemId));
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();

        assertThat(artifactVersion(2L)).as("dropped actor STK-2").isEqualTo(droppedBefore);
        assertThat(artifactVersion(externalSystemId)).as("added actor SYS-1").isEqualTo(addedBefore);
        assertThat(artifactVersion(3L)).as("the use case's own artifact").isEqualTo(useCaseArtifactBefore);
    }

    // The stored @Version of an artifact, read after flushing so a pending increment is counted.
    private Integer artifactVersion(long artifactId) {
        this.entityManager.flush();
        this.entityManager.clear();
        return this.entityManager.find(team.projectpulse.ram.requirement.RequirementArtifact.class, artifactId).getVersion();
    }

    @Test
    void updateUseCase_ActorsChanged_OnlyTheChangedLinksAreWritten() throws Exception {
        // UC-1 (use case 3) names STK-1 as primary actor and STK-2 as secondary actor. The edit keeps STK-1, drops
        // STK-2 and adds the external system SYS-1 as a secondary actor.
        long externalSystemId = fetchFirstArtifact(1, "EXTERNAL_SYSTEM", this.studentJohnToken).getLong("id");
        List<ArtifactLink> before = this.artifactLinkRepository.findBySourceArtifactId(3L, 1);
        ArtifactLink primaryBefore = before.stream().filter(link -> link.getType() == ArtifactLinkType.PRIMARY_ACTOR).findFirst().orElseThrow();
        ArtifactLink derivesBefore = before.stream().filter(link -> link.getType() == ArtifactLinkType.DERIVES_FROM).findFirst().orElseThrow();
        this.entityManager.clear();

        lockUseCase(1, 3, this.studentJohnToken);
        JSONObject payload = fetchUseCase(1, 3, this.studentJohnToken);
        payload.put("secondaryActorIds", new JSONArray().put(externalSystemId));
        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());
        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        assertThat(response.getJSONObject("data").getLong("primaryActorId")).isEqualTo(1L);
        assertThat(response.getJSONObject("data").getJSONArray("secondaryActorIds").getLong(0)).isEqualTo(externalSystemId);

        this.entityManager.flush();
        this.entityManager.clear();
        List<ArtifactLink> after = this.artifactLinkRepository.findBySourceArtifactId(3L, 1);
        assertThat(after).extracting(link -> link.getType(), link -> link.getTargetArtifact().getId()).containsExactlyInAnyOrder(
                tuple(ArtifactLinkType.PRIMARY_ACTOR, 1L), tuple(ArtifactLinkType.SECONDARY_ACTOR, externalSystemId), tuple(ArtifactLinkType.DERIVES_FROM, derivesBefore.getTargetArtifact().getId()));
        // The kept links are the same rows: the primary actor's link and UC-1's trace link were not rewritten.
        assertThat(after).extracting(ArtifactLink::getId).contains(primaryBefore.getId(), derivesBefore.getId());
        // Removing an actor removes only the link; the stakeholder stays.
        assertThat(this.entityManager.find(team.projectpulse.ram.requirement.RequirementArtifact.class, 2L)).isNotNull();
    }

    @Test
    void addUseCase_PrimaryActorOfAnotherType() throws Exception {
        // Extension 4a: a functional requirement of the caller's own team is not an actor.
        long functionalRequirementId = fetchFirstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.studentJohnToken).getLong("id");
        long useCasesBefore = this.useCaseRepository.count();

        JSONObject response = postUseCase(minimalUseCase(functionalRequirementId, new JSONArray()), this.studentJohnToken);

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Primary actor " + functionalRequirementId + " must be a stakeholder or an external system.");
        assertThat(this.useCaseRepository.count()).isEqualTo(useCasesBefore);
    }

    @Test
    void addUseCase_SecondaryActorOfAnotherType() throws Exception {
        // Extension 4a for a secondary actor; the message names the field and the id, which is the location 4a1
        // asks for.
        long functionalRequirementId = fetchFirstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.studentJohnToken).getLong("id");

        JSONObject response = postUseCase(minimalUseCase(1L, new JSONArray().put(2L).put(functionalRequirementId)), this.studentJohnToken);

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getString("message")).isEqualTo("Secondary actor " + functionalRequirementId + " must be a stakeholder or an external system.");
    }

    @Test
    void updateUseCase_PrimaryActorOfAnotherType() throws Exception {
        // Extension 7a: the same refusal on edit, and nothing changes.
        long functionalRequirementId = fetchFirstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.studentJohnToken).getLong("id");
        assertActorEditRefused(payload -> payload.put("primaryActorId", functionalRequirementId),
                StatusCode.INVALID_ARGUMENT, "Primary actor " + functionalRequirementId + " must be a stakeholder or an external system.");
    }

    @Test
    void updateUseCase_SecondaryActorOfAnotherType() throws Exception {
        long functionalRequirementId = fetchFirstArtifact(1, "FUNCTIONAL_REQUIREMENT", this.studentJohnToken).getLong("id");
        assertActorEditRefused(payload -> payload.put("secondaryActorIds", new JSONArray().put(functionalRequirementId)),
                StatusCode.INVALID_ARGUMENT, "Secondary actor " + functionalRequirementId + " must be a stakeholder or an external system.");
    }

    @Test
    void updateUseCase_PrimaryActorFromAnotherTeam() throws Exception {
        // The edit path resolves the primary actor through the same team-scoped lookup as create; another team's
        // artifact is refused with 404 and the stored use case is unchanged.
        long foreignActorId = fetchTeam2StakeholderId();
        assertActorEditRefused(payload -> payload.put("primaryActorId", foreignActorId),
                StatusCode.NOT_FOUND, "Could not find requirement artifact with Id " + foreignActorId + " :(");
    }

    @Test
    void updateUseCase_SecondaryActorFromAnotherTeam() throws Exception {
        long foreignActorId = fetchTeam2StakeholderId();
        assertActorEditRefused(payload -> payload.put("secondaryActorIds", new JSONArray().put(foreignActorId)),
                StatusCode.NOT_FOUND, null);
    }

    @Test
    void addUseCase_ActorFromAnotherTeamIsRefusedLikeAnIdThatNamesNothing() throws Exception {
        // The refusal must not reveal that the id exists in another team: the same status and the same message,
        // apart from the id itself, as an id that names no artifact at all.
        long foreignActorId = fetchTeam2StakeholderId();
        long unknownId = 999_999L;

        JSONObject foreign = postUseCase(minimalUseCase(foreignActorId, new JSONArray()), this.studentJohnToken);
        JSONObject unknown = postUseCase(minimalUseCase(unknownId, new JSONArray()), this.studentJohnToken);

        assertThat(foreign.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(unknown.getInt("code")).isEqualTo(StatusCode.NOT_FOUND);
        assertThat(foreign.getString("message").replace(String.valueOf(foreignActorId), "<id>"))
                .isEqualTo(unknown.getString("message").replace(String.valueOf(unknownId), "<id>"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"primaryActorId", "secondaryActorIds", "preconditions", "postconditions", "mainSteps"})
    void addUseCase_MissingField(String field) throws Exception {
        JSONObject payload = minimalUseCase(1L, new JSONArray());
        payload.remove(field);

        JSONObject response = postUseCase(payload, this.studentJohnToken);

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"primaryActorId", "secondaryActorIds", "preconditions", "postconditions", "mainSteps"})
    void updateUseCase_MissingField(String field) throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);
        JSONObject payload = fetchUseCase(1, 3, this.studentJohnToken);
        payload.remove(field);

        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
    }

    @Test
    void addUseCase_WithoutTeamId() throws Exception {
        JSONObject payload = minimalUseCase(1L, new JSONArray());
        payload.remove("teamId");

        JSONObject response = postUseCase(payload, this.studentJohnToken);

        assertThat(response.getBoolean("flag")).as(response.toString()).isTrue();
        assertThat(response.getJSONObject("data").getInt("teamId")).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"mainSteps[0].extensions", "mainSteps[0].extensions[0].steps"})
    void addUseCase_NullNestedList(String path) throws Exception {
        JSONObject payload = minimalUseCase(1L, new JSONArray());
        payload.put("mainSteps", stepTreeWithNullList(path));

        JSONObject response = postUseCase(payload, this.studentJohnToken);

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getJSONObject("data").has(path)).as(response.toString()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"mainSteps[0].extensions", "mainSteps[0].extensions[0].steps"})
    void updateUseCase_NullNestedList(String path) throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);
        JSONObject before = fetchUseCase(1, 3, this.studentJohnToken);
        JSONObject payload = new JSONObject(before.toString());
        payload.put("mainSteps", stepTreeWithNullList(path));

        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(StatusCode.INVALID_ARGUMENT);
        assertThat(response.getJSONObject("data").has(path)).as(response.toString()).isTrue();
        JSONObject after = fetchUseCase(1, 3, this.studentJohnToken);
        assertThat(after.getInt("version")).isEqualTo(before.getInt("version"));
        assertThat(after.getJSONArray("mainSteps").length()).isEqualTo(before.getJSONArray("mainSteps").length());
    }

    /**
     * A one-step tree holding one extension with one step, with the list at {@code path} sent as JSON null.
     */
    private JSONArray stepTreeWithNullList(String path) throws Exception {
        JSONObject extension = new JSONObject()
                .put("conditionText", "The probe fails.")
                .put("kind", "EXCEPTION")
                .put("exit", "RESUME")
                .put("steps", new JSONArray().put(new JSONObject()
                        .put("actor", "System")
                        .put("actionText", "The System reports the failure.")));
        JSONObject step = new JSONObject()
                .put("actor", "Student")
                .put("actionText", "The Student starts the probe.")
                .put("extensions", new JSONArray().put(extension));
        if (path.endsWith(".steps")) {
            extension.put("steps", JSONObject.NULL);
        } else {
            step.put("extensions", JSONObject.NULL);
        }
        return new JSONArray().put(step);
    }

    /**
     * Locks team 1's use case 3, submits it as served with one change, and asserts the refusal and that the stored
     * actors and version are unchanged. A null message skips the message check.
     */
    private void assertActorEditRefused(JsonEdit change, int expectedCode, String expectedMessage) throws Exception {
        lockUseCase(1, 3, this.studentJohnToken);
        JSONObject before = fetchUseCase(1, 3, this.studentJohnToken);
        JSONObject payload = new JSONObject(before.toString());
        change.apply(payload);

        MvcResult result = this.mockMvc.perform(put(this.baseUrl + "/teams/1/use-cases/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andReturn();
        JSONObject response = new JSONObject(result.getResponse().getContentAsString());

        assertThat(response.getBoolean("flag")).isFalse();
        assertThat(response.getInt("code")).as(response.toString()).isEqualTo(expectedCode);
        if (expectedMessage != null) {
            assertThat(response.getString("message")).isEqualTo(expectedMessage);
        }

        JSONObject after = fetchUseCase(1, 3, this.studentJohnToken);
        assertThat(after.getInt("version")).isEqualTo(before.getInt("version"));
        assertThat(after.getLong("primaryActorId")).isEqualTo(before.getLong("primaryActorId"));
        assertThat(after.getJSONArray("secondaryActorIds").toString()).isEqualTo(before.getJSONArray("secondaryActorIds").toString());
    }

    @FunctionalInterface
    private interface JsonEdit {
        void apply(JSONObject payload) throws Exception;
    }

    private JSONObject minimalUseCase(long primaryActorId, JSONArray secondaryActorIds) throws Exception {
        return new JSONObject()
                .put("title", "Actor rule probe")
                .put("description", "A minimal use case for checking actor rules.")
                .put("teamId", 1)
                .put("primaryActorId", primaryActorId)
                .put("secondaryActorIds", secondaryActorIds)
                .put("trigger", "The student starts the probe.")
                .put("preconditions", new JSONArray())
                .put("postconditions", new JSONArray())
                .put("mainSteps", new JSONArray());
    }

    private JSONObject postUseCase(JSONObject payload, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/1/use-cases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload.toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString());
    }

    private JSONObject fetchUseCase(int teamId, int useCaseId, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(get(this.baseUrl + "/teams/" + teamId + "/use-cases/" + useCaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data");
    }

    /**
     * The first artifact of a type in a team, found through that team's search endpoint rather than a hard-coded
     * seeded id.
     */
    private JSONObject fetchFirstArtifact(int teamId, String type, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/" + teamId + "/requirement-artifacts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new JSONObject().put("type", type).toString())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andReturn();
        return new JSONObject(result.getResponse().getContentAsString()).getJSONObject("data").getJSONArray("content").getJSONObject(0);
    }

    /**
     * Looks up a team 2 artifact id through team 2's own search endpoint, as a team 2 student,
     * rather than hard-coding a seeded id that future seed changes would silently invalidate.
     */
    private long fetchTeam2StakeholderId() throws Exception {
        String searchJson = """
                {
                    "type": "STAKEHOLDER"
                }
                """;
        MvcResult result = this.mockMvc.perform(post(this.baseUrl + "/teams/2/requirement-artifacts/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(searchJson)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andReturn();
        JSONObject json = new JSONObject(result.getResponse().getContentAsString());
        return json.getJSONObject("data").getJSONArray("content").getJSONObject(0).getLong("id");
    }

    private void lockUseCase(int teamId, int useCaseId, String token) throws Exception {
        String json = """
                {
                    "reason": "Locking use case for editing"
                }
                """;
        this.mockMvc.perform(put(this.baseUrl + "/teams/" + teamId + "/use-cases/" + useCaseId + "/lock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Lock use case successfully"))
                .andExpect(jsonPath("$.data.locked").value(true))
                .andExpect(jsonPath("$.data.useCaseId").value(useCaseId));
    }

    private int fetchUseCaseVersion(int teamId, int useCaseId, String token) throws Exception {
        MvcResult result = this.mockMvc.perform(
                        get(this.baseUrl + "/teams/" + teamId + "/use-cases/" + useCaseId)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, token)
                )
                .andReturn();
        String content = result.getResponse().getContentAsString();
        JSONObject json = new JSONObject(content);
        return json.getJSONObject("data").getInt("version");
    }

    private String addVersionToUseCasePayload(String payload, int version) {
        int lastBrace = payload.lastIndexOf('}');
        if (lastBrace < 0) {
            return payload;
        }
        return payload.substring(0, lastBrace) + ",\n                     \"version\": " + version + "\n                 }";
    }

}
