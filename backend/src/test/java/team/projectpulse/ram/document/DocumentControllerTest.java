package team.projectpulse.ram.document;

import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import team.projectpulse.AbstractIntegrationTest;
import team.projectpulse.system.StatusCode;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DocumentControllerTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

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
    void findDocumentsByCriteria() throws Exception {
        String json = """
                {
                    "type": "SRS"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement documents successfully"))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Software Requirements Specification"))
                .andExpect(jsonPath("$.data.content[0].type").value("SRS"));
    }

    @Test
    void findDocumentsByCriteria_InstructorFromDifferentSection() throws Exception {
        String json = """
                {
                    "type": "SRS"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminTimToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void findDocumentById() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find requirement document successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Software Requirements Specification"))
                .andExpect(jsonPath("$.data.sections", Matchers.hasSize(31)));
    }

    @Test
    void findDocumentById_NotSameTeam() throws Exception {
        this.mockMvc.perform(get(this.baseUrl + "/teams/1/documents/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void createVisionScopeDocument() throws Exception {
        // Team 3, which the seed gives no documents: teams 1 and 2 already have a Vision and Scope.
        String json = """
                {
                    "type": "VISION_SCOPE"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Create requirement document successfully"))
                .andExpect(jsonPath("$.data.title").value("Vision and Scope Document"))
                .andExpect(jsonPath("$.data.sections", Matchers.hasSize(18)));
    }

    @Test
    void createBusinessRulesDocument_OneListSectionForRules() throws Exception {
        // Team 3, since team 1 already has the Business Rules document the seed provisions.
        String json = """
                {
                    "type": "BUSINESS_RULES"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.title").value("Business Rules"))
                .andExpect(jsonPath("$.data.sections", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.data.sections[0].sectionKey").value("BUSINESS_RULES"))
                .andExpect(jsonPath("$.data.sections[0].type").value("LIST"));
    }

    @Test
    void createGlossaryAndUseCasesDocuments_FromTheirSectionlessTemplates() throws Exception {
        // Team 3, which the seed gives no documents. Both come from a template with a title and no sections: their glossary
        // terms and use cases are authored in their own editors, not in document sections.
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content("""
                        { "type": "GLOSSARY" }
                        """).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.title").value("Project Glossary"))
                .andExpect(jsonPath("$.data.sections", Matchers.hasSize(0)));
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content("""
                        { "type": "USE_CASES" }
                        """).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.title").value("Use Cases"))
                .andExpect(jsonPath("$.data.sections", Matchers.hasSize(0)));
    }

    @Test
    void createUserStoriesDocument_Extension4a_NoTemplate_Refused() throws Exception {
        // USER_STORIES has no built-in template, so team 3 gets a 400 naming the type, and no document is stored.
        String json = """
                {
                    "type": "USER_STORIES"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("No built-in template is configured for USER_STORIES, so no document of that type can be created."));
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents/search").contentType(MediaType.APPLICATION_JSON).content("{}").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(0)));
    }

    @Test
    void createArchitecturalDesignDocument_Arc42SectionsAndThreeLists() throws Exception {
        // Team 2, which the seed gives a Vision and Scope and an SRS but no Architectural Design (docs/design/tpl.md).
        String json = """
                {
                    "type": "ARCHITECTURAL_DESIGN"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/2/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.title").value("Architectural Design"))
                .andExpect(jsonPath("$.data.sections[*].sectionKey", Matchers.contains(
                        "INTRODUCTION_AND_GOALS", "REQUIREMENTS_OVERVIEW", "QUALITY_GOALS", "STAKEHOLDERS",
                        "ARCHITECTURE_CONSTRAINTS", "CONTEXT_AND_SCOPE", "SOLUTION_STRATEGY",
                        "BUILDING_BLOCK_VIEW", "CONTAINERS", "USE_CASE_AREAS_AND_COMPONENTS",
                        "RUNTIME_VIEW", "DEPLOYMENT_VIEW",
                        "CROSSCUTTING_CONCEPTS", "SECURITY_CONCEPT", "OTHER_CONCEPTS",
                        "ARCHITECTURE_DECISIONS", "SIGNIFICANT_REQUIREMENTS", "KEY_DECISIONS",
                        "QUALITY_REQUIREMENTS", "QUALITY_REQUIREMENTS_OVERVIEW", "QUALITY_SCENARIOS",
                        "RISKS_AND_TECHNICAL_DEBT", "ARCHITECTURE_GLOSSARY")))
                // The three lists are the 18th, 21st and 22nd sections; every other section is rich text.
                .andExpect(jsonPath("$.data.sections[?(@.type == 'LIST')].sectionKey", Matchers.contains("KEY_DECISIONS", "QUALITY_SCENARIOS", "RISKS_AND_TECHNICAL_DEBT")))
                .andExpect(jsonPath("$.data.sections[17].artifactTypes", Matchers.contains("KEY_DECISION")))
                .andExpect(jsonPath("$.data.sections[20].artifactTypes", Matchers.contains("QUALITY_SCENARIO")))
                .andExpect(jsonPath("$.data.sections[21].artifactTypes", Matchers.contains("TECHNICAL_RISK", "TECHNICAL_DEBT")));
    }

    @Test
    void createArchitecturalDesignDocument_Extension3a_AlreadyExists_Refused() throws Exception {
        // Team 1 already has the Architectural Design the seed gives it: a second one is refused with a 400 the course
        // admin can read (BR-document-creation), and nothing is stored.
        String json = """
                {
                    "type": "ARCHITECTURAL_DESIGN"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.INVALID_ARGUMENT))
                .andExpect(jsonPath("$.message").value("This team already has the Architectural Design. A team's document is created once and never regenerated or overwritten."));
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents/search").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.data.content", Matchers.hasSize(1)));
    }

    @Test
    void createSrsDocument_TheCourseStructureWithoutArchitecture() throws Exception {
        // Team 3, which the seed gives no documents. The SRS follows the course's SRS template (docs/design/tpl.md, The
        // SRS template): the Overall Description block ahead of the two pointers, Data Dictionary and Reports under the
        // data requirements, Hardware ahead of Software Interfaces, and no architecture sections, which moved to the
        // Architectural Design.
        String json = """
                {
                    "type": "SRS"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/3/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.adminBingyangToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.data.sections[*].sectionKey", Matchers.contains(
                        "INTRODUCTION", "SYSTEM_PURPOSE", "DOCUMENT_PURPOSE", "DOCUMENT_CONVENTIONS", "REFERENCES",
                        "OVERALL_DESCRIPTION", "PRODUCT_PERSPECTIVE", "USER_CLASSES",
                        "OPERATING_ENVIRONMENT", "DESIGN_CONSTRAINTS", "ASSUMPTIONS_DEPENDENCIES",
                        "PROJECT_GLOSSARY", "VISION_SCOPE_REFERENCE",
                        "FUNCTIONAL_REQUIREMENTS", "USE_CASES", "NON_USE_CASE_FUNCTIONAL_REQUIREMENTS", "BUSINESS_RULES",
                        "DATA_REQUIREMENTS", "BUSINESS_DOMAIN_MODEL", "DATA_DICTIONARY", "REPORTS", "DATA_LIFECYCLE",
                        "EXTERNAL_INTERFACES", "USER_INTERFACES", "HARDWARE_INTERFACES", "SOFTWARE_INTERFACES",
                        "API_DOCUMENTATION", "COMMUNICATION_INTERFACES",
                        "QUALITY_ATTRIBUTES", "I18N_L10N", "OTHER_REQUIREMENTS")))
                .andExpect(jsonPath("$.data.sections[?(@.sectionKey in ['OVERALL_DESCRIPTION', 'PRODUCT_PERSPECTIVE', 'USER_CLASSES', 'DATA_DICTIONARY', 'REPORTS'])].type",
                        Matchers.everyItem(Matchers.is("RICH_TEXT"))))
                .andExpect(jsonPath("$.data.sections[?(@.sectionKey in ['OVERALL_DESCRIPTION', 'PRODUCT_PERSPECTIVE', 'USER_CLASSES', 'DATA_DICTIONARY', 'REPORTS'])]",
                        Matchers.hasSize(5)));
    }

    @Test
    void createVisionScopeDocumentByStudent() throws Exception {
        String json = """
                {
                    "type": "VISION_SCOPE"
                }
                """;
        this.mockMvc.perform(post(this.baseUrl + "/teams/1/documents").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentJohnToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void updateDocumentStatus() throws Exception {
        String json = """
                {
                    "status": "SUBMITTED"
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/documents/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentEricToken))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update requirement document status successfully"));
    }

    @Test
    void updateDocumentStatus_NotSameTeam() throws Exception {
        String json = """
                {
                    "status": "SUBMITTED"
                }
                """;
        this.mockMvc.perform(patch(this.baseUrl + "/teams/1/documents/1").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.FORBIDDEN))
                .andExpect(jsonPath("$.message").value("No permission."));
    }

    @Test
    void findDocumentById_OtherTeamsDocumentThroughOwnTeamUrl() throws Exception {
        // Woody is a student on team 2; document 1 belongs to team 1. The URL names team 2,
        // so the membership guard passes and only service-layer scoping stops this.
        this.mockMvc.perform(get(this.baseUrl + "/teams/2/documents/1").accept(MediaType.APPLICATION_JSON).header(HttpHeaders.AUTHORIZATION, this.studentWoodyToken))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND));
    }

}
