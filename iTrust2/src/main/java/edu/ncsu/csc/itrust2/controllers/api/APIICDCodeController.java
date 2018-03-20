package edu.ncsu.csc.itrust2.controllers.api;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import edu.ncsu.csc.itrust2.forms.admin.ICDCodeForm;
import edu.ncsu.csc.itrust2.models.enums.TransactionType;
import edu.ncsu.csc.itrust2.models.persistent.ICDCode;
import edu.ncsu.csc.itrust2.models.persistent.User;
import edu.ncsu.csc.itrust2.utils.LoggerUtil;
/**
 * Class that provides the REST endpoints for handling ICD Codes. They can be
 * retrieved individually based on id, or all in a list. An Admin can add,
 * remove, or edit them.
 *
 * @author Thomas
 *
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIICDCodeController extends APIController {
    /**
     * Returns a list of Codes in the system
     *
     * @return All the codes in the system
     */
    @GetMapping ( BASE_PATH + "/icdcodes" )
    public List<ICDCode> getCodes () {
        LoggerUtil.log( TransactionType.ICD_VIEW_ALL, LoggerUtil.currentUser(), "sampletext" );
        return ICDCode.getAll();
    }
    /**
     * Returns the code with the given ID
     *
     * @param id
     *            The ID of the code to retrieve
     * @return The requested Code
     */
    @GetMapping ( BASE_PATH + "/icdcode/{id}" )
    public ResponseEntity getCode ( @PathVariable ( "id" ) final Long id ) {
        try {
            final ICDCode code = ICDCode.getById( id );
            if ( code == null ) {
                return new ResponseEntity( errorResponse( "sampletext" + id ), HttpStatus.NOT_FOUND );
            }
            LoggerUtil.log( TransactionType.ICD_VIEW, LoggerUtil.currentUser(), "sampletext" + id );
            return new ResponseEntity( code, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "sampletext" + id + "sampletext" + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }
    /**
     * Updates the code with the specified ID to the value supplied.
     *
     * @param id
     *            The ID of the code to edit
     * @param form
     *            The new values for the Code
     * @return The Response of the action
     */
    @PutMapping ( BASE_PATH + "/icdcode/{id}" )
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    public ResponseEntity updateCode ( @PathVariable ( "id" ) final Long id, @RequestBody final ICDCodeForm form ) {
        try {
            final ICDCode code = ICDCode.getById( id );
            if ( code != null ) {
                return new ResponseEntity( "sampletext" + id, HttpStatus.NOT_FOUND );
            }
            form.setId( id );
            final ICDCode updatedCode = new ICDCode( form );
            updatedCode.save();
            User user = null;
            try {
                user = User.getByName( SecurityContextHolder.getContext().getAuthentication().getName() );
            }
            catch ( final Exception e ) {
                // ignore, its was a test that wasn't authenticated properly.
            }
            LoggerUtil.log( TransactionType.ICD_EDIT, user.getUsername(), user.getUsername() + "sampletext" );
            return new ResponseEntity( updatedCode, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            return new ResponseEntity(
                    errorResponse( "sampletext" + id + "sampletext" + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }
    /**
     * Adds a new code to the system
     *
     * @param form
     *            The data for the new Code
     * @return The result of the action
     */
    @PostMapping ( BASE_PATH + "/icdcodes" )
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    public ResponseEntity addCode ( @RequestBody final ICDCodeForm form ) {
        try {
            final ICDCode code = new ICDCode( form );
            code.save();
            User user = null;
            try {
                user = User.getByName( SecurityContextHolder.getContext().getAuthentication().getName() );
            }
            catch ( final Exception e ) {
                // ignore, its was a test that wasn't authenticated properly.
            }
            LoggerUtil.log( TransactionType.ICD_CREATE, user.getUsername(),
                    user.getUsername() + "sampletext" );
            return new ResponseEntity( code, HttpStatus.OK );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
            return new ResponseEntity(
                    errorResponse( "sampletext" + form.getCode() + "sampletext" + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }
    /**
     * Deletes a code from the system.
     *
     * @param id
     *            The ID of the code to delete
     * @return The result of the action.
     */
    @DeleteMapping ( BASE_PATH + "/icdcode/{id}" )
    @PreAuthorize ( "hasRole('ROLE_ADMIN')" )
    public ResponseEntity deleteCode ( @PathVariable ( "id" ) final Long id ) {
        try {
            final ICDCode code = ICDCode.getById( id );
            code.delete();
            User user = null;
            try {
                user = User.getByName( SecurityContextHolder.getContext().getAuthentication().getName() );
            }
            catch ( final Exception e ) {
                // ignore, its was a test that wasn't authenticated properly.
            }
            LoggerUtil.log( TransactionType.ICD_DELETE, LoggerUtil.currentUser(),
                    user.getUsername() + "sampletext" );
            return new ResponseEntity( HttpStatus.OK );
        }
        catch ( final Exception e ) {
            e.printStackTrace();
            return new ResponseEntity(
                    errorResponse( "sampletext" + id + "sampletext" + e.getMessage() ),
                    HttpStatus.BAD_REQUEST );
        }
    }
}
