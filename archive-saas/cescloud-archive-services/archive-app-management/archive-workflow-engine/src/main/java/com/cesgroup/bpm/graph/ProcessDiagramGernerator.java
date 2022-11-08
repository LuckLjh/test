package com.cesgroup.bpm.graph;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.FlowElementsContainer;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Gateway;
import org.activiti.bpmn.model.GraphicInfo;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.image.impl.DefaultProcessDiagramCanvas;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chen.liang1
 * @version 1.0.0 2018-01-12
 */
public class ProcessDiagramGernerator extends DefaultProcessDiagramGenerator {

	@Override
	protected void drawActivity(DefaultProcessDiagramCanvas processDiagramCanvas,
								BpmnModel bpmnModel, FlowNode flowNode, List<String> highLightedActivities,
								List<String> highLightedFlows, double scaleFactor) {

		ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(flowNode.getClass());
		if (drawInstruction != null) {

			drawInstruction.draw(processDiagramCanvas, bpmnModel, flowNode);

			// Gather info on the multi instance marker
			boolean multiInstanceSequential = false;
			boolean multiInstanceParallel = false;
			boolean collapsed = false;
			if (flowNode instanceof Activity) {
				Activity activity = (Activity) flowNode;
				MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity
						.getLoopCharacteristics();
				if (multiInstanceLoopCharacteristics != null) {
					multiInstanceSequential = multiInstanceLoopCharacteristics.isSequential();
					multiInstanceParallel = !multiInstanceSequential;
				}
			}

			// Gather info on the collapsed marker
			GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			if (flowNode instanceof SubProcess) {
				collapsed = graphicInfo.getExpanded() != null && !graphicInfo.getExpanded();
			} else if (flowNode instanceof CallActivity) {
				collapsed = true;
			}


			if (new BigDecimal(Double.toString(scaleFactor)).equals(new BigDecimal("1.0"))) {
				// Actually draw the markers
				processDiagramCanvas.drawActivityMarkers((int) graphicInfo.getX(),
						(int) graphicInfo.getY(), (int) graphicInfo.getWidth(),
						(int) graphicInfo.getHeight(), multiInstanceSequential, multiInstanceParallel,
						collapsed);
			}

			// Draw highlighted activities
			if (highLightedActivities.contains(flowNode.getId())) {
				drawHighLight(processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()));
			}

		}

		// Outgoing transitions of activity
		for (SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
			boolean highLighted = (highLightedFlows.contains(sequenceFlow.getId()));
			String defaultFlow = null;
			if (flowNode instanceof Activity) {
				defaultFlow = ((Activity) flowNode).getDefaultFlow();
			} else if (flowNode instanceof Gateway) {
				defaultFlow = ((Gateway) flowNode).getDefaultFlow();
			}

			boolean isDefault = false;
			if (defaultFlow != null && defaultFlow.equalsIgnoreCase(sequenceFlow.getId())) {
				isDefault = true;
			}
			boolean drawConditionalIndicator = sequenceFlow.getConditionExpression() != null
					&& !(flowNode instanceof Gateway);

			String sourceRef = sequenceFlow.getSourceRef();
			String targetRef = sequenceFlow.getTargetRef();
			FlowElement sourceElement = bpmnModel.getFlowElement(sourceRef);
			FlowElement targetElement = bpmnModel.getFlowElement(targetRef);
			List<GraphicInfo> graphicInfoList = bpmnModel
					.getFlowLocationGraphicInfo(sequenceFlow.getId());
			if (graphicInfoList != null && graphicInfoList.size() > 0) {
				graphicInfoList = connectionPerfectionizer(processDiagramCanvas, bpmnModel,
						sourceElement, targetElement, graphicInfoList);
				int[] pointsX = new int[graphicInfoList.size()];
				int[] pointsY = new int[graphicInfoList.size()];

				for (int i = 1; i < graphicInfoList.size(); i++) {
					GraphicInfo graphicInfo = graphicInfoList.get(i);
					GraphicInfo previousGraphicInfo = graphicInfoList.get(i - 1);

					if (i == 1) {
						pointsX[0] = (int) previousGraphicInfo.getX();
						pointsY[0] = (int) previousGraphicInfo.getY();
					}
					pointsX[i] = (int) graphicInfo.getX();
					pointsY[i] = (int) graphicInfo.getY();

				}

				processDiagramCanvas.drawSequenceflow(pointsX, pointsY, drawConditionalIndicator,
						isDefault, highLighted, scaleFactor);

				// Draw sequenceflow label
				GraphicInfo labelGraphicInfo = bpmnModel.getLabelGraphicInfo(sequenceFlow.getId());
				if (labelGraphicInfo != null) {
					processDiagramCanvas.drawLabel(sequenceFlow.getName(), labelGraphicInfo, true);
				}
			}
		}

		// Nested elements
		if (flowNode instanceof FlowElementsContainer) {
			for (FlowElement nestedFlowElement : ((FlowElementsContainer) flowNode)
					.getFlowElements()) {
				if (nestedFlowElement instanceof FlowNode) {
					drawActivity(processDiagramCanvas, bpmnModel, (FlowNode) nestedFlowElement,
							highLightedActivities, highLightedFlows, scaleFactor);
				}
			}
		}
	}

	private static void drawHighLight(DefaultProcessDiagramCanvas processDiagramCanvas,
									  GraphicInfo graphicInfo) {
		processDiagramCanvas.drawHighLight((int) graphicInfo.getX(), (int) graphicInfo.getY(),
				(int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());

	}

}
