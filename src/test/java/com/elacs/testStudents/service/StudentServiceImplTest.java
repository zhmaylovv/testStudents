package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Student;
import com.elacs.testStudents.repository.StudentRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class StudentServiceImplTest {
    public static final Long ID = 1L;
    private static final Long STUDENT_ID = 2L;
    private static final String NAME = "Иванов Иван Иванович";

    @InjectMocks
    private StudentServiceImpl service;
    @Mock
    private StudentRepository repository;
    @Captor
    private ArgumentCaptor<Student> captor;
    @Captor
    private ArgumentCaptor<Long> longCaptor;


    @Test
    public void shouldDoRepositorySearchBySameId() {
        service.findAllStudentsByGroupId(ID);

        Mockito.verify(repository).findByStudentGroupIdOrderByFullNameAsc(ID);
    }

    @Test
    public void shouldSendSameDataToRepositoryWhenSave() {
        service.saveStudentAndRefresh(NAME, ID);

        verify(repository).save(captor.capture());
        Student value = captor.getValue();
        Assert.assertEquals(NAME, value.getFullName());
        Assert.assertEquals(ID, value.getStudentGroupId());
    }

    @Test
    public void shouldCallFindMethodWhenSave() {
        service.saveStudentAndRefresh(NAME, ID);

        Mockito.verify(repository).findByStudentGroupIdOrderByFullNameAsc(ID);
    }

    @Test
    public void shouldSendSameDataToRepositoryWhenDelete() {
        service.deleteStudentAndRefresh(STUDENT_ID, ID);

        verify(repository).deleteById(longCaptor.capture());
        Long value = longCaptor.getValue();
        Assert.assertEquals(STUDENT_ID, value);
    }

    @Test
    public void shouldCallFindMethodWhenDelete() {
        service.deleteStudentAndRefresh(STUDENT_ID, ID);

        Mockito.verify(repository).findByStudentGroupIdOrderByFullNameAsc(ID);
    }
}