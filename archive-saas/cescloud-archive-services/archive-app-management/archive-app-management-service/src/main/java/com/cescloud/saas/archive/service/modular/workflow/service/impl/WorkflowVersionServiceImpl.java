/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cescloud.saas.archive.service.modular.workflow.service.impl</p>
 * <p>文件名:WorkflowVersionServiceImpl.java</p>
 * <p>创建时间:2019年10月17日 下午1:45:21</p>
 * <p>作者:qiucs</p>
 */

package com.cescloud.saas.archive.service.modular.workflow.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.cescloud.saas.archive.api.modular.dept.entity.SysDept;
import com.cescloud.saas.archive.api.modular.dept.feign.RemoteDeptService;
import com.cescloud.saas.archive.api.modular.role.entity.SysRole;
import com.cescloud.saas.archive.api.modular.role.feign.RemoteRoleService;
import com.cescloud.saas.archive.api.modular.user.entity.SysUser;
import com.cescloud.saas.archive.api.modular.user.feign.RemoteUserService;
import com.cescloud.saas.archive.common.constants.ModelTypeEnum;
import com.cescloud.saas.archive.common.constants.RoleConstant;
import com.cescloud.saas.archive.common.constants.business.FilingFieldConstants;
import com.cescloud.saas.archive.common.search.Page;
import com.cescloud.saas.archive.service.modular.common.core.constant.CommonConstants;
import com.cescloud.saas.archive.service.modular.common.core.util.R;
import com.cescloud.saas.archive.service.modular.common.security.exception.ArchiveRuntimeException;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowOpenApiService;
import com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService;
import com.cesgroup.bpm.cmd.SyncProcessCmd;
import com.cesgroup.bpm.persistence.domain.BpmConfBase;
import com.cesgroup.bpm.persistence.domain.BpmModelEntity;
import com.cesgroup.bpm.persistence.manager.BpmConfBaseManager;
import com.cesgroup.bpm.persistence.manager.BpmModelManager;
import com.cesgroup.bpm.proxy.CesProcessEngine;
import com.cesgroup.core.mapper.JsonMapper;
import com.cesgroup.core.query.PropertyFilter;
import com.cesgroup.core.util.WorkflowConstants;
import com.cesgroup.workflow.json.converter.BpmnJsonConverter;
import com.cesgroup.workflow.persistence.domain.BusinessMetadata;
import com.cesgroup.workflow.persistence.manager.BusinessMetadataManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.xml.util.PrettyXMLEventWriter;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author qiucs
 * @version 1.0.0 2019年10月17日
 */
@Component
@Slf4j
public class WorkflowVersionServiceImpl implements WorkflowVersionService {

    @Autowired
    private BpmModelManager bpmModelManager;

    @Autowired
    private BpmConfBaseManager bpmConfBaseManager;

    @Autowired
    private BusinessMetadataManager businessMetadataManager;

    @Autowired
    private CesProcessEngine processEngine;

    @Autowired
    @Lazy
    private RemoteDeptService remoteDeptService;

    @Autowired
    @Lazy
    private RemoteUserService remoteUserService;

    @Autowired
    @Lazy
    private RemoteRoleService remoteRoleService;

