package team.projectpulse.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import team.projectpulse.security.authorizationmanagers.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    private final RSAPublicKey publicKey;

    private final RSAPrivateKey privateKey;

    @Value("${api.endpoint.base-url}")
    private String baseUrl;

    private final CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    private final CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint;

    private final CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler;

    private final UserOwnershipAuthorizationManager userOwnershipAuthorizationManager;

    private final CourseOwnershipAuthorizationManager courseOwnershipAuthorizationManager;

    private final CourseMembershipAuthorizationManager courseMembershipAuthorizationManager;

    private final CriterionOwnershipAuthorizationManager criterionOwnershipAuthorizationManager;

    private final RubricOwnershipAuthorizationManager rubricOwnershipAuthorizationManager;

    private final CriterionMembershipAuthorizationManager criterionMembershipAuthorizationManager;

    private final RubricMembershipAuthorizationManager rubricMembershipAuthorizationManager;

    private final AssignCriterionToRubricAuthorizationManager assignCriterionToRubricAuthorizationManager;

    private final SectionMembershipAuthorizationManager sectionMembershipAuthorizationManager;

    private final SectionOwnershipAuthorizationManager sectionOwnershipAuthorizationManager;

    private final AssignRubricToSectionAuthorizationManager assignRubricToSectionAuthorizationManager;

    private final AssignInstructorToSectionAuthorizationManager assignInstructorToSectionAuthorizationManager;

    private final TeamMembershipAuthorizationManager teamMembershipAuthorizationManager;

    private final TeamOwnershipAuthorizationManager teamOwnershipAuthorizationManager;

    private final AssignStudentToTeamAuthorizationManager assignStudentToTeamAuthorizationManager;

    private final AssignInstructorToTeamAuthorizationManager assignInstructorToTeamAuthorizationManager;

    private final TransferTeamAuthorizationManager transferTeamAuthorizationManager;

    private final ActivityOwnershipAuthorizationManager activityOwnershipAuthorizationManager;

    private final ActivityMembershipAuthorizationManager activityMembershipAuthorizationManager;

    private final EvaluationOwnershipAuthorizationManager evaluationOwnershipAuthorizationManager;

    private final SectionInstructorAuthorizationManager sectionInstructorAuthorizationManager;

    private final StudentInstructorAuthorizationManager studentInstructorAuthorizationManager;


    public SecurityConfiguration(CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint, CustomBearerTokenAuthenticationEntryPoint customBearerTokenAuthenticationEntryPoint, CustomBearerTokenAccessDeniedHandler customBearerTokenAccessDeniedHandler, UserOwnershipAuthorizationManager userOwnershipAuthorizationManager, CourseOwnershipAuthorizationManager courseOwnershipAuthorizationManager, CourseMembershipAuthorizationManager courseMembershipAuthorizationManager, CriterionOwnershipAuthorizationManager criterionOwnershipAuthorizationManager, RubricOwnershipAuthorizationManager rubricOwnershipAuthorizationManager, CriterionMembershipAuthorizationManager criterionMembershipAuthorizationManager, RubricMembershipAuthorizationManager rubricMembershipAuthorizationManager, AssignCriterionToRubricAuthorizationManager assignCriterionToRubricAuthorizationManager, SectionMembershipAuthorizationManager sectionMembershipAuthorizationManager, SectionOwnershipAuthorizationManager sectionOwnershipAuthorizationManager, AssignRubricToSectionAuthorizationManager assignRubricToSectionAuthorizationManager, AssignInstructorToSectionAuthorizationManager assignInstructorToSectionAuthorizationManager, TeamMembershipAuthorizationManager teamMembershipAuthorizationManager, TeamOwnershipAuthorizationManager teamOwnershipAuthorizationManager, AssignStudentToTeamAuthorizationManager assignStudentToTeamAuthorizationManager, AssignInstructorToTeamAuthorizationManager assignInstructorToTeamAuthorizationManager, TransferTeamAuthorizationManager transferTeamAuthorizationManager, ActivityOwnershipAuthorizationManager activityOwnershipAuthorizationManager, ActivityMembershipAuthorizationManager activityMembershipAuthorizationManager, EvaluationOwnershipAuthorizationManager evaluationOwnershipAuthorizationManager, SectionInstructorAuthorizationManager sectionInstructorAuthorizationManager, StudentInstructorAuthorizationManager studentInstructorAuthorizationManager) throws NoSuchAlgorithmException {
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
        this.customBearerTokenAuthenticationEntryPoint = customBearerTokenAuthenticationEntryPoint;
        this.customBearerTokenAccessDeniedHandler = customBearerTokenAccessDeniedHandler;
        this.userOwnershipAuthorizationManager = userOwnershipAuthorizationManager;
        this.courseOwnershipAuthorizationManager = courseOwnershipAuthorizationManager;
        this.courseMembershipAuthorizationManager = courseMembershipAuthorizationManager;
        this.criterionOwnershipAuthorizationManager = criterionOwnershipAuthorizationManager;
        this.rubricOwnershipAuthorizationManager = rubricOwnershipAuthorizationManager;
        this.criterionMembershipAuthorizationManager = criterionMembershipAuthorizationManager;
        this.rubricMembershipAuthorizationManager = rubricMembershipAuthorizationManager;
        this.assignCriterionToRubricAuthorizationManager = assignCriterionToRubricAuthorizationManager;
        this.sectionMembershipAuthorizationManager = sectionMembershipAuthorizationManager;
        this.sectionOwnershipAuthorizationManager = sectionOwnershipAuthorizationManager;
        this.assignRubricToSectionAuthorizationManager = assignRubricToSectionAuthorizationManager;
        this.assignInstructorToSectionAuthorizationManager = assignInstructorToSectionAuthorizationManager;
        this.teamMembershipAuthorizationManager = teamMembershipAuthorizationManager;
        this.teamOwnershipAuthorizationManager = teamOwnershipAuthorizationManager;
        this.assignStudentToTeamAuthorizationManager = assignStudentToTeamAuthorizationManager;
        this.assignInstructorToTeamAuthorizationManager = assignInstructorToTeamAuthorizationManager;
        this.transferTeamAuthorizationManager = transferTeamAuthorizationManager;
        this.activityOwnershipAuthorizationManager = activityOwnershipAuthorizationManager;
        this.activityMembershipAuthorizationManager = activityMembershipAuthorizationManager;
        this.evaluationOwnershipAuthorizationManager = evaluationOwnershipAuthorizationManager;
        this.sectionInstructorAuthorizationManager = sectionInstructorAuthorizationManager;
        this.studentInstructorAuthorizationManager = studentInstructorAuthorizationManager;

        // Generate a public/private key pair.
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // The generated key will have a size of 2048 bits.
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        this.publicKey = (RSAPublicKey) keyPair.getPublic();
        this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        // Security rules for the /courses/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/courses/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/courses").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/courses/{courseId}").access(this.courseMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/courses/{courseId}").access(this.courseOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/courses/{courseId}").access(this.courseOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/courses/{courseId}/instructors/email-invitations").access(this.courseOwnershipAuthorizationManager)

                        // Security rules for the /criteria/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/criteria/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/criteria").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/criteria/{criterionId}").access(this.criterionMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/criteria/{criterionId}").access(this.criterionOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/criteria/{criterionId}").access(this.criterionOwnershipAuthorizationManager)

                        // Security rules for the /rubrics/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/rubrics/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/rubrics").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/rubrics/{rubricId}").access(this.rubricMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/rubrics/{rubricId}").access(this.rubricOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/rubrics/{rubricId}").access(this.rubricOwnershipAuthorizationManager)
                        .requestMatchers(this.baseUrl + "/rubrics/{rubricId}/criteria/{criterionId}").access(this.assignCriterionToRubricAuthorizationManager)

                        // Security rules for the /sections/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/sections/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/sections").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/sections/{sectionId}").access(this.sectionMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/sections/{sectionId}").access(this.sectionOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/sections/{sectionId}").access(this.sectionOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/sections/{sectionId}/weeks").access(this.sectionOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/sections/{sectionId}/rubrics/{rubricId}").access(this.assignRubricToSectionAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/sections/{sectionId}/instructors/{instructorId}").access(this.assignInstructorToSectionAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/sections/{sectionId}/instructors/{instructorId}").access(this.assignInstructorToSectionAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/sections/{sectionId}/students/email-invitations").access(this.sectionOwnershipAuthorizationManager)

                        // Security rules for the /teams/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}").access(this.teamOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/teams/{teamId}/section").access(this.transferTeamAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}").access(this.teamOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/students/{studentId}").access(this.assignStudentToTeamAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/students/{studentId}").access(this.assignStudentToTeamAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/instructors/{instructorId}").access(this.assignInstructorToTeamAuthorizationManager)

                        // Security rules for the /instructors/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/instructors/search").hasAuthority("ROLE_admin")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/instructors").permitAll()
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/instructors/{instructorId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/instructors/{instructorId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/instructors/{instructorId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/instructors/sections/{sectionId}/default").access(this.sectionMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/instructors/courses/{courseId}/default").access(this.courseMembershipAuthorizationManager)

                        // Security rules for the /students/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/students/search").hasAuthority("ROLE_instructor")
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/students").permitAll()
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/students/{studentId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/students/{studentId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/students/{studentId}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/students/teams/{teamId}").access(this.teamMembershipAuthorizationManager)

                        // Security rules for the /activities/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/activities/search").authenticated()
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/activities").authenticated()
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/activities/{activityId}").access(this.activityMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/activities/{activityId}").access(this.activityOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/activities/{activityId}").access(this.activityOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/activities/{activityId}/comments").access(this.activityMembershipAuthorizationManager)

                        // Security rules for the /evaluations/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/evaluations").authenticated()
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/evaluations/{evaluationId}").access(this.evaluationOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/evaluations/evaluators/{evaluatorId}/week/{week}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/evaluations/sections/{sectionId}/week/{week}").access(this.sectionInstructorAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/evaluations/students/{studentId}/week/{week}").access(this.userOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/evaluations/students/{studentId}/week/{week}/details").access(this.studentInstructorAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/evaluations/students/{studentId}").access(this.userOwnershipAuthorizationManager)

                        // Security rules for the /users/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/users/forget-password/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/users/reset-password/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/users/{userId}").access(this.userOwnershipAuthorizationManager)

                        // The following endpoint is used by the front-end application to check if a user exists during user registration.
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/users/exists/{email}").permitAll()

                        // Security rules for the RAM /teams/{teamId}/documents/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/documents/search").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/documents").access(this.teamOwnershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/documents/{documentId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/teams/{teamId}/documents/{documentId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/documents/{documentId}").access(this.teamOwnershipAuthorizationManager)

                        // Security rules for the RAM /teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/** endpoint.
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/lock").access(this.teamMembershipAuthorizationManager)

                        // Security rules for the RAM /teams/{teamId}/requirement-artifacts/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/requirement-artifacts/search").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/requirement-artifacts").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}").access(this.teamMembershipAuthorizationManager)

                        // Security rules for the RAM /teams/{teamId}/artifact-links/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/artifact-links/search").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/artifact-links").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/artifact-links/{artifactLinkId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/artifact-links/{artifactLinkId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/artifact-links/{artifactLinkId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}/traceability").access(this.teamMembershipAuthorizationManager)

                        // Security rules for the RAM /teams/{teamId}/use-cases/** endpoint.
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/use-cases/search").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/use-cases").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/use-cases/{useCaseId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PUT, this.baseUrl + "/teams/{teamId}/use-cases/{useCaseId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/use-cases/{useCaseId}").access(this.teamMembershipAuthorizationManager)

                        // Security rules for the RAM comment endpoint.
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/documents/{documentId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/documents/{documentId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/documents/{documentId}/document-sections/{documentSectionId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/requirement-artifacts/{artifactId}/comment-threads").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.POST, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}/comments").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.GET, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.PATCH, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}").access(this.teamMembershipAuthorizationManager)
                        .requestMatchers(HttpMethod.DELETE, this.baseUrl + "/teams/{teamId}/comment-threads/{commentThreadId}/comments/{commentId}").access(this.teamMembershipAuthorizationManager)
                        
                        .requestMatchers(this.baseUrl + "/**").authenticated() // This is the default rule for all other endpoints.

                        .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(this.customBasicAuthenticationEntryPoint))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(this.customBearerTokenAuthenticationEntryPoint)
                        .accessDeniedHandler(this.customBearerTokenAccessDeniedHandler))
                /* Configures the spring boot application as an OAuth2 Resource Server which authenticates all
                 the incoming requests (except the ones excluded above) using JWT authentication.
                 */
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(this.privateKey).build();
        JWKSource<SecurityContext> jwkSet = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSet);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(this.publicKey).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        /*
        Let’s say that that your authorization server communicates authorities in a custom claim called "authorities".
        In that case, you can configure the claim that JwtAuthenticationConverter should inspect, like so:
         */
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("authorities");

        /*
        You can also configure the authority prefix to be different as well. The default one is "SCOPE_".
        In this project, you need to change it to empty, that is, no prefix!
         */
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    /**
     * This method defines a role hierarchy where the ROLE_admin implies ROLE_instructor and ROLE_instructor implies ROLE_student.
     * For example, a user who is authenticated with ROLE_admin, will behave as if they have all three roles when security constraints are evaluated
     * against any filter- or method-based rules.
     *
     * @return
     */
    @Bean
    static RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("admin").implies("instructor")
                .role("instructor").implies("student")
                .build();
    }

}
