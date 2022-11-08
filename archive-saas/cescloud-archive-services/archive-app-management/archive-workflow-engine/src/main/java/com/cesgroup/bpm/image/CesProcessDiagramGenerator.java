/**
 * <p>Copyright:Copyright(c) 2019</p>
 * <p>Company:上海中信信息发展股份有限公司</p>
 * <p>包名:com.cesgroup.bpm.image</p>
 * <p>文件名:CesProcessDiagramGenerator.java</p>
 * <p>创建时间:2019年12月10日 下午4:28:00</p>
 * <p>作者:qiucs</p>
 */

package com.cesgroup.bpm.image;

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.impl.DefaultProcessDiagramCanvas;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * 流程图生成，参考DefaultProcessDiagramGenerator
 *
 * 写死字体（linux无中文这字体）
 *
 * @author qiucs
 * @version 1.0.0 2019年12月10日
 */
public class CesProcessDiagramGenerator implements ProcessDiagramGenerator {

    protected Map<Class<? extends BaseElement>, ActivityDrawInstruction> activityDrawInstructions = new HashMap<Class<? extends BaseElement>, ActivityDrawInstruction>();

    protected Map<Class<? extends BaseElement>, ArtifactDrawInstruction> artifactDrawInstructions = new HashMap<Class<? extends BaseElement>, ArtifactDrawInstruction>();

    public CesProcessDiagramGenerator() {
        this(1.0);
    }

    // The instructions on how to draw a certain construct is
    // created statically and stored in a map for performance.
    public CesProcessDiagramGenerator(final double scaleFactor) {
        // start event
        activityDrawInstructions.put(StartEvent.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final StartEvent startEvent = (StartEvent) flowNode;
			if (startEvent.getEventDefinitions() != null && !startEvent.getEventDefinitions().isEmpty()) {
				final EventDefinition eventDefinition = startEvent.getEventDefinitions().get(0);
				if (eventDefinition instanceof TimerEventDefinition) {
					processDiagramCanvas.drawTimerStartEvent(graphicInfo, scaleFactor);
				} else if (eventDefinition instanceof ErrorEventDefinition) {
					processDiagramCanvas.drawErrorStartEvent(graphicInfo, scaleFactor);
				} else if (eventDefinition instanceof SignalEventDefinition) {
					processDiagramCanvas.drawSignalStartEvent(graphicInfo, scaleFactor);
				} else if (eventDefinition instanceof MessageEventDefinition) {
					processDiagramCanvas.drawMessageStartEvent(graphicInfo, scaleFactor);
				} else {
					processDiagramCanvas.drawNoneStartEvent(graphicInfo);
				}
			} else {
				processDiagramCanvas.drawNoneStartEvent(graphicInfo);
			}
		});