    @Autowired
	private WorkflowOpenApiService workflowOpenApiService;

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getBpmConfBaseListByBpmModelId(java.lang.Long)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<BpmConfBase> getBpmConfBaseListByBpmModelId(Long bpmModelId) {
        Assert.notNull(bpmModelId, "bpmModelId不能为空");
        final List<PropertyFilter> propertyFilters = new ArrayList<PropertyFilter>();
        propertyFilters.add(new PropertyFilter("EQL_bpmModel.id", bpmModelId.toString()));
        return bpmConfBaseManager.find("from BpmConfBase t where t.bpmModel = ?0 order by t.createTime desc",
            new BpmModelEntity(bpmModelId));
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getBpmConfBasePageByBpmModelId(
     *      com.cescloud.saas.archive.common.search.Page,
     *      java.lang.Long)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Page<?> getBpmConfBasePageByBpmModelId(Page<?> page, Long bpmModelId) {
        final com.cesgroup.core.page.Page pagedQuery = bpmConfBaseManager.pagedQuery(
            "from BpmConfBase t where t.bpmModel = ?0 order by t.createTime desc",
            (int) page.getCurrent(), (int) page.getSize(), new BpmModelEntity(bpmModelId));
        page.setRecords((List) pagedQuery.getResult());
        page.setTotal(pagedQuery.getTotalCount());
        return page;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getBpmConfBaseById(java.lang.Long)
     */
    @Override
    public BpmConfBase getBpmConfBaseById(Long id) {
        return bpmConfBaseManager.get(id);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#saveBpmConfBase(
     *      com.cesgroup.bpm.persistence.domain.BpmConfBase,
     *      java.lang.Long, java.lang.Integer)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BpmConfBase saveBpmConfBase(BpmConfBase bpmConfBase, Long bpmModelId) {
        Assert.notNull(bpmModelId, "bpmModelId不能为空");
        final BpmModelEntity bpmModel = bpmModelManager.get(bpmModelId);
        Assert.notNull(bpmModel, "bpmModelId对应的模型不存在");
        bpmConfBase.setBpmModel(bpmModel);
        bpmConfBase.setName(bpmModel.getName());
        BpmConfBase entity = null;
        final boolean isCreate = null == bpmConfBase.getId();
        if (isCreate) {
            bpmConfBase.setCreateTime(new Date());
            bpmConfBase.setUpdateTime(new Date());
            bpmConfBase.setVersion(generateVersion(bpmModelId));
            Model model = null;
            final RepositoryService repositoryService = processEngine.getRepositoryService();
            if (StrUtil.isNotBlank(bpmConfBase.getModelId())) {
                model = repositoryService.getModel(bpmConfBase.getModelId());
            }
            Assert.isTrue(checkBpmConfBase(bpmConfBase), "版本号重复");
            if (null == model) {
                // 新增版本，先保存一下，确保有ID，这样可以与model关联
                bpmConfBaseManager.save(bpmConfBase);
                model = insertModel(repositoryService, bpmConfBase);
                entity = bpmConfBase;
            }
            if (!model.getId().equals(bpmConfBase.getModelId())) {
                bpmConfBase.setModelId(model.getId());
                bpmConfBase.setStatus(0); // 初始化
            }
            if (isCreate) {
                // 更新BpmModelEntity当前版本信息
                updateBpmModelEntityVersion(bpmConfBase);
            }
        } else {
            // 修改只能修改描述信息
            entity = bpmConfBaseManager.get(bpmConfBase.getId());
            entity.setDescription(bpmConfBase.getDescription());
        }
        bpmConfBaseManager.save(entity);
        return entity;
    }

    private void updateBpmModelEntityVersion(BpmConfBase bpmConfBase) {
        final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
        final Integer count = bpmConfBaseManager.getCount(
            "select count(*) from BpmConfBase t where t.bpmModel = ?0 and t.status=1",
            bpmModel);
        if (count > 0) {
            return;
        }
        bpmModel.setConfBaseModelId(bpmConfBase.getModelId());
        bpmModel.setConfBaseVersion(bpmConfBase.getVersion());
        bpmModel.setConfBaseStatus(bpmConfBase.getStatus());
        bpmModelManager.save(bpmModel);
    }

    private boolean checkBpmConfBase(BpmConfBase bpmConfBase) {
        BpmConfBase dbBpmConfBase = null;
        String hql = null;
        if (bpmConfBase.getId() != null) { //修改
            hql = " from BpmConfBase b where b.version = ?0 and b.bpmModel = ?1 and b.id <> ?2";
            dbBpmConfBase = bpmModelManager.findUnique(hql, bpmConfBase.getVersion(), bpmConfBase.getBpmModel(),
                bpmConfBase.getId());
        } else {
            hql = " from BpmConfBase b where b.version = ?0 and b.bpmModel = ?1";
            dbBpmConfBase = bpmModelManager.findUnique(hql, bpmConfBase.getVersion(),
                bpmConfBase.getBpmModel());
        }
        return null == dbBpmConfBase;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#removeBpmConfBaseById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBpmConfBaseById(Long id) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager.get(id);
        if (null == bpmConfBase) {
            return true;
        }

        return removeBpmConfBase(bpmConfBase, true);
    }

	@Override
	public boolean hasUnfinishedProcessByID(Long id) {
		final BpmConfBase bpmConfBase = bpmConfBaseManager.get(id);
		String processDefinitionId = bpmConfBase.getProcessDefinitionId();
		if (StrUtil.isNotEmpty(processDefinitionId)) {
			long count = checkUnfinishedProcessInstance(processDefinitionId);
			return 0 != count;
		}else{
			return false;
		}
	}

	private boolean removeBpmConfBase(final BpmConfBase bpmConfBase, boolean isUpdateBpmModel) {
		Assert.isTrue(1 != bpmConfBase.getStatus(), "已激活的版本不能删除");
        final String processDefinitionId = bpmConfBase.getProcessDefinitionId();
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        if (StrUtil.isNotEmpty(processDefinitionId)) {
        	if (workflowOpenApiService.getProcessWasUsed(processDefinitionId)) {
        		throw new ArchiveRuntimeException("该流程已被使用，不能删除！");
			}
            long count = checkUnfinishedProcessInstance(processDefinitionId);
			if (0 != count) {
				throw new ArchiveRuntimeException(String.format("流程中还有[%d]条流程实现正在运行，不能删除！", count));
			}
			final ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
            final String deploymentId = definition.getDeploymentId();
            final String modelId = repositoryService.createModelQuery().deploymentId(deploymentId)
                .singleResult().getId();
            repositoryService.deleteModel(modelId);
            repositoryService.deleteDeployment(deploymentId);
        } else {
            repositoryService.deleteModel(bpmConfBase.getModelId());
        }
        final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
        bpmConfBaseManager.remove(bpmConfBase);
        if (isUpdateBpmModel && bpmConfBase.getVersion().equals(bpmModel.getConfBaseVersion())) {
            updateBpmModelVersion(bpmModel);
        }
        return true;
    }

    private long checkUnfinishedProcessInstance(String processDefinitionId) {
        final HistoryService historyService = processEngine.getHistoryService();
        final long count = historyService.createHistoricProcessInstanceQuery().processDefinitionId(processDefinitionId)
            .unfinished().count();
        return count;
    }

    private void updateBpmModelVersion(BpmModelEntity bpmModel) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager
            .findUnique("from BpmConfBase t where t.bpmModel.id=?0 order by t.createTime desc", bpmModel.getId());
        if (null == bpmConfBase) {
            bpmModel.setConfBaseModelId(null);
            bpmModel.setConfBaseStatus(null);
            bpmModel.setConfBaseVersion(null);
        } else {
            bpmModel.setConfBaseModelId(bpmConfBase.getModelId());
            bpmModel.setConfBaseStatus(bpmConfBase.getStatus());
            bpmModel.setConfBaseVersion(bpmConfBase.getVersion());
        }
        bpmModelManager.save(bpmModel);
    }

    /**
     * 生成版本号
     *
     * @param bpmModelId
     * @return
     */
    private String generateVersion(Long bpmModelId) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager
            .findUnique("from BpmConfBase t where t.bpmModel.id=?0 order by t.createTime desc", bpmModelId);
        if (null == bpmConfBase) {
            return "1.0";
        }
        String version = NumberUtil.decimalFormat("#.0", Double.parseDouble(bpmConfBase.getVersion()) + 1);
        while (!checkBpmConfBaseByBpmModelIdAndVersion(bpmModelId, version)) {
            version = NumberUtil.decimalFormat("#.0", Double.parseDouble(version) + 1);
        }
        return version;
    }

