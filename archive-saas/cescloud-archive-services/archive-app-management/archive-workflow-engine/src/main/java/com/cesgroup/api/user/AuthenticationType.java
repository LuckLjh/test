package com.cesgroup.api.user;

import com.cesgroup.core.util.WorkflowConstants;

/**
 * 
 * @author 国栋
 *
 */
public class AuthenticationType {

    // normal
    /** normal */
    public static final String NORMAL = WorkflowConstants.HumanTaskConstants.CATALOG_NORMAL;

    /** normal-otp */
    public static final String NORMAL_OTP = "normal-otp";

    /** special-or-normal */
    public static final String SPECIAL_OR_NORMAL = "special-or-normal";

    // special
    /** special */
    public static final String SPECIAL = "special";

    // ldap
    /** ldap */
    public static final String LDAP = "ldap";

    /** ldap-otp */
    public static final String LDAP_OTP = "ldap-otp";

    /** special-or-ldap */
    public static final String SPECIAL_OR_LDAP = "special-or-ldap";
}