        // signal catch
        activityDrawInstructions.put(IntermediateCatchEvent.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final IntermediateCatchEvent intermediateCatchEvent = (IntermediateCatchEvent) flowNode;
			if (intermediateCatchEvent.getEventDefinitions() != null
				&& !intermediateCatchEvent.getEventDefinitions()
					.isEmpty()) {
				if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
					processDiagramCanvas.drawCatchingSignalEvent(flowNode.getName(), graphicInfo, true,
						scaleFactor);
				} else if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {
					processDiagramCanvas.drawCatchingTimerEvent(flowNode.getName(), graphicInfo, true, scaleFactor);
				} else if (intermediateCatchEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
					processDiagramCanvas.drawCatchingMessageEvent(flowNode.getName(), graphicInfo, true,
						scaleFactor);
				}
			}
		});

        // signal throw
        activityDrawInstructions.put(ThrowEvent.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final ThrowEvent throwEvent = (ThrowEvent) flowNode;
			if (throwEvent.getEventDefinitions() != null && !throwEvent.getEventDefinitions().isEmpty()) {
				if (throwEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
					processDiagramCanvas.drawThrowingSignalEvent(graphicInfo, scaleFactor);
				} else if (throwEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
					processDiagramCanvas.drawThrowingCompensateEvent(graphicInfo, scaleFactor);
				} else {
					processDiagramCanvas.drawThrowingNoneEvent(graphicInfo, scaleFactor);
				}
			} else {
				processDiagramCanvas.drawThrowingNoneEvent(graphicInfo, scaleFactor);
			}
		});

        // end event
        activityDrawInstructions.put(EndEvent.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final EndEvent endEvent = (EndEvent) flowNode;
			if (endEvent.getEventDefinitions() != null && !endEvent.getEventDefinitions().isEmpty()) {
				if (endEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {
					processDiagramCanvas.drawErrorEndEvent(flowNode.getName(), graphicInfo, scaleFactor);
				} else {
					processDiagramCanvas.drawNoneEndEvent(graphicInfo, scaleFactor);
				}
			} else {
				processDiagramCanvas.drawNoneEndEvent(graphicInfo, scaleFactor);
			}
		});

        // task
        activityDrawInstructions.put(Task.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawTask(flowNode.getName(), graphicInfo);
		});

        // user task
        activityDrawInstructions.put(UserTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawUserTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // script task
        activityDrawInstructions.put(ScriptTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawScriptTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // service task
        activityDrawInstructions.put(ServiceTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final ServiceTask serviceTask = (ServiceTask) flowNode;
			if ("camel".equalsIgnoreCase(serviceTask.getType())) {
				processDiagramCanvas.drawCamelTask(serviceTask.getName(), graphicInfo, scaleFactor);
			} else if ("mule".equalsIgnoreCase(serviceTask.getType())) {
				processDiagramCanvas.drawMuleTask(serviceTask.getName(), graphicInfo, scaleFactor);
			} else {
				processDiagramCanvas.drawServiceTask(serviceTask.getName(), graphicInfo, scaleFactor);
			}
		});

        // receive task
        activityDrawInstructions.put(ReceiveTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawReceiveTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // send task
        activityDrawInstructions.put(SendTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawSendTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // manual task
        activityDrawInstructions.put(ManualTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawManualTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // businessRuleTask task
        activityDrawInstructions.put(BusinessRuleTask.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawBusinessRuleTask(flowNode.getName(), graphicInfo, scaleFactor);
		});

        // exclusive gateway
        activityDrawInstructions.put(ExclusiveGateway.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawExclusiveGateway(graphicInfo, scaleFactor);
		});

        // inclusive gateway
        activityDrawInstructions.put(InclusiveGateway.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawInclusiveGateway(graphicInfo, scaleFactor);
		});

        // parallel gateway
        activityDrawInstructions.put(ParallelGateway.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawParallelGateway(graphicInfo, scaleFactor);
		});

        // event based gateway
        activityDrawInstructions.put(EventGateway.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawEventBasedGateway(graphicInfo, scaleFactor);
		});

        // Boundary timer
        activityDrawInstructions.put(BoundaryEvent.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			final BoundaryEvent boundaryEvent = (BoundaryEvent) flowNode;
			if (boundaryEvent.getEventDefinitions() != null && !boundaryEvent.getEventDefinitions().isEmpty()) {
				if (boundaryEvent.getEventDefinitions().get(0) instanceof TimerEventDefinition) {

					processDiagramCanvas.drawCatchingTimerEvent(flowNode.getName(), graphicInfo,
						boundaryEvent.isCancelActivity(), scaleFactor);

				} else if (boundaryEvent.getEventDefinitions().get(0) instanceof ErrorEventDefinition) {

					processDiagramCanvas.drawCatchingErrorEvent(graphicInfo, boundaryEvent.isCancelActivity(),
						scaleFactor);

				} else if (boundaryEvent.getEventDefinitions().get(0) instanceof SignalEventDefinition) {
					processDiagramCanvas.drawCatchingSignalEvent(flowNode.getName(), graphicInfo,
						boundaryEvent.isCancelActivity(), scaleFactor);

				} else if (boundaryEvent.getEventDefinitions().get(0) instanceof MessageEventDefinition) {
					processDiagramCanvas.drawCatchingMessageEvent(flowNode.getName(), graphicInfo,
						boundaryEvent.isCancelActivity(), scaleFactor);

				} else if (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
					processDiagramCanvas.drawCatchingCompensateEvent(graphicInfo, boundaryEvent.isCancelActivity(),
						scaleFactor);
				}
			}

		});

        // subprocess
        activityDrawInstructions.put(SubProcess.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			if (graphicInfo.getExpanded() != null && !graphicInfo.getExpanded()) {
				processDiagramCanvas.drawCollapsedSubProcess(flowNode.getName(), graphicInfo, false);
			} else {
				processDiagramCanvas.drawExpandedSubProcess(flowNode.getName(), graphicInfo, false, scaleFactor);
			}
		});

        // Event subprocess
        activityDrawInstructions.put(EventSubProcess.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			if (graphicInfo.getExpanded() != null && !graphicInfo.getExpanded()) {
				processDiagramCanvas.drawCollapsedSubProcess(flowNode.getName(), graphicInfo, true);
			} else {
				processDiagramCanvas.drawExpandedSubProcess(flowNode.getName(), graphicInfo, true, scaleFactor);
			}
		});

        // call activity
        activityDrawInstructions.put(CallActivity.class, (processDiagramCanvas, bpmnModel, flowNode) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
			processDiagramCanvas.drawCollapsedCallActivity(flowNode.getName(), graphicInfo);
		});

        // text annotation
        artifactDrawInstructions.put(TextAnnotation.class, (processDiagramCanvas, bpmnModel, artifact) -> {
			final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(artifact.getId());
			final TextAnnotation textAnnotation = (TextAnnotation) artifact;
			processDiagramCanvas.drawTextAnnotation(textAnnotation.getText(), graphicInfo);
		});

        // association
        artifactDrawInstructions.put(Association.class, (processDiagramCanvas, bpmnModel, artifact) -> {
			final Association association = (Association) artifact;
			final String sourceRef = association.getSourceRef();
			final String targetRef = association.getTargetRef();

			// source and target can be instance of FlowElement or Artifact
			BaseElement sourceElement = bpmnModel.getFlowElement(sourceRef);
			BaseElement targetElement = bpmnModel.getFlowElement(targetRef);
			if (sourceElement == null) {
				sourceElement = bpmnModel.getArtifact(sourceRef);
			}
			if (targetElement == null) {
				targetElement = bpmnModel.getArtifact(targetRef);
			}
			List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(artifact.getId());
			graphicInfoList = connectionPerfectionizer(processDiagramCanvas, bpmnModel, sourceElement,
				targetElement, graphicInfoList);
			final int[] xPoints = new int[graphicInfoList.size()];
			final int[] yPoints = new int[graphicInfoList.size()];
			for (int i = 1; i < graphicInfoList.size(); i++) {
				final GraphicInfo graphicInfo = graphicInfoList.get(i);
				final GraphicInfo previousGraphicInfo = graphicInfoList.get(i - 1);

				if (i == 1) {
					xPoints[0] = (int) previousGraphicInfo.getX();
					yPoints[0] = (int) previousGraphicInfo.getY();
				}
				xPoints[i] = (int) graphicInfo.getX();
				yPoints[i] = (int) graphicInfo.getY();
			}

			final AssociationDirection associationDirection = association.getAssociationDirection();
			processDiagramCanvas.drawAssociation(xPoints, yPoints, associationDirection, false, scaleFactor);
		});
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities,
        List<String> highLightedFlows,
        String activityFontName, String labelFontName, ClassLoader customClassLoader, double scaleFactor) {

        return generateProcessDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows,
            activityFontName, labelFontName, customClassLoader, scaleFactor).generateImage(imageType);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities,
        List<String> highLightedFlows) {
        return generateDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows, null, null, null, 1.0);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType,
        List<String> highLightedActivities, List<String> highLightedFlows, double scaleFactor) {
        return generateDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows, null, null, null,
            scaleFactor);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities) {
        return generateDiagram(bpmnModel, imageType, highLightedActivities, Collections.<String> emptyList());
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities,
        double scaleFactor) {
        return generateDiagram(bpmnModel, imageType, highLightedActivities, Collections.<String> emptyList(),
            scaleFactor);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, String activityFontName,
        String labelFontName, ClassLoader customClassLoader) {
        return generateDiagram(bpmnModel, imageType, Collections.<String> emptyList(), Collections.<String> emptyList(),
            activityFontName, labelFontName, customClassLoader, 1.0);
    }

    @Override
    public InputStream generateDiagram(BpmnModel bpmnModel, String imageType, String activityFontName,
        String labelFontName, ClassLoader customClassLoader, double scaleFactor) {

        return generateDiagram(bpmnModel, imageType, Collections.<String> emptyList(), Collections.<String> emptyList(),
            activityFontName, labelFontName, customClassLoader, scaleFactor);
    }

    @Override
    public InputStream generatePngDiagram(BpmnModel bpmnModel) {
        return generatePngDiagram(bpmnModel, 1.0);
    }

    @Override
    public InputStream generatePngDiagram(BpmnModel bpmnModel, double scaleFactor) {
        return generateDiagram(bpmnModel, "png", Collections.<String> emptyList(), Collections.<String> emptyList(),
            scaleFactor);
    }

    @Override
    public InputStream generateJpgDiagram(BpmnModel bpmnModel) {
        return generateJpgDiagram(bpmnModel, 1.0);
    }

    @Override
    public InputStream generateJpgDiagram(BpmnModel bpmnModel, double scaleFactor) {
        return generateDiagram(bpmnModel, "jpg", Collections.<String> emptyList(), Collections.<String> emptyList());
    }

    public BufferedImage generateImage(BpmnModel bpmnModel, String imageType, List<String> highLightedActivities,
        List<String> highLightedFlows,
        String activityFontName, String labelFontName, ClassLoader customClassLoader, double scaleFactor) {

        return generateProcessDiagram(bpmnModel, imageType, highLightedActivities, highLightedFlows,
            activityFontName, labelFontName, customClassLoader, scaleFactor).generateBufferedImage(imageType);
    }

    public BufferedImage generateImage(BpmnModel bpmnModel, String imageType,
        List<String> highLightedActivities, List<String> highLightedFlows, double scaleFactor) {

        return generateImage(bpmnModel, imageType, highLightedActivities, highLightedFlows, null, null, null,
            scaleFactor);
    }

    @Override
    public BufferedImage generatePngImage(BpmnModel bpmnModel, double scaleFactor) {
        return generateImage(bpmnModel, "png", Collections.<String> emptyList(), Collections.<String> emptyList(),
            scaleFactor);
    }

    protected DefaultProcessDiagramCanvas generateProcessDiagram(BpmnModel bpmnModel, String imageType,
        List<String> highLightedActivities, List<String> highLightedFlows,
        String activityFontName, String labelFontName, ClassLoader customClassLoader, double scaleFactor) {

        prepareBpmnModel(bpmnModel);

        final DefaultProcessDiagramCanvas processDiagramCanvas = initProcessDiagramCanvas(bpmnModel, imageType,
            activityFontName, labelFontName, customClassLoader);

        // Draw pool shape, if process is participant in collaboration
        for (final Pool pool : bpmnModel.getPools()) {
            final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            processDiagramCanvas.drawPoolOrLane(pool.getName(), graphicInfo);
        }

        // Draw lanes
        for (final Process process : bpmnModel.getProcesses()) {
            for (final Lane lane : process.getLanes()) {
                final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(lane.getId());
                processDiagramCanvas.drawPoolOrLane(lane.getName(), graphicInfo);
            }
        }

        // Draw activities and their sequence-flows
        for (final FlowNode flowNode : bpmnModel.getProcesses().get(0).findFlowElementsOfType(FlowNode.class)) {
            drawActivity(processDiagramCanvas, bpmnModel, flowNode, highLightedActivities, highLightedFlows,
                scaleFactor);
        }

        // Draw artifacts
        for (final Process process : bpmnModel.getProcesses()) {
            for (final Artifact artifact : process.getArtifacts()) {
                drawArtifact(processDiagramCanvas, bpmnModel, artifact);
            }
        }

        return processDiagramCanvas;
    }

    protected void prepareBpmnModel(BpmnModel bpmnModel) {

        // Need to make sure all elements have positive x and y.
        // Check all graphicInfo and update the elements accordingly

        final List<GraphicInfo> allGraphicInfos = new ArrayList<GraphicInfo>();
        if (bpmnModel.getLocationMap() != null) {
            allGraphicInfos.addAll(bpmnModel.getLocationMap().values());
        }
        if (bpmnModel.getLabelLocationMap() != null) {
            allGraphicInfos.addAll(bpmnModel.getLabelLocationMap().values());
        }
        if (bpmnModel.getFlowLocationMap() != null) {
            for (final List<GraphicInfo> flowGraphicInfos : bpmnModel.getFlowLocationMap().values()) {
                allGraphicInfos.addAll(flowGraphicInfos);
            }
        }

        if (allGraphicInfos.size() > 0) {

            boolean needsTranslationX = false;
            boolean needsTranslationY = false;

            double lowestX = 0.0;
            double lowestY = 0.0;

            // Collect lowest x and y
            for (final GraphicInfo graphicInfo : allGraphicInfos) {

                final double x = graphicInfo.getX();
                final double y = graphicInfo.getY();

                if (x < lowestX) {
                    needsTranslationX = true;
                    lowestX = x;
                }
                if (y < lowestY) {
                    needsTranslationY = true;
                    lowestY = y;
                }

            }

            // Update all graphicInfo objects
            if (needsTranslationX || needsTranslationY) {

                final double translationX = Math.abs(lowestX);
                final double translationY = Math.abs(lowestY);

                for (final GraphicInfo graphicInfo : allGraphicInfos) {
                    if (needsTranslationX) {
                        graphicInfo.setX(graphicInfo.getX() + translationX);
                    }
                    if (needsTranslationY) {
                        graphicInfo.setY(graphicInfo.getY() + translationY);
                    }
                }
            }

        }

    }

    protected void drawActivity(DefaultProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel,
        FlowNode flowNode, List<String> highLightedActivities, List<String> highLightedFlows, double scaleFactor) {

        final ActivityDrawInstruction drawInstruction = activityDrawInstructions.get(flowNode.getClass());
        if (drawInstruction != null) {

            drawInstruction.draw(processDiagramCanvas, bpmnModel, flowNode);

            // Gather info on the multi instance marker
            boolean multiInstanceSequential = false, multiInstanceParallel = false, collapsed = false;
            if (flowNode instanceof Activity) {
                final Activity activity = (Activity) flowNode;
                final MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = activity
                    .getLoopCharacteristics();
                if (multiInstanceLoopCharacteristics != null) {
                    multiInstanceSequential = multiInstanceLoopCharacteristics.isSequential();
                    multiInstanceParallel = !multiInstanceSequential;
                }
            }

            // Gather info on the collapsed marker
            final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());
            if (flowNode instanceof SubProcess) {
                collapsed = graphicInfo.getExpanded() != null && !graphicInfo.getExpanded();
            } else if (flowNode instanceof CallActivity) {
                collapsed = true;
            }

            if (new BigDecimal(String.valueOf(scaleFactor)).equals(new BigDecimal("1.0"))) {
                // Actually draw the markers
                processDiagramCanvas.drawActivityMarkers((int) graphicInfo.getX(), (int) graphicInfo.getY(),
                    (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight(),
                    multiInstanceSequential, multiInstanceParallel, collapsed);
            }

            // Draw highlighted activities
            if (highLightedActivities.contains(flowNode.getId())) {
                drawHighLight(processDiagramCanvas, bpmnModel.getGraphicInfo(flowNode.getId()));
            }

        }

        // Outgoing transitions of activity
        for (final SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
            final boolean highLighted = (highLightedFlows.contains(sequenceFlow.getId()));
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
            final boolean drawConditionalIndicator = sequenceFlow.getConditionExpression() != null
                && !(flowNode instanceof Gateway);

            final String sourceRef = sequenceFlow.getSourceRef();
            final String targetRef = sequenceFlow.getTargetRef();
            final FlowElement sourceElement = bpmnModel.getFlowElement(sourceRef);
            final FlowElement targetElement = bpmnModel.getFlowElement(targetRef);
            List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
            if (graphicInfoList != null && graphicInfoList.size() > 0) {
                graphicInfoList = connectionPerfectionizer(processDiagramCanvas, bpmnModel, sourceElement,
                    targetElement, graphicInfoList);
                final int[] xPoints = new int[graphicInfoList.size()];
                final int[] yPoints = new int[graphicInfoList.size()];

                for (int i = 1; i < graphicInfoList.size(); i++) {
                    final GraphicInfo graphicInfo = graphicInfoList.get(i);
                    final GraphicInfo previousGraphicInfo = graphicInfoList.get(i - 1);

                    if (i == 1) {
                        xPoints[0] = (int) previousGraphicInfo.getX();
                        yPoints[0] = (int) previousGraphicInfo.getY();
                    }
                    xPoints[i] = (int) graphicInfo.getX();
                    yPoints[i] = (int) graphicInfo.getY();

                }

                processDiagramCanvas.drawSequenceflow(xPoints, yPoints, drawConditionalIndicator, isDefault,
                    highLighted, scaleFactor);

                // Draw sequenceflow label
                final GraphicInfo labelGraphicInfo = bpmnModel.getLabelGraphicInfo(sequenceFlow.getId());
                if (labelGraphicInfo != null) {
                    processDiagramCanvas.drawLabel(sequenceFlow.getName(), labelGraphicInfo, false);
                }
            }
        }

        // Nested elements
        if (flowNode instanceof FlowElementsContainer) {
            for (final FlowElement nestedFlowElement : ((FlowElementsContainer) flowNode).getFlowElements()) {
                if (nestedFlowElement instanceof FlowNode) {
                    drawActivity(processDiagramCanvas, bpmnModel, (FlowNode) nestedFlowElement,
                        highLightedActivities, highLightedFlows, scaleFactor);
                }
            }
        }
    }

    /**
     * This method makes coordinates of connection flow better.
     *
     * @param processDiagramCanvas
     * @param bpmnModel
     * @param sourceElement
     * @param targetElement
     * @param graphicInfoList
     * @return
     */
    protected static List<GraphicInfo> connectionPerfectionizer(DefaultProcessDiagramCanvas processDiagramCanvas,
        BpmnModel bpmnModel, BaseElement sourceElement, BaseElement targetElement, List<GraphicInfo> graphicInfoList) {
        final GraphicInfo sourceGraphicInfo = bpmnModel.getGraphicInfo(sourceElement.getId());
        final GraphicInfo targetGraphicInfo = bpmnModel.getGraphicInfo(targetElement.getId());

        final DefaultProcessDiagramCanvas.SHAPE_TYPE sourceShapeType = getShapeType(sourceElement);
        final DefaultProcessDiagramCanvas.SHAPE_TYPE targetShapeType = getShapeType(targetElement);

        return processDiagramCanvas.connectionPerfectionizer(sourceShapeType, targetShapeType, sourceGraphicInfo,
            targetGraphicInfo, graphicInfoList);
    }

    /**
     * This method returns shape type of base element.<br>
     * Each element can be presented as rectangle, rhombus, or ellipse.
     *
     * @param baseElement
     * @return DefaultProcessDiagramCanvas.SHAPE_TYPE
     */
    protected static DefaultProcessDiagramCanvas.SHAPE_TYPE getShapeType(BaseElement baseElement) {
        if (baseElement instanceof Task || baseElement instanceof Activity || baseElement instanceof TextAnnotation) {
            return DefaultProcessDiagramCanvas.SHAPE_TYPE.Rectangle;
        } else if (baseElement instanceof Gateway) {
            return DefaultProcessDiagramCanvas.SHAPE_TYPE.Rhombus;
        } else if (baseElement instanceof Event) {
            return DefaultProcessDiagramCanvas.SHAPE_TYPE.Ellipse;
        } else {
            // unknown source element, just do not correct coordinates
        }
        return null;
    }

    protected static GraphicInfo getLineCenter(List<GraphicInfo> graphicInfoList) {
        final GraphicInfo gi = new GraphicInfo();

        final int[] xPoints = new int[graphicInfoList.size()];
        final int[] yPoints = new int[graphicInfoList.size()];

        double length = 0;
        final double[] lengths = new double[graphicInfoList.size()];
        lengths[0] = 0;
        double m;
        for (int i = 1; i < graphicInfoList.size(); i++) {
            final GraphicInfo graphicInfo = graphicInfoList.get(i);
            final GraphicInfo previousGraphicInfo = graphicInfoList.get(i - 1);

            if (i == 1) {
                xPoints[0] = (int) previousGraphicInfo.getX();
                yPoints[0] = (int) previousGraphicInfo.getY();
            }
            xPoints[i] = (int) graphicInfo.getX();
            yPoints[i] = (int) graphicInfo.getY();

            length += Math.sqrt(
                Math.pow((int) graphicInfo.getX() - (int) previousGraphicInfo.getX(), 2) +
                    Math.pow((int) graphicInfo.getY() - (int) previousGraphicInfo.getY(), 2));
            lengths[i] = length;
        }
        m = length / 2;
        int p1 = 0, p2 = 1;
        for (int i = 1; i < lengths.length; i++) {
            final double len = lengths[i];
            p1 = i - 1;
            p2 = i;
            if (len > m) {
                break;
            }
        }

        final GraphicInfo graphicInfo1 = graphicInfoList.get(p1);
        final GraphicInfo graphicInfo2 = graphicInfoList.get(p2);

        final double AB = (int) graphicInfo2.getX() - (int) graphicInfo1.getX();
        final double OA = (int) graphicInfo2.getY() - (int) graphicInfo1.getY();
        final double OB = lengths[p2] - lengths[p1];
        final double ob = m - lengths[p1];
        final double ab = AB * ob / OB;
        final double oa = OA * ob / OB;

        final double mx = graphicInfo1.getX() + ab;
        final double my = graphicInfo1.getY() + oa;

        gi.setX(mx);
        gi.setY(my);
        return gi;
    }

    protected void drawArtifact(DefaultProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel,
        Artifact artifact) {

        final ArtifactDrawInstruction drawInstruction = artifactDrawInstructions.get(artifact.getClass());
        if (drawInstruction != null) {
            drawInstruction.draw(processDiagramCanvas, bpmnModel, artifact);
        }
    }

    private static void drawHighLight(DefaultProcessDiagramCanvas processDiagramCanvas, GraphicInfo graphicInfo) {
        processDiagramCanvas.drawHighLight((int) graphicInfo.getX(), (int) graphicInfo.getY(),
            (int) graphicInfo.getWidth(), (int) graphicInfo.getHeight());

    }

    protected static DefaultProcessDiagramCanvas initProcessDiagramCanvas(BpmnModel bpmnModel, String imageType,
        String activityFontName, String labelFontName, ClassLoader customClassLoader) {

        // We need to calculate maximum values to know how big the image will be in its entirety
        double minX = Double.MAX_VALUE;
        double maxX = 0;
        double minY = Double.MAX_VALUE;
        double maxY = 0;

        for (final Pool pool : bpmnModel.getPools()) {
            final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(pool.getId());
            minX = graphicInfo.getX();
            maxX = graphicInfo.getX() + graphicInfo.getWidth();
            minY = graphicInfo.getY();
            maxY = graphicInfo.getY() + graphicInfo.getHeight();
        }

        final List<FlowNode> flowNodes = gatherAllFlowNodes(bpmnModel);
        for (final FlowNode flowNode : flowNodes) {

            final GraphicInfo flowNodeGraphicInfo = bpmnModel.getGraphicInfo(flowNode.getId());

            // width
            if (flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth() > maxX) {
                maxX = flowNodeGraphicInfo.getX() + flowNodeGraphicInfo.getWidth();
            }
            if (flowNodeGraphicInfo.getX() < minX) {
                minX = flowNodeGraphicInfo.getX();
            }
            // height
            if (flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight() > maxY) {
                maxY = flowNodeGraphicInfo.getY() + flowNodeGraphicInfo.getHeight();
            }
            if (flowNodeGraphicInfo.getY() < minY) {
                minY = flowNodeGraphicInfo.getY();
            }

            for (final SequenceFlow sequenceFlow : flowNode.getOutgoingFlows()) {
                final List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(sequenceFlow.getId());
                if (graphicInfoList != null) {
                    for (final GraphicInfo graphicInfo : graphicInfoList) {
                        // width
                        if (graphicInfo.getX() > maxX) {
                            maxX = graphicInfo.getX();
                        }
                        if (graphicInfo.getX() < minX) {
                            minX = graphicInfo.getX();
                        }
                        // height
                        if (graphicInfo.getY() > maxY) {
                            maxY = graphicInfo.getY();
                        }
                        if (graphicInfo.getY() < minY) {
                            minY = graphicInfo.getY();
                        }
                    }
                }
            }
        }

        final List<Artifact> artifacts = gatherAllArtifacts(bpmnModel);
        for (final Artifact artifact : artifacts) {

            final GraphicInfo artifactGraphicInfo = bpmnModel.getGraphicInfo(artifact.getId());

            if (artifactGraphicInfo != null) {
                // width
                if (artifactGraphicInfo.getX() + artifactGraphicInfo.getWidth() > maxX) {
                    maxX = artifactGraphicInfo.getX() + artifactGraphicInfo.getWidth();
                }
                if (artifactGraphicInfo.getX() < minX) {
                    minX = artifactGraphicInfo.getX();
                }
                // height
                if (artifactGraphicInfo.getY() + artifactGraphicInfo.getHeight() > maxY) {
                    maxY = artifactGraphicInfo.getY() + artifactGraphicInfo.getHeight();
                }
                if (artifactGraphicInfo.getY() < minY) {
                    minY = artifactGraphicInfo.getY();
                }
            }

            final List<GraphicInfo> graphicInfoList = bpmnModel.getFlowLocationGraphicInfo(artifact.getId());
            if (graphicInfoList != null) {
                for (final GraphicInfo graphicInfo : graphicInfoList) {
                    // width
                    if (graphicInfo.getX() > maxX) {
                        maxX = graphicInfo.getX();
                    }
                    if (graphicInfo.getX() < minX) {
                        minX = graphicInfo.getX();
                    }
                    // height
                    if (graphicInfo.getY() > maxY) {
                        maxY = graphicInfo.getY();
                    }
                    if (graphicInfo.getY() < minY) {
                        minY = graphicInfo.getY();
                    }
                }
            }
        }

        int nrOfLanes = 0;
        for (final Process process : bpmnModel.getProcesses()) {
            for (final Lane l : process.getLanes()) {

                nrOfLanes++;

                final GraphicInfo graphicInfo = bpmnModel.getGraphicInfo(l.getId());
                // // width
                if (graphicInfo.getX() + graphicInfo.getWidth() > maxX) {
                    maxX = graphicInfo.getX() + graphicInfo.getWidth();
                }
                if (graphicInfo.getX() < minX) {
                    minX = graphicInfo.getX();
                }
                // height
                if (graphicInfo.getY() + graphicInfo.getHeight() > maxY) {
                    maxY = graphicInfo.getY() + graphicInfo.getHeight();
                }
                if (graphicInfo.getY() < minY) {
                    minY = graphicInfo.getY();
                }
            }
        }

        // Special case, see https://activiti.atlassian.net/browse/ACT-1431
        if (flowNodes.isEmpty() && bpmnModel.getPools().isEmpty() && nrOfLanes == 0) {
            // Nothing to show
            minX = 0;
            minY = 0;
        }

        return new CesProcessDiagramCanvas((int) maxX + 10, (int) maxY + 10, (int) minX, (int) minY,
            imageType, activityFontName, labelFontName, customClassLoader);
    }

    protected static List<Artifact> gatherAllArtifacts(BpmnModel bpmnModel) {
        final List<Artifact> artifacts = new ArrayList<Artifact>();
        for (final Process process : bpmnModel.getProcesses()) {
            artifacts.addAll(process.getArtifacts());
        }
        return artifacts;
    }

    protected static List<FlowNode> gatherAllFlowNodes(BpmnModel bpmnModel) {
        final List<FlowNode> flowNodes = new ArrayList<FlowNode>();
        for (final Process process : bpmnModel.getProcesses()) {
            flowNodes.addAll(gatherAllFlowNodes(process));
        }
        return flowNodes;
    }

    protected static List<FlowNode> gatherAllFlowNodes(FlowElementsContainer flowElementsContainer) {
        final List<FlowNode> flowNodes = new ArrayList<FlowNode>();
        for (final FlowElement flowElement : flowElementsContainer.getFlowElements()) {
            if (flowElement instanceof FlowNode) {
                flowNodes.add((FlowNode) flowElement);
            }
            if (flowElement instanceof FlowElementsContainer) {
                flowNodes.addAll(gatherAllFlowNodes((FlowElementsContainer) flowElement));
            }
        }
        return flowNodes;
    }

    public Map<Class<? extends BaseElement>, ActivityDrawInstruction> getActivityDrawInstructions() {
        return activityDrawInstructions;
    }

    public void setActivityDrawInstructions(
        Map<Class<? extends BaseElement>, ActivityDrawInstruction> activityDrawInstructions) {
        this.activityDrawInstructions = activityDrawInstructions;
    }

    public Map<Class<? extends BaseElement>, ArtifactDrawInstruction> getArtifactDrawInstructions() {
        return artifactDrawInstructions;
    }

    public void setArtifactDrawInstructions(
        Map<Class<? extends BaseElement>, ArtifactDrawInstruction> artifactDrawInstructions) {
        this.artifactDrawInstructions = artifactDrawInstructions;
    }

    protected interface ActivityDrawInstruction {

        void draw(DefaultProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, FlowNode flowNode);
    }

    protected interface ArtifactDrawInstruction {

        void draw(DefaultProcessDiagramCanvas processDiagramCanvas, BpmnModel bpmnModel, Artifact artifact);
    }

}