    private boolean checkBpmConfBaseByBpmModelIdAndVersion(Long bpmModelId, String version) {
        final Integer count = bpmModelManager.getCount(
            "select count(*) from BpmConfBase b where b.bpmModel.id = ?0 and b.version = ?1", bpmModelId,
            version);
        return 0 == count;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#removeBpmConfBaseByBpmModelId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeBpmConfBaseByBpmModelId(Long bpmModelId) {
        final List<BpmConfBase> bpmConfBaseList = bpmConfBaseManager.findByBpmModelId(bpmModelId);
        for (final BpmConfBase entity : bpmConfBaseList) {
            removeBpmConfBase(entity, false);
        }
        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#openModelByModelId(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> openModelByModelId(String modelId) {
        final Map<String, Object> root = new HashMap<String, Object>();

        try {
            final RepositoryService repositoryService = processEngine.getRepositoryService();
            // 从仓库获取已经部署保存的model
            final Model model = repositoryService.getModel(modelId);
            final BpmConfBase bpmConfBase = bpmConfBaseManager.findUniqueBy("modelId", modelId);
            final BpmModelEntity bpmModelEntity = bpmConfBase.getBpmModel();

            root.put("modelId", model.getId());
            root.put("name", model.getName());
            root.put("bpmConfBaseId", bpmConfBase.getId());
            root.put("revision", bpmConfBase.getVersion());
            root.put("description", bpmModelEntity.getDescription());
            root.put("bpmModelId", bpmModelEntity.getId());
            final byte[] bytes = repositoryService.getModelEditorSource(model.getId());

            final JsonMapper jsonMapper = new JsonMapper();
            if (bytes != null) {
                final String modelEditorSource = new String(bytes, StandardCharsets.UTF_8);
                final Map<String, Object> modelNode = jsonMapper.fromJson(modelEditorSource, Map.class);
                root.put("model", modelNode);
            } else {
                final Map<String, Object> modelNode = new HashMap<String, Object>();
                modelNode.put("id", "canvas");
                modelNode.put("resourceId", "canvas");

                final Map<String, Object> stencilSetNode = new HashMap<String, Object>();
                stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
                modelNode.put("stencilset", stencilSetNode);

                final Map<String, Object> properties = new HashMap<String, Object>();
                properties.put("process_id", model.getKey());
                properties.put("name", model.getName());
                properties.put("documentation", bpmModelEntity.getDescription());
                properties.put("process_author", "workflow");
                properties.put("process_version", bpmConfBase.getVersion());
                properties.put("process_namespace", "http://www.activiti.org/processdef");
                properties.put("executionlisteners", "");
                properties.put("eventlisteners", "");
                properties.put("signaldefinitions", "");
                properties.put("messagedefinitions", "");

                modelNode.put("properties", properties);
                model.setMetaInfo(jsonMapper.toJson(root));

                root.put("model", modelNode);
            }
        } catch (final Exception e) {
            log.error("打开流程图出错", e);
            throw new ArchiveRuntimeException("打开流程图出错", e);
        }

        return root;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#saveModel(java.lang.String,
     *      java.lang.String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Model saveModel(String modelId, String xmlJson) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager.findUniqueBy("modelId", modelId);
        Assert.notNull(bpmConfBase, "流程版本不存在，请刷新流程版本列表");
        Assert.isTrue(1 != bpmConfBase.getStatus(), "当前版本已被激活，不能修改流程图");
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        Model model = repositoryService.getModel(modelId);
        if (2 == bpmConfBase.getStatus() || null == model
            || null != model.getDeploymentId()/*对已发布的model进行保存则生成一个新的model*/) {
            model = insertModel(repositoryService, bpmConfBase);
            bpmConfBase.setStatus(0); // 设置成初始化状态
            bpmConfBase.setModelId(model.getId());
            bpmConfBaseManager.save(bpmConfBase);
            // 联动更新bpmModelEntity里的版本信息
            final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
            if (bpmConfBase.getVersion().equals(bpmModel.getConfBaseVersion())) {
                bpmModel.setConfBaseModelId(model.getId());
                bpmModel.setConfBaseStatus(bpmConfBase.getStatus());
                bpmModelManager.save(bpmModel);
            }
        } else {
            model = repositoryService.getModel(modelId);
        }
        saveModelXml(repositoryService, model, xmlJson);
        // 更新时间
        bpmConfBase.setUpdateTime(new Date());
        bpmConfBaseManager.save(bpmConfBase);
        return model;
    }

