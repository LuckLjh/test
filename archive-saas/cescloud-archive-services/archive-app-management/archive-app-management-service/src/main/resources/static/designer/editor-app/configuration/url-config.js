/*
 * Activiti Modeler component part of the Activiti project
 * Copyright 2005-2014 Alfresco Software, Ltd. All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
var KISBPM = KISBPM || {};

KISBPM.URL = {

    getModel: function(modelId,fileName) {
        return "../workflow-version/model?modelId=" + modelId + "&fileName=" + fileName + "&access_token=" + token + "&__t=" + new Date().getTime();
    },

    getStencilSet: function() {
        return "stencilset.json?__t=" + Date.now();
    },

    putModel: function(modelId,bpmModelId) {
    	return "../workflow-version/model?modelId=" + modelId + "&access_token=" + token;
    },
    exportModelXml:function() {
    	return "../workflow-version/model/export?modelId=" + modelId + "&access_token=" + token;
    },
    
    checkUnique: function(workflowId,version) {
    	return "../workflow-version/checkUnique.json?id=&Q_EQ_version="+version+"&Q_EQ_workflowId="+workflowId+"&___t="+new Date().getTime();
    },
    
    putNewModel: function(modelId,bpmModelId) {
    	return "../workflow-version/saveNewModel.json?modelId=" + modelId;
    }
};