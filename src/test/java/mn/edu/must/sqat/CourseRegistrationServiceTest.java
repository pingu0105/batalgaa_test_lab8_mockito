package mn.edu.must.sqat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() { service = new CourseRegistrationService(repository, emailSender); }

    // A1 
    @Test
    void register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1"))
            .thenReturn(new Student("S1", "s1@must.edu.mn", true, List.of("CS101")));
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        assertEquals("OK", service.register("S1", "CS201"));
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), anyString());
    }

    // A2 
    @Test
    void register_unknownStudent_returnsErrorNoStudentAndSavesNothing() {
        when(repository.findStudent("X")).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register("X", "CS201"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3 
    @Test
    void register_inactiveStudent_returnsErrorInactiveAndNoEmail() {
        when(repository.findStudent("S2"))
            .thenReturn(new Student("S2", "s2@must.edu.mn", false, List.of()));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register("S2", "CS201"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    // A4 
    @Test
    void register_missingPrerequisite_returnsErrorPrerequisitesAndSavesNothing() {
        when(repository.findStudent("S1"))
            .thenReturn(new Student("S1", "s1@must.edu.mn", true, List.of("CS101")));
        when(repository.courseExists("CS301")).thenReturn(true);
        when(repository.prerequisites("CS301")).thenReturn(List.of("CS101", "CS201"));

        assertEquals("ERROR_PREREQUISITES", service.register("S1", "CS301"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5 
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent("S1"))
            .thenReturn(new Student("S1", "s1@must.edu.mn", true, List.of()));
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of());
        doThrow(new RuntimeException("SMTP down"))
            .when(emailSender).send(anyString(), anyString(), anyString());

        assertEquals("OK", service.register("S1", "CS101"));
        verify(repository).saveRegistration("S1", "CS101");
    }

    // A6 
    @Test
    void register_nullInput_throwsAndTouchesNothing() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS101"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));
        verifyNoInteractions(repository, emailSender);
    }

    // A7 
    @Test
    void register_success_emailBodyContainsCourseCode() {
        when(repository.findStudent("S1"))
            .thenReturn(new Student("S1", "s1@must.edu.mn", true, List.of()));
        when(repository.courseExists("CS101")).thenReturn(true);
        when(repository.prerequisites("CS101")).thenReturn(List.of());

        service.register("S1", "CS101");

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("s1@must.edu.mn"), anyString(), body.capture());
        assertTrue(body.getValue().contains("CS101"));
    }

    // A8 
    @ParameterizedTest(name = "[{index}] {3}")
    @CsvSource({
        "false, true,  true,  ERROR_NO_STUDENT",
        "true,  false, true,  ERROR_INACTIVE_STUDENT",
        "true,  true,  false, ERROR_NO_COURSE"
    })
    void register_invalidState_returnsMatchingError(boolean exists, boolean active,
                                                    boolean courseExists, String expected) {
        Student s = exists ? new Student("S1", "s1@must.edu.mn", active, List.of()) : null;
        when(repository.findStudent("S1")).thenReturn(s);
        lenient().when(repository.courseExists("CS101")).thenReturn(courseExists);

        assertEquals(expected, service.register("S1", "CS101"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}