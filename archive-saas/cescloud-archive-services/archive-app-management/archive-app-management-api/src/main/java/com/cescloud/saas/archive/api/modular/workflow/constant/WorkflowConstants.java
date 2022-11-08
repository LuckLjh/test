/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.api.modular.workflow.constant</p>
 * <p>文件名:WorkflowConstants.java</p>
 * <p>创建时间:2019年12月3日 下午3:35:19</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.api.modular.workflow.constant;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年12月3日
 */
public final class WorkflowConstants {

    public final static class BusinessType {

        // 业务主表
        public final static String MATSER = "master";

        // 业务明细
        public final static String DETAIL = "detail";

        // 业务附件
        public final static String DOCUMENT = "document";
    }

    public final static class ObjectType {

        // 部门
        public final static String DEPT = "d";

        // 用户
        public final static String USER = "u";

        // 角色
        public final static String ROLE = "r";
    }

    public final static class ErrorMsg {

        // 流程未启用信息
        public final static String PROCESS_DISABLE_MSG = "业务编码[%s]未启用流程";
    }

    /** 任务常量 */
    public final static class HumanTaskConstants {

        /** 开始（开始节点）. */
        public final static String CATALOG_START = "start";

        /** 发起（开始节点后面的第一个节点）. */
        public final static String CATALOG_SPONSOR = "sponsor";

        /** 普通状态. */
        public final static String CATALOG_NORMAL = "normal";

        /** 结束（结束节点）. */
        public final static String CATALOG_END = "end";

        /** 沟通. */
        public final static String CATALOG_COMMUNICATE = "communicate";

        /** 工作流状态：激活状态 */
        public final static String STATUS_ACTIVE = "active";

        /** 工作流状态：完成状态 */
        public final static String STATUS_COMPLETE = "complete";

        /** 工作流状态： 撤回 */
        public final static String STATUS_WITHDRAW = "withdraw";

        /** 工作流状态： 退回 */
        public final static String STATUS_ROLLBACK = "rollback";

        /** 工作流状态： 终止 */
        public final static String STATUS_TERMINATE = "terminate";

        /** 工作流状态：删除 */
        public final static String STATUS_DELETE = "delete";

        /** 工作流执行动作： 自动提交 */
        public final static String ACTION_AUTO = "auto";

        /** 工作流执行动作： 完成任务 */
        public final static String ACTION_COMPLETE = "complete";

        /** 工作流执行动作： 沟通 */
        public final static String ACTION_COMMUNICATE = "communicate";

        /** 工作流执行动作： 抄送 */
        public final static String ACTION_COPY = "copy";

        /** 工作流执行动作： 撤回 */
        public final static String ACTION_WITHDRAW = "withdraw";

        /** 工作流执行动作： 签收 */
        public final static String ACTION_CLAIM = "claim";

        /** 工作流执行动作： 退回 */
        public final static String ACTION_ROLLBACK = "rollback";

        /** 工作流执行动作： 转办 */
        public final static String ACTION_TRANSFER = "transfer";

        /** 工作流执行动作： 跳过 */
        public final static String ACTION_SKIP = "skip";

        /** 工作流执行动作： 跳转 */
        public final static String ACTION_JUMP = "jump";

        /** 工作流执行动作： 终止 */
        public final static String ACTION_TERMINATE = "terminate";

        /** 工作流执行动作： 减签 */
        public final static String ACTION_REMOVE_VOTE = "removeVote";

    }

}