    private Model insertModel(final RepositoryService repositoryService, BpmConfBase bpmConfBase) {
        final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
        final org.activiti.engine.repository.Model model = repositoryService.newModel();
        model.setName(bpmModel.getName());
        model.setKey(bpmModel.getCode());
        model.setCategory(bpmConfBase.getId().toString());
        model.setTenantId(bpmModel.getTenantId());
        repositoryService.saveModel(model);
        return model;
    }

    private Model saveModelXml(final RepositoryService repositoryService, Model model, String xmlJson) {
        Assert.isTrue(StrUtil.isBlank(model.getDeploymentId()), "发布过的流程暂不支持修改流程信息");
		repositoryService.addModelEditorSource(model.getId(), xmlJson.getBytes(StandardCharsets.UTF_8));
		return model;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#exportModelXmlFile(java.lang.String)
     */
    @Override
    public String exportModelXmlFile(String xmlJson) {
        String jsonStr = xmlJson.replace(":[]", ":\"EmptyArray\"");
        jsonStr = "{\"bpm\":" + jsonStr + "}";
        final StringReader input = new StringReader(jsonStr);
        final StringWriter output = new StringWriter();
        final JsonXMLConfig config = new JsonXMLConfigBuilder().autoPrimitive(false).repairingNamespaces(false).build();
        try {
            final XMLEventReader reader = new JsonXMLInputFactory(config).createXMLEventReader(input);
            XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(output);
            writer = new PrettyXMLEventWriter(writer);
            writer.add(reader);
            reader.close();
            writer.close();
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("导出流程图出错", e);
        } finally {
            IoUtil.close(output);
            IoUtil.close(input);
        }
        final String xmlValue = output.toString();

        return xmlValue;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getModelByModelId(java.lang.String)
     */
    @Override
    public Model getModelByModelId(String modelId) {
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        // 从仓库获取已经部署保存的model
        return repositoryService.getModel(modelId);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#importModelXmlFile(java.lang.String,
     *      java.io.File)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Model importModelXmlFile(String modelId, File xmlFile) {
        InputStreamReader streamReader = null;
        BufferedReader bufReader = null;
        StringReader input = null;
        StringWriter output = null;

        try {
            streamReader = new InputStreamReader(new FileInputStream(xmlFile), StandardCharsets.UTF_8);
            bufReader = new BufferedReader(streamReader);
            final StringBuilder builder = new StringBuilder();
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = bufReader.readLine()) != null) {
                builder.append(tempString);
            }
            input = new StringReader(builder.toString());
            output = new StringWriter();
            final JsonXMLConfig config = new JsonXMLConfigBuilder().autoArray(true).build();

            final XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(input);
            final XMLEventWriter writer = new JsonXMLOutputFactory(config).createXMLEventWriter(output);
            writer.add(reader);
            reader.close();
            writer.close();
            String xmlJson = output.toString().replace("\"null\"", "null").replace("null", "\"\"")
                .replace("\"EmptyArray\"", "[]");
            xmlJson = xmlJson.substring(7, xmlJson.length() - 1);
            return saveModel(modelId, processImportXmlJson(modelId, xmlJson));
        } catch (final Exception e) {
            log.error("导入流程图出错", e);
            throw new ArchiveRuntimeException("导入流程图出错", e);
        } finally {
            IoUtil.close(bufReader);
            IoUtil.close(output);
            IoUtil.close(input);
            IoUtil.close(streamReader);
        }
    }

    @SuppressWarnings("unchecked")
    private String processImportXmlJson(String modelId, String xmlJson) throws IOException {
        final JsonMapper jsonMapper = new JsonMapper();
        final Map<String, Object> modelNode = jsonMapper.fromJson(xmlJson, Map.class);
        final Map<String, Object> properties = (Map<String, Object>) modelNode.get("properties");
        final BpmConfBase bpmConfBase = bpmConfBaseManager.findUniqueBy("modelId", modelId);
        final BpmModelEntity bpmModelEntity = bpmConfBase.getBpmModel();
        // 把导入的文件中的流程信息改为把原本的code在给他还原进去
        properties.put("process_id", bpmModelEntity.getCode());
        properties.put("name", bpmModelEntity.getName());
        properties.put("process_version", bpmConfBase.getVersion());
        return jsonMapper.toJson(modelNode);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#activeProcessByBpmConfBaseId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activeProcessByBpmConfBaseId(Long bpmConfBaseId) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager.get(bpmConfBaseId);
        return activeProcess(bpmConfBase);
    }

