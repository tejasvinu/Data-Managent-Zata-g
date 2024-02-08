package org.zatag.dev.datamanagement;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.zatag.dev.datamanagement.Controller.MySql.MySqlDataController;
import org.zatag.dev.datamanagement.Models.MySql.MySQLLink;
import org.zatag.dev.datamanagement.Repository.MySql.MySQLLinkRepository;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Mockito.*;

class MySqlDataControllerTest {

    @InjectMocks
    MySqlDataController mySqlDataController;

    @Mock
    MySQLLinkRepository mySQLLinkRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void shouldExtractMySQLTableWhenIdExists() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.of(new MySQLLink()));
        mySqlDataController.extractMySQLTable("1");
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldNotExtractMySQLTableWhenIdDoesNotExist() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.empty());
        mySqlDataController.extractMySQLTable("1");
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldDeleteMySQLLinkWhenIdExists() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.of(new MySQLLink()));
        mySqlDataController.deleteMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldNotDeleteMySQLLinkWhenIdDoesNotExist() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.empty());
        mySqlDataController.deleteMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldUpdateMySQLLinkWhenIdExists() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.of(new MySQLLink()));
        mySqlDataController.updateMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldNotUpdateMySQLLinkWhenIdDoesNotExist() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.empty());
        mySqlDataController.updateMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldInsertMySQLLinkWhenIdExists() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.of(new MySQLLink()));
        mySqlDataController.insertMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }

    @Test
    void shouldNotInsertMySQLLinkWhenIdDoesNotExist() {
        when(mySQLLinkRepository.findById(anyString())).thenReturn(Optional.empty());
        mySqlDataController.insertMySQLLink("1", new HashMap<>());
        verify(mySQLLinkRepository, times(1)).findById(anyString());
    }
}
