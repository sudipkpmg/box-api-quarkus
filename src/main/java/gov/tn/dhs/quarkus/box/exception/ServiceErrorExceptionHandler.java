package gov.tn.dhs.quarkus.box.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ServiceErrorExceptionHandler implements ExceptionMapper<ServiceError> {

    @Override
    public Response toResponse(ServiceError serviceError) {
        return Response.status(serviceError.getCode()).entity(serviceError.toJson()).build();
    }

}
