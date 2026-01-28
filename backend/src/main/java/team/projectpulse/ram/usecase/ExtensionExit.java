package team.projectpulse.ram.usecase;

public enum ExtensionExit {
    RESUME,      // merge back to main steps at resumeStepNo
    END_SUCCESS, // extension ends the use case successfully
    END_FAILURE  // extension ends the use case unsuccessfully
}