    private boolean activeProcess(final BpmConfBase bpmConfBase) {
        Assert.notNull(bpmConfBase, "流程版本不存在，请刷新流程版本列表");
        Assert.isTrue(!WorkflowConstants.BpmConfBaseConstants.STATUS_ACTIVE.equals(bpmConfBase.getStatus()),
            "流程版本不存在，请刷新流程版本列表");
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        final Model model = repositoryService.getModel(bpmConfBase.getModelId());
        // 只允许一条流程定义是激活状态（前一次激活的流程定义）
        final ProcessDefinition preProcessDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(model.getKey())
            .processDefinitionTenantId(bpmConfBase.getBpmModel().getTenantId())
            .active().singleResult();
        if (preProcessDefinition != null) {
            repositoryService.suspendProcessDefinitionById(preProcessDefinition.getId(),
                false, null);
            final BpmConfBase activeBase = bpmConfBaseManager
                .findUniqueBy("processDefinitionId", preProcessDefinition.getId());
            // 将原来激活的挂起
            if (null != activeBase && !bpmConfBase.getId().equals(activeBase.getId())) {
                activeBase.setStatus(WorkflowConstants.BpmConfBaseConstants.STATUS_SUSPEND);
                bpmConfBaseManager.save(activeBase);
            }
        }
        //
        ProcessDefinition deployProcessDefinition = null;
        if (StrUtil.isBlank(model.getDeploymentId())) {
            deployProcessDefinition = activeNotDeployment(bpmConfBase, model, repositoryService);
        } else {
            deployProcessDefinition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(model.getDeploymentId()).singleResult();
            repositoryService.activateProcessDefinitionById(deployProcessDefinition.getId());
        }
        // 激活当前的流程定义
        bpmConfBase.setProcessDefinitionId(deployProcessDefinition.getId());
        bpmConfBase.setProcessDefinitionKey(deployProcessDefinition.getKey());
        bpmConfBase.setProcessDefinitionVersion(deployProcessDefinition.getVersion());
        bpmConfBase.setStatus(WorkflowConstants.BpmConfBaseConstants.STATUS_ACTIVE);
        bpmConfBaseManager.save(bpmConfBase);
        bpmConfBaseManager.flush();
        // 更新模型上的版本信息
        final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
        bpmModel.setConfBaseModelId(bpmConfBase.getModelId());
        bpmModel.setConfBaseVersion(bpmConfBase.getVersion());
        bpmModel.setConfBaseStatus(bpmConfBase.getStatus());
        bpmModelManager.save(bpmModel);
        bpmModelManager.flush();
        // 同步记录流程定义
        processEngine.getManagementService().executeCommand(new SyncProcessCmd(bpmConfBase.getProcessDefinitionId()));
        return true;
    }

