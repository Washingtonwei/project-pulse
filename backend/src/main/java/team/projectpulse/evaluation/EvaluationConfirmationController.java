package team.projectpulse.evaluation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team.projectpulse.student.Student;
import team.projectpulse.system.EmailService;
import team.projectpulse.system.UserUtils;

@RestController
@RequestMapping("${api.endpoint.base-url}/evaluations")
public class EvaluationConfirmationController {

    private final EmailService emailService;
    private final UserUtils userUtils;


    public EvaluationConfirmationController(EmailService emailService, UserUtils userUtils) {
        this.emailService = emailService;
        this.userUtils = userUtils;
    }

    @PostMapping("/weeks/{week}/receipt")
    public void sendEvaluationReceipt(@PathVariable String week) {
        Student submitter = this.userUtils.getStudent();
        String email = submitter.getEmail();
        String sectionName = submitter.getSection().getSectionName();
        this.emailService.sendPeerEvaluationConfirmationEmail(email, submitter.getFirstName() + " " + submitter.getLastName(), sectionName, week);
    }

}
