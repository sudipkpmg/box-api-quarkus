package gov.tn.dhs.quarkus.box;

import gov.tn.dhs.quarkus.box.exception.ServiceError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class ClientErrorHandler implements ExceptionMapper<ServiceError> {
    @Override
    public Response toResponse(ServiceError serviceError) {
        return Response.status(serviceError.getCode()).entity(serviceError.getMessage()).build();
    }
}
