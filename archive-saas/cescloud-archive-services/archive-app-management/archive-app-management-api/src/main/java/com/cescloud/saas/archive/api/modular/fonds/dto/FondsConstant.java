package com.cescloud.saas.archive.api.modular.fonds.dto;

import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.archivetree.dto.FondsArchiveTreeSyncTreeNode;
import com.cescloud.saas.archive.api.modular.fonds.entity.Fonds;

public class FondsConstant {

	public static final String GLOBAL_FONDS_CODE = "G";

	public static final String GLOBAL_FONDS_NAME = "全部全宗";

	public static final String FONDS_TREE_CLASS = "F";

	/**
	 * 节点类型为树 T
	 */
	public static final String ARCHIVE_TREE_CLASS = "T";

	public static final Fonds getGlobalFonds() {
		return Fonds.builder().fondsCode(GLOBAL_FONDS_CODE).fondsName(GLOBAL_FONDS_NAME).build();
	}

	public static final FondsArchiveTreeSyncTreeNode getGlobalFondsTreeNode(){
		FondsArchiveTreeSyncTreeNode treeNode = new FondsArchiveTreeSyncTreeNode();
		treeNode.setPk(FONDS_TREE_CLASS + StrUtil.DASHED + GLOBAL_FONDS_CODE);
		treeNode.setName(GLOBAL_FONDS_NAME);
		treeNode.setFondsCode(GLOBAL_FONDS_CODE);
		treeNode.setNodeClass(FONDS_TREE_CLASS);
		return treeNode;
	}
}
