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
'use strict';

var KISBPM = KISBPM || {};
KISBPM.TOOLBAR_CONFIG = {
    "items" : [
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.SAVE",
            "name" : "保存",
            "cssClass" : "editoricon editor-icon editor-icon-save",
            "action" : "KISBPM.TOOLBAR.ACTIONS.saveModel"
        },
        {
            "type" : "button",
            "title" : "导入",
            "name" : "导入",
            "cssClass" : "editoricon iconfont icon-daoru",
            "action" : "KISBPM.TOOLBAR.ACTIONS.importXmlForm"
        },
        {
            "type" : "button",
            "title" : "导出",
            "name" : "导出",
            "cssClass" : "editoricon iconfont icon-daochu",
            "action" : "KISBPM.TOOLBAR.ACTIONS.exportXmlForm"
        },
        {
            "type" : "separator",
            "title" : "",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.CUT",
            "name" : "剪切",
            "cssClass" : "editoricon editor-icon editor-icon-cut",
            "action" : "KISBPM.TOOLBAR.ACTIONS.cut",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.COPY",
            "name" : "拷贝",
            "cssClass" : "editoricon editor-icon editor-icon-copy",
            "action" : "KISBPM.TOOLBAR.ACTIONS.copy",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.PASTE",
            "name" : "粘贴",
            "cssClass" : "editoricon editor-icon editor-icon-paste",
            "action" : "KISBPM.TOOLBAR.ACTIONS.paste",
            "enabled" : false
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.DELETE",
            "name" : "删除",
            "cssClass" : "editoricon editor-icon editor-icon-delete",
            "action" : "KISBPM.TOOLBAR.ACTIONS.deleteItem",
            "enabled" : false,
            "enabledAction" : "element"
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.REDO",
            "name" : "重做",
            "cssClass" : "editoricon editor-icon editor-icon-redo",
            "action" : "KISBPM.TOOLBAR.ACTIONS.redo",
            "enabled" : false
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.UNDO",
            "name" : "撤销",
            "cssClass" : "editoricon editor-icon editor-icon-undo",
            "action" : "KISBPM.TOOLBAR.ACTIONS.undo",
            "enabled" : false
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ALIGNVERTICAL",
            "name" : "纵向对齐",
            "cssClass" : "editoricon editor-icon editor-icon-align-vertical",
            "action" : "KISBPM.TOOLBAR.ACTIONS.alignVertical",
            "enabled" : false,
            "enabledAction" : "element",
            "minSelectionCount" : 2
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ALIGNHORIZONTAL",
            "name" : "水平对齐",
            "cssClass" : "editoricon editor-icon editor-icon-align-horizontal",
            "action" : "KISBPM.TOOLBAR.ACTIONS.alignHorizontal",
            "enabledAction" : "element",
            "enabled" : false,
            "minSelectionCount" : 2
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.SAMESIZE",
            "name" : "相同大小",
            "cssClass" : "editoricon editor-icon editor-icon-same-size",
            "action" : "KISBPM.TOOLBAR.ACTIONS.sameSize",
            "enabledAction" : "element",
            "enabled" : false,
            "minSelectionCount" : 2
        },
        {
        	"type" : "separator",
        	"title" : "TOOLBAR.ACTION.SAVE",
        	"cssClass" : "toolbar-separator"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMIN",
            "name" : "放大",
            "cssClass" : "editoricon editor-icon editor-icon-zoom-in",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomIn"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMOUT",
            "name" : "缩小",
            "cssClass" : "editoricon editor-icon editor-icon-zoom-out",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomOut"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMACTUAL",
            "name" : "实际大小",
            "cssClass" : "editoricon editor-icon editor-icon-zoom-actual",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomActual"
        },
        {
            "type" : "button",
            "title" : "TOOLBAR.ACTION.ZOOMFIT",
            "name" : "窗口自适应",
            "cssClass" : "editoricon editor-icon editor-icon-zoom-fit",
            "action" : "KISBPM.TOOLBAR.ACTIONS.zoomFit"
        },
        {
            "type" : "separator",
            "title" : "TOOLBAR.ACTION.SAVE",
            "cssClass" : "toolbar-separator"
        },
    	{
            "type" : "button",
            "title" : "TOOLBAR.ACTION.BENDPOINT.ADD",
            "name" : "增加折点",
            "cssClass" : "editoricon editor-icon editor-icon-bendpoint-add",
            "action" : "KISBPM.TOOLBAR.ACTIONS.addBendPoint",
            "id" : "add-bendpoint-button"
    	},
    	{
    	    "type" : "button",
    	    "title" : "TOOLBAR.ACTION.BENDPOINT.REMOVE",
    	    "name" : "删除折点",
    	    "cssClass" : "editoricon editor-icon editor-icon-bendpoint-remove",
    	    "action" : "KISBPM.TOOLBAR.ACTIONS.removeBendPoint",
    	    "id" : "remove-bendpoint-button"
    	}
    ],
    
    "secondaryItems" : [
		{
		    "type" : "button",
		    "title" : "Close",
		    "name" : "关闭",
		    "cssClass" : "editoricon editor-icon editor-icon-close",
		    "action" : "KISBPM.TOOLBAR.ACTIONS.closeEditor"
		}
    ]
};