package com.veloservice.config.tenant;

import com.veloservice.config.tenant.SucursalContext;
import com.veloservice.config.tenant.TallerContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantOperationAspectTest {

    @Mock private EntityManager entityManager;
    @Mock private ProceedingJoinPoint joinPoint;
    @InjectMocks private TenantOperationAspect aspect;

    @AfterEach
    void cleanup() {
        SucursalContext.clear();
        TallerContext.clear();
    }

    @Test
    void setsTallerConfigWhenTallerContextPresent() throws Throwable {
        UUID tallerId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        Query query = mock(Query.class);
        given(entityManager.createNativeQuery(contains("current_taller_id"))).willReturn(query);
        given(query.setParameter(eq(1), anyString())).willReturn(query);
        given(query.getSingleResult()).willReturn(null);
        given(joinPoint.proceed()).willReturn(null);

        aspect.applySucursalContext(joinPoint);

        verify(entityManager).createNativeQuery(contains("current_taller_id"));
        verify(query).setParameter(1, tallerId.toString());
    }

    @Test
    void setsSucursalConfigWhenOnlySucursalContextPresent() throws Throwable {
        UUID sucursalId = UUID.randomUUID();
        SucursalContext.setCurrentSucursal(sucursalId);
        Query query = mock(Query.class);
        given(entityManager.createNativeQuery(contains("current_sucursal_id"))).willReturn(query);
        given(query.setParameter(eq(1), anyString())).willReturn(query);
        given(query.getSingleResult()).willReturn(null);
        given(joinPoint.proceed()).willReturn(null);

        aspect.applySucursalContext(joinPoint);

        verify(entityManager).createNativeQuery(contains("current_sucursal_id"));
        verify(query).setParameter(1, sucursalId.toString());
    }

    @Test
    void setsBothConfigsWhenTallerAndSucursalContextsArePresent() throws Throwable {
        UUID tallerId = UUID.randomUUID();
        UUID sucursalId = UUID.randomUUID();
        TallerContext.setCurrentTaller(tallerId);
        SucursalContext.setCurrentSucursal(sucursalId);
        Query tallerQuery = mock(Query.class);
        Query sucursalQuery = mock(Query.class);
        given(entityManager.createNativeQuery(contains("current_taller_id"))).willReturn(tallerQuery);
        given(entityManager.createNativeQuery(contains("current_sucursal_id"))).willReturn(sucursalQuery);
        given(tallerQuery.setParameter(eq(1), anyString())).willReturn(tallerQuery);
        given(sucursalQuery.setParameter(eq(1), anyString())).willReturn(sucursalQuery);
        given(tallerQuery.getSingleResult()).willReturn(null);
        given(sucursalQuery.getSingleResult()).willReturn(null);
        given(joinPoint.proceed()).willReturn(null);

        aspect.applySucursalContext(joinPoint);

        verify(tallerQuery).setParameter(1, tallerId.toString());
        verify(sucursalQuery).setParameter(1, sucursalId.toString());
    }
}
