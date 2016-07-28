package rowcord.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.postgresql.util.PSQLException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import rowcord.exceptions.JwtAuthException;
import rowcord.models.requests.StdRequest;
import rowcord.models.responses.StdResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jiaweizhang on 7/26/2016.
 */

@org.springframework.stereotype.Controller
class Controller {

    void pre(StdRequest stdRequest, HttpServletRequest httpServletRequest) {
        String jwt = httpServletRequest.getHeader("Authorization");
        try {
            Claims claims = Jwts.parser().setSigningKey("secret key").parseClaimsJws(jwt).getBody();
            stdRequest.userId = Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            throw new JwtAuthException();
        }
    }

    ResponseEntity wrap(StdResponse stdResponse) {
        switch (stdResponse.status) {
            case 200:
                return ResponseEntity.status(HttpStatus.OK).body(stdResponse);
            case 403:
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(stdResponse);
            case 500:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(stdResponse);
            default:
                return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(stdResponse);
        }
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
            reason = "Database error")
    @ExceptionHandler(BadSqlGrammarException.class)
    public void handleBadSqlGrammarException() {
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR,
            reason = "Database error")
    @ExceptionHandler(PSQLException.class)
    public void handlePSQLException() {
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException() {
        return wrap(new StdResponse(500, "Internal server error"));
    }
}
