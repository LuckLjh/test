package com.cescloud.saas.archive.api.modular.archivetree.constant;

/**
 * 档案树节点常量类
 *
 * @author zhangxuehu
 */

public enum ArchiveTreeNodeEnum {
    /**
     * 树根节点
     */
    TREE_ROOT("树根节点", "T"),

    /**
     * 档案门类节点
     */
    ARCHIVE_TYPE("档案门类节点", "A"),
    /**
     * 层级节点
     */
    LAYER("层级节点", "L"),

    /**
     * 归档范围节点
     */
    FILING_SCOPE("归档范围节点", "S"),
    /**
     * 分类节点
     */
    CLAZZ("分类节点", "C"),
    /**
     * 组织机构结点
     */
    DEPT("组织机构结点", "D"),
    /**
     * 动态数据节点
     */
    DYNAMIC("动态数据节点", "Y"),
    ;

    private final String name;

    private final String code;

    private ArchiveTreeNodeEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static ArchiveTreeNodeEnum getEnum(String code) {
        for (final ArchiveTreeNodeEnum archiveTreeNodeEnum : ArchiveTreeNodeEnum.values()) {
            if (archiveTreeNodeEnum.getCode().equals(code)) {
                return archiveTreeNodeEnum;
            }
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

}
