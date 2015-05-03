/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.studies.family.internal;

import org.phenotips.data.Patient;
import org.phenotips.data.internal.PhenoTipsPatient;
import org.phenotips.data.permissions.AccessLevel;
import org.phenotips.data.permissions.PatientAccess;
import org.phenotips.data.permissions.PermissionsManager;
import org.phenotips.security.authorization.AuthorizationService;
import org.phenotips.studies.family.FamilyUtils;
import org.phenotips.studies.family.Validation;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.security.authorization.Right;
import org.xwiki.users.User;
import org.xwiki.users.UserManager;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * Collection of checks for checking if certain actions are allowed.
 *
 * @version $Id$
 * @since 1.2RC1
 */
@Component
@Singleton
public class ValidationImpl implements Validation
{
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> referenceResolver;

    @Inject
    private FamilyUtils familyUtils;

    @Inject
    private PermissionsManager permissionsManager;

    @Inject
    private UserManager userManager;

    @Inject
    private AuthorizationService authorizationService;

    @Inject
    @Named("edit")
    private AccessLevel editAccess;

    /**
     * Checks if the patient is already present within the family members list.
     */
    private boolean isInFamily(XWikiDocument family, String patientId) throws XWikiException
    {
        return this.familyUtils.getFamilyMembers(family).contains(patientId);
    }

    @Override
    public StatusResponse canAddToFamily(String familyAnchor, String patientId) throws XWikiException
    {
        XWikiDocument family = this.familyUtils.getFamilyDoc(this.familyUtils.getFromDataSpace(familyAnchor));

        return canAddToFamily(family, patientId);
    }

    @Override
    public StatusResponse canAddToFamily(XWikiDocument familyDoc, String patientId)
        throws XWikiException
    {
        StatusResponse response = new StatusResponse();

        DocumentReference patientRef = this.referenceResolver.resolve(patientId, Patient.DEFAULT_DATA_SPACE);
        XWikiDocument patientDoc = this.familyUtils.getDoc(patientRef);
        if (patientDoc == null) {
            response.statusCode = 404;
            response.errorType = "invalidId";
            response.message = String.format("Could not find patient %s.", patientId);
            return response;
        }

        EntityReference patientFamilyRef = this.familyUtils.getFamilyReference(patientDoc);
        if (patientFamilyRef != null) {
            boolean hasOtherFamily;
            hasOtherFamily = familyDoc == null || patientFamilyRef.compareTo(familyDoc.getDocumentReference()) != 0;
            if (hasOtherFamily) {
                response.statusCode = 501;
                response.errorType = "familyConflict";
                response.message = String.format("Patient %s belongs to a different family.", patientId);
                return response;
            }
        }

        boolean isInFamily = safeIsInFamilyCheck(familyDoc, patientId);

        PedigreeUtils.Pedigree pedigree = PedigreeUtils.getPedigree(patientDoc);
        if ((pedigree == null || pedigree.isEmpty()) || isInFamily) {
            if (!isInFamily && familyDoc != null) {
                return this.checkFamilyAccessWithResponse(familyDoc);
            }
            StatusResponse familyResponse = new StatusResponse();
            familyResponse.statusCode = 200;
            return familyResponse;
        } else {
            response.statusCode = 501;
            response.errorType = "existingPedigree";
            response.message =
                String.format("Patient %s has an existing pedigree.", patientId);
            return response;
        }
    }

    private boolean safeIsInFamilyCheck(XWikiDocument familyDoc, String patientId) throws XWikiException
    {
        if (familyDoc != null) {
            return this.isInFamily(familyDoc, patientId);
        }
        return false;
    }

    @Override
    public StatusResponse checkFamilyAccessWithResponse(XWikiDocument familyDoc)
    {
        StatusResponse response = new StatusResponse();
        User currentUser = this.userManager.getCurrentUser();
        if (this.authorizationService.hasAccess(currentUser, Right.EDIT,
            new DocumentReference(familyDoc.getDocumentReference()))) {
            response.statusCode = 200;
            return response;
        }
        response.statusCode = 401;
        response.errorType = "permissions";
        response.message = "Insufficient permissions to edit the family record.";
        return response;
    }

    /* Should not be used when saving families. Todo why? */
    @Override
    public boolean hasPatientEditAccess(XWikiDocument patientDoc)
    {
        User currentUser = this.userManager.getCurrentUser();
        // fixme. should not instantiate the implementation of PhenoTipsPatient
        PatientAccess patientAccess = this.permissionsManager.getPatientAccess(new PhenoTipsPatient(patientDoc));
        AccessLevel patientAccessLevel = patientAccess.getAccessLevel(currentUser.getProfileDocument());
        return patientAccessLevel.compareTo(this.editAccess) >= 0;
    }
}
