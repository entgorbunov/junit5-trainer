package com.dmdev.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

class ConnectionManagerTest {

    private MockedStatic<PropertiesUtil> propertiesUtilMock;
    private MockedStatic<DriverManager> driverManagerMock;

    @BeforeEach
    void setUp() {
        propertiesUtilMock = mockStatic(PropertiesUtil.class);
        driverManagerMock = mockStatic(DriverManager.class);
    }

    @Test
    void testGetConnection() throws Exception {
        propertiesUtilMock.when(() -> PropertiesUtil.get("db.url")).thenReturn("jdbc:h2:mem:test");
        propertiesUtilMock.when(() -> PropertiesUtil.get("db.user")).thenReturn("sa");
        propertiesUtilMock.when(() -> PropertiesUtil.get("db.password")).thenReturn("");
        propertiesUtilMock.when(() -> PropertiesUtil.get("db.driver")).thenReturn("org.h2.Driver");

        Connection mockConnection = Mockito.mock(Connection.class);
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(mockConnection);

        Connection connection = ConnectionManager.get();

        assertNotNull(connection);
        driverManagerMock.verify(() -> DriverManager.getConnection("jdbc:h2:mem:test", "sa", ""), Mockito.times(1));
    }
}