    private ProcessDefinition activeNotDeployment(BpmConfBase bpmConfBase, final Model model,
        final RepositoryService repositoryService) {
        try {
            final BpmModelEntity bpmModelEntity = bpmConfBase.getBpmModel();

            final byte[] bpmnBytes = repositoryService.getModelEditorSource(model.getId());
            if (null == bpmnBytes) {
                throw new ArchiveRuntimeException("激活流程出错：流程图未设计");
            }

            final String processName = model.getName() + ".bpmn20.xml";
            final JsonNode modelNode = new ObjectMapper().readTree(bpmnBytes);

            final BpmnModel bpmModel = new BpmnJsonConverter().convertToBpmnModel(modelNode);
            final byte[] bytes = new BpmnXMLConverter().convertToXML(bpmModel);

            final Deployment deployment = repositoryService.createDeployment().name(model.getName())
                .addString(processName, new String(bytes, StandardCharsets.UTF_8)).tenantId(bpmModelEntity.getTenantId()).deploy();
            model.setDeploymentId(deployment.getId());
            repositoryService.saveModel(model);
            return repositoryService.createProcessDefinitionQuery().deploymentId(model.getDeploymentId())
                .singleResult();
        } catch (final ArchiveRuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("激活流程出错", e);
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#activeProcessByBpmConfBaseId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean activeProcessByModelId(String modelId) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager.findUniqueBy("modelId", modelId);
        return activeProcess(bpmConfBase);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#suspendProcessByBpmConfBaseId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean suspendProcessByBpmConfBaseId(Long bpmConfBaseId) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager.get(bpmConfBaseId);
        return suspendProcess(bpmConfBase);
    }

    private boolean suspendProcess(final BpmConfBase bpmConfBase) {
        Assert.notNull(bpmConfBase, "流程版本不存在，请刷新流程版本列表");
        final String processDefinitionId = bpmConfBase.getProcessDefinitionId();
        if (StrUtil.isNotBlank(processDefinitionId)) {
            final RepositoryService repositoryService = processEngine.getRepositoryService();
            final ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
            if (null == processDefinition) {
                if (log.isWarnEnabled()) {
                    log.warn("挂起流程时，ProcessDefinition.id={} is null", processDefinitionId);
                }
            } else {
                if (processDefinition.isSuspended()) {
                    // 已经是挂起状态
                    if (log.isWarnEnabled()) {
                        log.warn("挂起流程时，ProcessDefinition.isSuspended() == true");
                    }
                } else {
                    repositoryService.suspendProcessDefinitionById(processDefinitionId,
                        false, null);
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("挂起流程时，BpmConfBase.processDefinitionId is null");
            }
        }
        bpmConfBase.setStatus(WorkflowConstants.BpmConfBaseConstants.STATUS_SUSPEND);
        bpmConfBaseManager.save(bpmConfBase);
        bpmConfBaseManager.flush();
        // 更新模型上的版本信息
        final BpmModelEntity bpmModel = bpmConfBase.getBpmModel();
        bpmModel.setConfBaseModelId(bpmConfBase.getModelId());
        bpmModel.setConfBaseVersion(bpmConfBase.getVersion());
        bpmModel.setConfBaseStatus(bpmConfBase.getStatus());
        bpmModelManager.save(bpmModel);
        bpmModelManager.flush();
        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#suspendProcessByBpmModelId(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean suspendProcessByBpmModelId(Long bpmModelId) {
        final List<BpmConfBase> list = bpmConfBaseManager
            .find("from BpmConfBase t where t.bpmModel.id=?0 and t.status=?1", bpmModelId,
                WorkflowConstants.BpmConfBaseConstants.STATUS_ACTIVE);
        if (null == list) {
            return true;
        }
        for (final BpmConfBase entity : list) {
            suspendProcess(entity);
        }
        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getDeptRootList()
     */
    @Override
    public List<Map<String, Object>> getDeptRootList() {
        final List<Map<String, Object>> nodeList = Lists.newArrayList();
        nodeList.add(toCoralSimpleTreeNode(0, null, "组织", true));
        nodeList.addAll(getDeptNodeByParentId(0L));
        return nodeList;
    }

    private Map<String, Object> toCoralSimpleTreeNode(Object id, Object pId, Object name, Object open) {
        return toCoralSimpleTreeNode(id, pId, null, name, open, true);
    }

    private Map<String, Object> toCoralSimpleTreeNode(Object id, Object pId, Object loginName, Object name, Object open,
        Boolean isParent) {
        final Map<String, Object> node = new HashMap<String, Object>();
        node.put("id", id);
        node.put("pId", pId);
        if (null != loginName) {
            node.put("loginName", loginName);
        }
        node.put("name", name);
        node.put("isParent", isParent);
        node.put("open", open);
        return node;
    }

    private boolean checkRemoteData(R<?> remoteData, String name) {
        if (!CommonConstants.SUCCESS.equals(remoteData.getCode())) {
            log.error("获取" + name + "失败");
            return false;
        }
        if (null == remoteData.getData()) {
            log.error("没有" + name + "数据");
            return false;
        }

        return true;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getDeptNodeByParentId(java.lang.Integer)
     */
    @Override
    public List<Map<String, Object>> getDeptNodeByParentId(Long parentId) {
        final List<Map<String, Object>> nodeList = Lists.newArrayList();
        if (null == parentId) {
            return nodeList;
        }
        final R<List<SysDept>> remoteData = remoteDeptService.getDeptByParentId(parentId);
        if (!checkRemoteData(remoteData, "组织机构")) {
            return nodeList;
        }
        final List<SysDept> deptList = remoteData.getData();
        deptList.forEach(dept -> {
            nodeList.add(toCoralSimpleTreeNode(dept.getDeptId(), dept.getParentId(), dept.getName(), false));
        });
        return nodeList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getUserListByParentId(java.lang.Integer)
     */
    @Override
    public List<Map<String, Object>> getUserListByDeptId(Long deptId) {
        final List<Map<String, Object>> nodeList = Lists.newArrayList();
        nodeList.add(toCoralSimpleTreeNode(-1, null, "组织用户", true));
        final R<List<SysUser>> remoteData = remoteUserService.getUsersByDeptId(deptId);
        if (!checkRemoteData(remoteData, "组织用户")) {
            return nodeList;
        }
        final List<SysUser> userList = remoteData.getData();
        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        userList.forEach(user -> {
            nodeList.add(toCoralSimpleTreeNode(user.getUserId(), -1, user.getUsername(), user.getChineseName(), false, false));
        });
        return nodeList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getRoleRootList()
     */
    @Override
    public List<Map<String, Object>> getRoleRootList() {
        final List<Map<String, Object>> nodeList = Lists.newArrayList();
        nodeList.add(toCoralSimpleTreeNode(0, null, "角色", true));
        final R<List<SysRole>> remoteData = remoteRoleService.getAllRoleList();
        if (!checkRemoteData(remoteData, "角色")) {
            return nodeList;
        }
        final List<SysRole> roleList = remoteData.getData();
        roleList.forEach(role -> {
        	if((ObjectUtil.isNotNull(role.getRoleCode())&&!role.getRoleCode().equals(RoleConstant.RoleCodeEnum.SECURITY_SECRET_MANAGER.getCode())&&
					!role.getRoleCode().equals(RoleConstant.RoleCodeEnum.SECURITY_AUDIT_MANAGER.getCode())&&
					!role.getRoleCode().equals(RoleConstant.RoleCodeEnum.SYSTEM_MANAGER.getCode())&&
					!role.getRoleCode().equals(RoleConstant.RoleCodeEnum.TENANT_ADMIN.getCode()))||ObjectUtil.isNull(role.getRoleCode())){
				nodeList
						.add(toCoralSimpleTreeNode(role.getRoleId(), role.getParentId(), null, role.getRoleName(), false,
								0 == role.getRoleType()));
			}

        });
        List<Object> pIds = nodeList.stream().map( map -> map.get("pId")).collect(Collectors.toList());
		nodeList.forEach(map -> {
			if(!pIds.contains(map.get("id"))){
				map.put("isParent", false);
			}
		});
        return nodeList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getUserListByRoleId(java.lang.Integer)
     */
    @Override
    public List<Map<String, Object>> getUserListByRoleId(Long roleId) {
        final List<Map<String, Object>> nodeList = Lists.newArrayList();
        nodeList.add(toCoralSimpleTreeNode(-1, null, "角色用户", true));
        final R<List<SysUser>> remoteData = remoteUserService.getUsersByRoleId(roleId);
        if (!checkRemoteData(remoteData, "角色用户")) {
            return nodeList;
        }
        final List<SysUser> userList = remoteData.getData();
        if (userList.isEmpty()) {
            return Collections.emptyList();
        }
        userList.forEach(user -> {
            nodeList.add(toCoralSimpleTreeNode(user.getUserId(), -1, user.getUsername(), user.getChineseName(), false, false));
        });
        return nodeList;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getCustomFormulaList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getCustomFormulaList(Long tenantId, String bpmModelCode) {
        final List<Map<String, Object>> data = Lists.newArrayList();
        try {
            final URL url = this.getClass().getResource("/workflow/formula.xml");
            final String xmlStr = FileUtil.readString(url, "utf-8");
            final Document document = DocumentHelper.parseText(xmlStr);
            // 租户个性表达式
            getXmlPathData(data, document, StrBuilder.create().append("/formulas/tenant_").append(tenantId).append("/")
                .append(bpmModelCode.toLowerCase()).toString());
            // 流程特有表达式
            getXmlPathData(data, document,
                StrBuilder.create().append("/formulas/commons/").append(bpmModelCode.toLowerCase()).toString());
            // 全局公共表达式
            getXmlPathData(data, document, "/formulas/commons/common");
        } catch (final Exception e) {
            log.error("获取自定义表达式出错", e);
        }
        return data;
    }

    void getXmlPathData(List<Map<String, Object>> data, Document document, String xpath) {
        final List<Node> selectNodes = document.selectNodes(xpath);
        selectNodes.forEach(node -> {
            final Node textNode = node.selectSingleNode(".//text");
            if (null == textNode) {
                return;
            }
            final Node valueNode = node.selectSingleNode(".//value");
            if (null == valueNode) {
                return;
            }
            final Map<String, Object> item = Maps.newHashMap();
            item.put("text", textNode.getText());
            item.put("value", valueNode.getText());
            data.add(item);
        });
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getExecutionListenerList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getExecutionListenerList(Long tenantId, String bpmModelCode) {
        return getListenerList(tenantId, "executions", bpmModelCode);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getTaskListenerList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getTaskListenerList(Long tenantId, String bpmModelCode) {
        return getListenerList(tenantId, "tasks", bpmModelCode);
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getEventListenerList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getEventListenerList(Long tenantId, String bpmModelCode) {
        return getListenerList(tenantId, "events", bpmModelCode);
    }

    /**
     * 获取监听器集合
     *
     * @param tenantId
     *            租户ID
     * @param category
     *            分类： executions/tasks/events
     * @param bpmModelCode
     *            流程编码
     * @return
     */
    private List<Map<String, Object>> getListenerList(Long tenantId, String category, String bpmModelCode) {
        final List<Map<String, Object>> data = Lists.newArrayList();
        try {
            final URL url = this.getClass().getResource("/workflow/listener.xml");
            final String xmlStr = FileUtil.readString(url, "utf-8");
            final Document document = DocumentHelper.parseText(xmlStr);
            // 租户个性表达式
            getXmlPathData(data, document, StrBuilder.create().append("/listeners/tenant_").append(tenantId).append("/")
                .append(bpmModelCode.toLowerCase()).append("/").append(category).toString());
            // 流程特有表达式
            getXmlPathData(data, document,
                StrBuilder.create().append("/listeners/commons/").append(bpmModelCode.toLowerCase()).append("/")
                    .append(category).toString());
            // 全局公共表达式
            getXmlPathData(data, document, "/listeners/commons/common/" + category);
        } catch (final Exception e) {
            log.error("获取自定义表达式出错", e);
        }
        return data;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getConditionFieldList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getConditionFieldList(Long tenantId, String bpmModelCode) {
        final List<Map<String, Object>> data = Lists.newArrayList();
        final BpmModelEntity bpmModelEntity = bpmModelManager.findByTenantIdAndCode(tenantId.toString(), bpmModelCode);
        Assert.notNull(bpmModelEntity, String.format("流程编码[%s]对应的流程不存在", bpmModelCode));
        final List<BusinessMetadata> conditionList = businessMetadataManager.findConditionList(tenantId.toString(),
            bpmModelEntity.getBusinessCode());
        if (null != conditionList) {
            for (final BusinessMetadata businessMetadata : conditionList) {
				String metadataEnglish = businessMetadata.getMetadataEnglish();
				//点击流条件的时候替换字段，专门存放部门名称
				if (metadataEnglish.equals(FilingFieldConstants.ARCHIVE_DEPT)) {
            		metadataEnglish = FilingFieldConstants.ARCHIVE_DEPT_NAME;
				}
                data.add(MapUtil.<String, Object> builder("value", metadataEnglish)
                    .put("text", businessMetadata.getMetadataChinese()).build());
            }
        }
        return data;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getDeptFieldList(java.lang.Integer,
     *      java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getDeptFieldList(Long tenantId, String bpmModelCode) {
        final List<Map<String, Object>> data = Lists.newArrayList();
        final BpmModelEntity bpmModelEntity = bpmModelManager.findByTenantIdAndCode(tenantId.toString(), bpmModelCode);
        Assert.notNull(bpmModelEntity, String.format("流程编码[%s]对应的流程不存在", bpmModelCode));
		String businessCode;
		if (bpmModelCode.startsWith(ModelTypeEnum.TRANSFERRECEIVING.getCode())) { //移交接收和移交共用一套表
			businessCode = ModelTypeEnum.TRANSFERFORM.getCode();
		}else{
			businessCode = bpmModelEntity.getBusinessCode();
		}
		final List<BusinessMetadata> conditionList = businessMetadataManager.findDeptList(tenantId.toString(),businessCode);
        if (null != conditionList) {
            for (final BusinessMetadata businessMetadata : conditionList) {
                data.add(MapUtil.<String, Object> builder("value", businessMetadata.getMetadataEnglish())
                    .put("text", businessMetadata.getMetadataChinese()).build());
            }
        }
        return data;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#copyBpmConfBaseById(java.lang.Long)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BpmConfBase copyBpmConfBaseById(Long id) {
        Assert.notNull(id, "复制流程版本ID不能为空");
        final BpmConfBase bpmConfBase = bpmConfBaseManager.get(id);
        Assert.notNull(bpmConfBase, "复制流程版本不存在，请刷新再试");
        final RepositoryService repositoryService = processEngine.getRepositoryService();
        final byte[] bytes = repositoryService.getModelEditorSource(bpmConfBase.getModelId());
        if (null == bytes) {
            throw new ArchiveRuntimeException("该版本流程图未设计");
        }
        final BpmConfBase targetBpmConfBase = new BpmConfBase();
        try {
            final String modelEditorSource = new String(bytes, StandardCharsets.UTF_8);
            final JsonMapper jsonMapper = new JsonMapper();
            final Map<String, Object> modelNode = jsonMapper.fromJson(modelEditorSource, Map.class);
            final Map<String, Object> properties = (Map<String, Object>) modelNode.get("properties");
            // 新增一个版本
            targetBpmConfBase.setDescription("复制来源于：版本" + bpmConfBase.getVersion());
            saveBpmConfBase(targetBpmConfBase, bpmConfBase.getBpmModel().getId());
            // 把导入的文件中的流程信息改为把原本的code在给他还原进去
            modelNode.put("resourceId", targetBpmConfBase.getModelId());
            properties.put("process_version", targetBpmConfBase.getVersion());
            // 复制流程图
            saveModel(targetBpmConfBase.getModelId(), jsonMapper.toJson(modelNode));
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("复制出错", e);
        }
        return targetBpmConfBase;
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getImageByProcessDefinitionId(java.lang.String)
     */
    @Override
    public String getImageByProcessDefinitionId(String processDefinitionId) {
        try {
            return bpmConfBaseManager.getImageByProcessDefinitionId(processDefinitionId);
        } catch (final Exception e) {
            throw new ArchiveRuntimeException("获取流程图出错", e);
        }
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getImageByBpmModelIdAndVersion(java.lang.Long,
     *      java.lang.String)
     */
    @Override
    public String getImageByBpmModelIdAndVersion(Long bpmModelId, String version) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager
            .findUnique("from BpmConfBase t where t.bpmModel.id=?0 and t.version=?1", bpmModelId, version);
        Assert.notNull(bpmConfBase, String.format("流程版本%s不存在", version));
        return getImageByProcessDefinitionId(bpmConfBase.getProcessDefinitionId());
    }

    /**
     *
     * @see com.cescloud.saas.archive.service.modular.workflow.service.WorkflowVersionService#getImageByModelId(java.lang.String)
     */
    @Override
    public String getImageByModelId(String modelId) {
        final BpmConfBase bpmConfBase = bpmConfBaseManager
            .findUnique("from BpmConfBase t where t.modelId=?0", modelId);
        Assert.notNull(bpmConfBase, "流程版本不存在，请刷新再试");
        return getImageByProcessDefinitionId(bpmConfBase.getProcessDefinitionId());
    }
}
