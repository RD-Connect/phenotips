/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.phenotips.data.events;

import org.phenotips.data.Patient;

import org.xwiki.observation.event.Event;
import org.xwiki.stability.Unstable;
import org.xwiki.users.User;

/**
 * Represents all events affecting whole patient records: creation, modification, deletion.
 *
 * @version $Id$
 * @since 1.0RC1
 */
@Unstable
public interface PatientEvent extends Event
{
    /**
     * Identifies the type of action performed on the patient record.
     *
     * @return a short string, e.g. {@code patientRecordCreated}
     */
    String getEventType();

    /**
     * The affected patient record.
     *
     * @return the new version of the patient record for events notifying of a change, or the last version if this is a
     *         deletion event
     */
    Patient getPatient();

    /**
     * The user performing this action.
     *
     * @return a user reference, may be a real user, the special "system" user, or an anonymous user
     */
    User getAuthor();
}
