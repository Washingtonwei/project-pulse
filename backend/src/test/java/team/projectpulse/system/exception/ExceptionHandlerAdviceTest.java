package team.projectpulse.system.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import team.projectpulse.system.Result;
import team.projectpulse.system.StatusCode;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class ExceptionHandlerAdviceTest {

    private final ExceptionHandlerAdvice advice = new ExceptionHandlerAdvice();

    @Test
    void testUnhandledExceptionIsLoggedWithItsStackTrace(CapturedOutput output) {
        advice.handleOtherException(new IllegalStateException("could not execute statement [insert into users]"));

        assertThat(output).contains("Unhandled exception")
                .contains("java.lang.IllegalStateException: could not execute statement [insert into users]")
                .contains("at team.projectpulse.system.exception.ExceptionHandlerAdviceTest");
    }

    @Test
    void testUnhandledExceptionMessageIsNotReturnedToTheCaller() {
        Result result = advice.handleOtherException(new IllegalStateException("could not execute statement [insert into users]"));

        assertThat(result.isFlag()).isFalse();
        assertThat(result.getCode()).isEqualTo(StatusCode.INTERNAL_SERVER_ERROR);
        assertThat(result.getMessage()).isEqualTo("A server internal error occurs.");
        assertThat(result.getData()).isNull();
    }
}
