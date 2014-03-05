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

package org.phenotips.data.push;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * API that allows pushing patient data to a remote PhenoTips instance.
 *
 * @version $Id$
 * @since 1.0M11
 */
@Unstable
@Role
public interface PushServerResponse
{
    /**
     * Returns the status of the requested action.
     *
     * @return {@code true} iff:
     *   1. pushes from this server are accepted by the remote server, and the server token is valid
     *   2. credentials provided for the given remote user were accepted by the remote server
     *   3. the operation requested was successfully performed
     * {@code false} in all other cases, in which case one and only one of {@code isUnauthorizedServer()},
     * {@code isUnknownAction()}, {@code isLoginFailed()} or {@code isActionFailed()} will return true.
     */
    boolean isSuccessful();


    boolean isLoginFailed();     // any problems related to authenticating the user on the remote server.
                                 // server token may be incorrect, or the password may be wrong, etc.

    boolean isActionFailed();    // any problems executing the action requested after the user was successfully authenticated

    boolean isIncorrectProtocolVersion();    // if the version of the POST protocol used is not supported by the server


    /**
     * @return {@code true} iff the failure reason is known.<br>
     * {@code false} may indicate an unknown problem on the server side.
     */
    boolean isLoginFailed_knownReason();
    /**
     * @return {@code true} iff remote server token configured on this server does not match the token configured on the remote server for this server
     * (including the case when remote server does not have a token for this server at all)
     */
    boolean isLoginFailed_UnauthorizedServer();
    /**
     * @return {@code true} iff the user name was a not a valid user on the remote server, or
     * either the password or the token were not correct for the user provided.
     */
    boolean isLoginFailed_IncorrectCredentials();
    /**
     * @return {@code true} iff the user token porovided is expired. May only be true if user token was supplied in the POST request.
     */
    boolean isLoginFailed_UserTokenExpired();
    /**
     * @return {@code true} iff the user token are not accepted by the remote server (possibly after a config change).
     * May only be true if user token was supplied in the POST request.
     */
    boolean isLoginFailed_TokensNotSuported();


    /**
     * @return {@code true} iff the failure reason is known.<br>
     * {@code false} may indicate an unknown problem on the server side.
     */
    boolean isActionFailed_knownReason();
    /**
     * @return {@code true} iff the action requested is not supported by the server
     */
    boolean isActionFailed_isUnknownAction();
}