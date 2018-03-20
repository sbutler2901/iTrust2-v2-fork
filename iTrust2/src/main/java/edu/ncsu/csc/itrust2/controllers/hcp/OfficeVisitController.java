package edu.ncsu.csc.itrust2.controllers.hcp;
import java.text.ParseException;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import edu.ncsu.csc.itrust2.forms.hcp.OfficeVisitForm;
import edu.ncsu.csc.itrust2.models.enums.TransactionType;
import edu.ncsu.csc.itrust2.models.persistent.Hospital;
import edu.ncsu.csc.itrust2.models.persistent.OfficeVisit;
import edu.ncsu.csc.itrust2.models.persistent.User;
import edu.ncsu.csc.itrust2.utils.LoggerUtil;
/**
 *
 * Controller that enables a HCP to document an office visit into the system
 *
 * @author Kai Presler-Marshall
 *
 */
@Controller
public class OfficeVisitController {
    /**
     * Returns the form page for a HCP to document an OfficeVisit
     *
     * @param model
     *            The data for the front end
     * @return Page to display to the user
     */
    @GetMapping ( "/hcp/documentOfficeVisit" )
    @PreAuthorize ( "hasRole('ROLE_HCP')" )
    public String documentOfficeVisit ( final Model model ) {
        model.addAttribute( "sampletext", new OfficeVisitForm() );
        model.addAttribute( "sampletext", Hospital.getHospitals() );
        model.addAttribute( "sampletext", User.getPatients() );
        return "/hcp/documentOfficeVisit";
    }
    /**
     * Processes the form page for a HCP to document an OfficeVisit
     *
     * @param form
     *            OfficeVisitForm to save
     * @param result
     *            Validation result
     * @param model
     *            Data from the front end
     * @return Page to display for the user
     */
    @PostMapping ( "/hcp/documentOfficeVisit" )
    @PreAuthorize ( "hasRole('ROLE_HCP')" )
    public String documentVisitSubmit ( @Valid @ModelAttribute ( "OfficeVisitForm" ) final OfficeVisitForm form,
            final BindingResult result, final Model model ) {
        form.setHcp( SecurityContextHolder.getContext().getAuthentication().getName() );
        OfficeVisit req = null;
        try {
            req = new OfficeVisit( form );
        }
        catch ( final ParseException e ) {
            result.rejectValue( "date", "date.notvalid", "Expected format: MM/DD/YYYY" );
            result.rejectValue( "sampletext", "time.notvalid", "Expected format: hh:mm aaaa" );
        }
        catch ( final IllegalArgumentException e ) {
            result.rejectValue( "sampletext", "preScheduled.notvalid",
                    "sampletext" );
        }
        if ( result.hasErrors() ) {
            model.addAttribute( "sampletext", form );
            model.addAttribute( "patients", User.getPatients() );
            model.addAttribute( "sampletext", Hospital.getHospitals() );
            return "hcp/documentOfficeVisit";
        }
        else {
            req.save();
            LoggerUtil.log( TransactionType.OFFICE_VISIT_CREATE, form.getHcp(), form.getPatient(),
                    form.getHcp() + "sampletext" + form.getPatient() );
            return "hcp/documentOfficeVisitResult";
        }
    }
    /**
     * Returns the form page for a HCP to edit an OfficeVisit
     *
     * @param model
     *            The data for the front end
     * @return Page to display to the user
     */
    @GetMapping ( "/hcp/editOfficeVisit" )
    @PreAuthorize ( "hasRole('ROLE_HCP')" )
    public String getAllOfficeVisits ( final Model model ) {
        model.addAttribute( "sampletext", new OfficeVisitForm() );
        model.addAttribute( "sampletext", Hospital.getHospitals() );
        model.addAttribute( "sampletext", User.getPatients() );
        model.addAttribute( "visits", OfficeVisit.getOfficeVisits() );
        return "/hcp/editOfficeVisit";
    }
}
