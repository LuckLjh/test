package com.cesgroup.api.org;

import java.util.List;

/**
 * 试验中的组织机构相关的connector.
 * 
 * @author 国栋
 *
 */
public class MockOrgConnector implements OrgConnector {

    @Override
    public int getJobLevelByUserId(String userId) {
        return 0;
    }

    @Override
    public int getJobLevelByInitiatorAndPosition(String userId, String positionName) {
        return 0;
    }

    @Override
    public String getSuperiorId(String userId) {
        return null;
    }

    @Override
    public List<String> getPositionUserIds(String userId, String positionName) {
        return null;
    }

    @Override
    public List<OrgDTO> getOrgsByUserId(String userId) {
        return null;
    }
}
