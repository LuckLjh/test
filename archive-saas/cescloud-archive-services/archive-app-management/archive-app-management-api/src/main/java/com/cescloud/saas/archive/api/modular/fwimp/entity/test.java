package com.cescloud.saas.archive.api.modular.fwimp.entity;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
public class test {
	//用Element方式
	public static void element(NodeList list){
		for (int i = 0; i <list.getLength() ; i++) {
			Element element = (Element) list.item(i);
			NodeList childNodes = element.getChildNodes();
			for (int j = 0; j <childNodes.getLength() ; j++) {
				if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
					//获取节点
					System.out.print(childNodes.item(j).getNodeName() + ":");
					//获取节点值
					System.out.println(childNodes.item(j).getFirstChild().getNodeValue());
				}
			}
		}
	}

	public static void node(NodeList nodeList){
/*		for (int i = 0; i <list.getLength() ; i++) {
			Node node = list.item(i);
			NodeList childNodes = node.getChildNodes();
			for (int j = 0; j <childNodes.getLength() ; j++) {
				if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
					System.out.print(childNodes.item(j).getNodeName() + ":");
					if(childNodes.item(j).getFirstChild() == null) {
						System.out.println(childNodes.item(j).getFirstChild().getNodeValue() + "");
					}else{
						System.out.println( "没有");
					}
				}
			}
		}*/
		for(int b = 0; b< nodeList.getLength() ; b ++){
			//获取头部信息
			//XML中的WorkflowId节点值
			Element element = (Element)nodeList.item(b);
			String WorkflowId=element.getElementsByTagName("WorkflowId").item(0).getFirstChild().getNodeValue();
			//if(WorkflowId.equals(OaFlowid)){
			String	sn = element.getElementsByTagName("Sn").item(0).getFirstChild().getNodeValue();
			String	title =  element.getElementsByTagName("Title").item(0).getFirstChild().getNodeValue();

			/**2.获取XML字段节点信息**/
			NodeList Fieldlist = element.getElementsByTagName("Field");
			//获取过程信息
			NodeList opinionList = element.getElementsByTagName("Opinion");

			System.out.print(nodeList.getLength() +"长度");
			System.out.print(sn);
			System.out.print(title);
		}
	}

	public static void main(String[] args) {
		//1.创建DocumentBuilderFactory对象
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		//2.创建DocumentBuilder对象
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document d = builder.parse("F:/log/WFL_376408.xml");
			NodeList sList = d.getElementsByTagName("Results");
			//element(sList);
			node(sList);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
