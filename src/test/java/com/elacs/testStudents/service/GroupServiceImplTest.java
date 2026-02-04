package com.elacs.testStudents.service;

import com.elacs.testStudents.model.Group;
import com.elacs.testStudents.repository.GroupRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceImplTest {
    public static final String GROUP_NUMBER = "00-00";

    @InjectMocks
    private GroupServiceImpl service;
    @Mock
    private GroupRepository repository;
    @Captor
    private ArgumentCaptor<Group> captor;
    @Captor
    private ArgumentCaptor<Long> longCaptor;

    @Test
    public void shouldDoRepositorySearch() {
        service.getGroupViewDtoPage(1, 10);

        verify(repository).findAllByOrderByAddedDateDescWithCount(any());
    }

    @Test
    public void shouldSaveWithSameData() {
        service.saveStudentGroup(GROUP_NUMBER);

        verify(repository).save(captor.capture());
        Group value = captor.getValue();
        Assert.assertEquals(GROUP_NUMBER, value.getGroupNumber());
    }

}