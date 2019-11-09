/**
 * Copyright 2009-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.parsing;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.builder.BuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * XPath 解析器
 *
 * @author Clinton Begin
 * @author Kazuki Shimizu
 */
public class XPathParser {

  /**
   * XML Document 对象
   * XML 解析后，生成该对象
   */
  private final Document document;
  /**
   * 是否校验
   */
  private boolean validation;
  /**
   * XML 实体解析器
   */
  private EntityResolver entityResolver;
  /**
   * 变量 Properties 对象
   * 用于替换需要动态替换的属性值
   */
  private Properties variables;
  /**
   * Java XPath 对象
   * 用于查询 XML 中的节点与元素
   */
  private XPath xpath;

  /**
   * @param xml
   */
  public XPathParser(String xml) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream) {
    commonConstructor(false, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document) {
    commonConstructor(false, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation) {
    commonConstructor(validation, null, null);
    this.document = document;
  }

  public XPathParser(String xml, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables) {
    commonConstructor(validation, variables, null);
    this.document = document;
  }

  /**
   * @param xml            XML 文件地址
   * @param validation     是否校验
   * @param variables      变量对象
   * @param entityResolver XML 实体解析器
   */
  public XPathParser(String xml, boolean validation, Properties variables, EntityResolver entityResolver) {
    // 调用公用构造器设置属性值
    commonConstructor(validation, variables, entityResolver);
    // 获取 Document 对象
    this.document = createDocument(new InputSource(new StringReader(xml)));
  }

  public XPathParser(Reader reader, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(reader));
  }

  public XPathParser(InputStream inputStream, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = createDocument(new InputSource(inputStream));
  }

  public XPathParser(Document document, boolean validation, Properties variables, EntityResolver entityResolver) {
    commonConstructor(validation, variables, entityResolver);
    this.document = document;
  }

  public void setVariables(Properties variables) {
    this.variables = variables;
  }

  public String evalString(String expression) {
    return evalString(document, expression);
  }

  /**
   * 用于获得 String 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public String evalString(Object root, String expression) {
    // 获取值
    String result = (String) evaluate(expression, root, XPathConstants.STRING);
    // 基于 variables 替换动态值，如果 result 为动态值
    result = PropertyParser.parse(result, variables);
    return result;
  }

  public Boolean evalBoolean(String expression) {
    return evalBoolean(document, expression);
  }

  /**
   * 用于获得 Boolean 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Boolean evalBoolean(Object root, String expression) {
    return (Boolean) evaluate(expression, root, XPathConstants.BOOLEAN);
  }

  public Short evalShort(String expression) {
    return evalShort(document, expression);
  }

  /**
   * 用于获得 Short 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Short evalShort(Object root, String expression) {
    return Short.valueOf(evalString(root, expression));
  }

  public Integer evalInteger(String expression) {
    return evalInteger(document, expression);
  }

  /**
   * 用于获得 Integer 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Integer evalInteger(Object root, String expression) {
    return Integer.valueOf(evalString(root, expression));
  }

  public Long evalLong(String expression) {
    return evalLong(document, expression);
  }

  /**
   * 用于获得 Long 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Long evalLong(Object root, String expression) {
    return Long.valueOf(evalString(root, expression));
  }

  public Float evalFloat(String expression) {
    return evalFloat(document, expression);
  }

  /**
   * 用于获得 Float 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Float evalFloat(Object root, String expression) {
    return Float.valueOf(evalString(root, expression));
  }

  public Double evalDouble(String expression) {
    return evalDouble(document, expression);
  }

  /**
   * 用于获得 Double 类型的元素的值
   *
   * @param root
   * @param expression
   * @return
   */
  public Double evalDouble(Object root, String expression) {
    return (Double) evaluate(expression, root, XPathConstants.NUMBER);
  }

  /**
   * 获取全部 Node 信息
   *
   * @param expression
   * @return
   */
  public List<XNode> evalNodes(String expression) {
    return evalNodes(document, expression);
  }

  /**
   * 用于获得全部 Node 类型的节点的信息
   *
   * @param root
   * @param expression
   * @return
   */
  public List<XNode> evalNodes(Object root, String expression) {
    // 获取 Node 数组
    NodeList nodes = (NodeList) evaluate(expression, root, XPathConstants.NODESET);

    // 分装成数组
    List<XNode> xnodes = new ArrayList<>();
    for (int i = 0; i < nodes.getLength(); i++) {
      xnodes.add(new XNode(this, nodes.item(i), variables));
    }
    return xnodes;
  }

  /**
   * 获取 Node 信息
   *
   * @param expression
   * @return
   */
  public XNode evalNode(String expression) {
    return evalNode(document, expression);
  }

  /**
   * 获取 Node 信息
   *
   * @param root
   * @param expression
   * @return
   */
  public XNode evalNode(Object root, String expression) {
    Node node = (Node) evaluate(expression, root, XPathConstants.NODE);
    if (node == null) {
      return null;
    }
    return new XNode(this, node, variables);
  }

  /**
   * 获得指定元素或节点的值
   * 用于获得 Boolean、Short、Integer、Long、Float、Double、String、Node 类型的元素或节点的值
   *
   * @param expression expression 表达式
   * @param root       指定节点
   * @param returnType 返回类型
   * @return
   */
  private Object evaluate(String expression, Object root, QName returnType) {
    try {
      return xpath.evaluate(expression, root, returnType);
    } catch (Exception e) {
      throw new BuilderException("Error evaluating XPath.  Cause: " + e, e);
    }
  }

  /**
   * 创建 Document 对象
   *
   * @param inputSource
   * @return
   */
  private Document createDocument(InputSource inputSource) {
    // important: this must only be called AFTER common constructor
    try {
      /**
       * 1. 创建 DocumentBuilderFactory 对象
       */
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // 设置安全处理规范
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      // 设置是否验证 XML
      factory.setValidating(validation);

      factory.setNamespaceAware(false);
      // 忽略
      factory.setIgnoringComments(true);
      // 设置忽略元素空白
      factory.setIgnoringElementContentWhitespace(false);
      // 设置合并
      factory.setCoalescing(false);
      // 设置扩大实体引用
      factory.setExpandEntityReferences(true);

      /**
       * 2. 创建 DocumentBuilder 对象
       */
      DocumentBuilder builder = factory.newDocumentBuilder();
      // 设置实体解析器
      builder.setEntityResolver(entityResolver);
      // 设置失败操作
      builder.setErrorHandler(new ErrorHandler() {
        @Override
        public void error(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
          throw exception;
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
          // NOP
        }
      });

      /**
       * 3. 解析 XML 文件
       */
      return builder.parse(inputSource);
    } catch (Exception e) {
      throw new BuilderException("Error creating document instance.  Cause: " + e, e);
    }
  }

  /**
   * 公有构造器
   *
   * @param validation
   * @param variables
   * @param entityResolver
   */
  private void commonConstructor(boolean validation, Properties variables, EntityResolver entityResolver) {
    this.validation = validation;
    this.entityResolver = entityResolver;
    this.variables = variables;
    // 创建 XPathFactory 对象
    XPathFactory factory = XPathFactory.newInstance();
    this.xpath = factory.newXPath();
  }

}
